package com.example.loans.service;

import com.example.loans.dao.LoanRepository;
import com.example.loans.dto.DecisionDto;
import com.example.loans.dto.LoanApprovalRequestDto;
import com.example.loans.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

import static com.example.loans.model.DecisionState.PENDING;

@Service
public class LoanService {

    @Autowired
    LoanRepository loanRepository;

    @Value("${app.statistics.period:60s}")
    Duration statisticsPeriod;

    public void saveApproval(LoanApprovalRequestDto dto) {
        Set<Approver> approvers = new HashSet<>();
        dto.getApprovers().forEach(approver -> approvers.add(new Approver(approver)));

        LoanApprovalRequest approvalRequest = LoanApprovalRequest.builder()
                .customerId(dto.getCustomerId())
                .loanAmount(dto.getLoanAmount())
                .approvers(approvers)
                .decisionState(PENDING)
                .timestamp(ZonedDateTime.now())
                .build();

        approvalRequest.setTimestamp(ZonedDateTime.now());

        loanRepository.saveAsPending(approvalRequest);
    }

    public void makeDecision(DecisionDto dto) {
        Decision decision = Decision.builder()
                .customerId(dto.getCustomerId())
                .approverUsername(dto.getApproverUsername())
                .state(DecisionState.valueOf(dto.getState()))
                .build();
        loanRepository.save(decision);
    }

    public Statistics getStatistics() {
        return loanRepository.getStatistics(statisticsPeriod);
    }
}
