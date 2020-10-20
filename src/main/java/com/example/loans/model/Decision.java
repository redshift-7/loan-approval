package com.example.loans.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Decision implements Comparable<Decision> {
    long loanApprovalRequestId;
    String customerId;
    String approverUsername;
    DecisionState state;

    @Override
    public int compareTo(Decision o) {
        return approverUsername.compareTo(o.approverUsername);
    }
}
