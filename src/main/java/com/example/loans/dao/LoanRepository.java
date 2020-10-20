package com.example.loans.dao;

import com.example.loans.exception.BusinessRuleException;
import com.example.loans.model.*;
import com.example.loans.task.NotifyCustomerTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

import static com.example.loans.model.DecisionState.*;

@Slf4j
@Component
public class LoanRepository {

    final ConcurrentSkipListMap<EventKey, UUID> events;
    final ConcurrentHashMap<ApprovalRequest, ConcurrentHashMap<String, Decision>> storage;
    private final ThreadPoolTaskScheduler threadPoolTaskScheduler;

    public LoanRepository(ConcurrentSkipListMap<EventKey, UUID> events,
                          ConcurrentHashMap<ApprovalRequest, ConcurrentHashMap<String, Decision>> storage,
                          ThreadPoolTaskScheduler threadPoolTaskScheduler) {
        this.events = events;
        this.storage = storage;
        this.threadPoolTaskScheduler = threadPoolTaskScheduler;
    }

    /**
     * Saves new LoanApprovalRequests. Non-blocking. Uses CAS.
     *
     * @param loanApprovalRequest
     */
    public void saveAsPending(ApprovalRequest loanApprovalRequest) {
        log.info("Trying to save: {}", loanApprovalRequest);

        UUID uuid = UUID.randomUUID();
        ZonedDateTime now = ZonedDateTime.now();

        UUID resultUuid = events.putIfAbsent(new EventKey(now, uuid), uuid);

        if (resultUuid != null) {
            log.debug("Failed events.putIfAbsent({}}, {}})", now.toInstant().toEpochMilli(), uuid);
        }
        loanApprovalRequest.setUuid(uuid);

        if (storage.containsKey(loanApprovalRequest)) {
            log.error("Attempt to save duplicate pending LoanApprovalRequest: {}", loanApprovalRequest);
            throw new BusinessRuleException("Attempt to save duplicate pending LoanApprovalRequest: " + loanApprovalRequest);
        }

        ConcurrentHashMap<String, Decision> putResult = storage.putIfAbsent(loanApprovalRequest, getDecisionMap(loanApprovalRequest));

        if (putResult == null) {
            log.info("Successfully added new LoanApprovalRequest: {}", loanApprovalRequest);
        } else {
            log.error("Save failed. Duplicate LoanApprovalRequest: {}", loanApprovalRequest);
            throw new BusinessRuleException("Fail to add LoanApprovalRequest. Duplicate LoanApprovalRequest: " + loanApprovalRequest);
        }
    }

    private ConcurrentHashMap<String, Decision> getDecisionMap(ApprovalRequest loanApprovalRequest) {
        ConcurrentHashMap<String, Decision> decisions = new ConcurrentHashMap<>();

        for (Approver approver : loanApprovalRequest.getApprovers()) {
            Decision pendingDecision = Decision.builder()
                    .customerId(loanApprovalRequest.getCustomerId())
                    .approverUsername(approver.getName())
                    .state(PENDING)
                    .build();
            decisions.put(approver.getName(), pendingDecision);
        }
        return decisions;
    }

    /**
     * Saves decision only if related LoanApprovalRequest with correct customerId exists in storage.
     *
     * @param decision
     * @return
     */
    public void save(final Decision decision) {
        String msg;

        if (decision.getState() == PENDING) {
            msg = "PENDING decisions cannot be saved";
            log.error(msg);
            throw new BusinessRuleException(msg);
        }
        String customerId = decision.getCustomerId();
        ApprovalRequest lookupKey = LoanApprovalRequest.builder()
                .customerId(customerId)
                .decisionState(PENDING)
                .build();

        ConcurrentHashMap<String, Decision> allDecisions = storage.get(lookupKey);

        if (allDecisions == null) {
            msg = "Decision cannot be saved. There is no related ApprovalRequest with customerId: " + customerId;
            log.error(msg);
            throw new BusinessRuleException(msg);
        }
        allDecisions.put(decision.getApproverUsername(), decision);
        processDecisions(lookupKey, allDecisions);
        log.info("Decision is saved");
    }

    /**
     * Checks if all decisions are of the same DecisionState Type.
     * If true then update DecisionState of LoanApprovalRequest and run NotifyCustomerTask
     * @param approvalRequest
     * @param allDecisions
     */
    private void processDecisions(ApprovalRequest approvalRequest, ConcurrentHashMap<String, Decision> allDecisions) {
        boolean allApproved = allMatchState(allDecisions, APPROVED);
        boolean allDeclined = allMatchState(allDecisions, DECLINED);

        if (allApproved || allDeclined) {
            log.info("The decisions is {} by everyone", allApproved ? APPROVED.name() : DECLINED.name());
            if (allApproved) {
                approvalRequest.setDecisionState(APPROVED);
            } else {
                approvalRequest.setDecisionState(DECLINED);
            }

            storage.put(approvalRequest, allDecisions);
            threadPoolTaskScheduler.execute(new NotifyCustomerTask(approvalRequest, allDecisions));
        }
    }

    private boolean allMatchState(ConcurrentHashMap<String, Decision> allDecisions, DecisionState decisionState) {
        return allDecisions.values().stream().allMatch(v -> v.getState() == decisionState);
    }

    public Statistics getStatistics(Duration period) {
        log.info("Getting statistics for period {}sec", period.getSeconds());
        Set<UUID> eventIds = new HashSet<>(events.tailMap(new EventKey(ZonedDateTime.now().minus(period))).values());

        List<BigDecimal> amounts = storage.keySet().stream()
                .filter(storageKey -> eventIds.contains(storageKey.getUuid()))
                .map(ApprovalRequest::getLoanAmount)
                .collect(Collectors.toList());

        Statistics statistics = Statistics.calculate(amounts);
        log.info("Statistics: {}", statistics.toString());
        return statistics;
    }
}
