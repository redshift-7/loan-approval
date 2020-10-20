package com.example.loans.task;

import com.example.loans.model.ApprovalRequest;
import com.example.loans.model.Decision;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class NotifyCustomerTask implements Runnable {

    private final ApprovalRequest loanApprovalRequest;
    private final Map<String, Decision> decisions;

    public NotifyCustomerTask(ApprovalRequest loanApprovalRequest, Map<String, Decision> decisions) {
        this.decisions = decisions;
        this.loanApprovalRequest = loanApprovalRequest;
    }

    @Override
    public void run() {
        log.info("Sending notification to customer: {}, decision: {}, approvers: [{}]",
                loanApprovalRequest.getCustomerId(),
                loanApprovalRequest.getDecisionState(),
                String.join(", ", decisions.keySet()));
    }
}
