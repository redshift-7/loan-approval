package com.example.loans.model;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;

public interface ApprovalRequest {
    BigDecimal getLoanAmount();
    ZonedDateTime getTimestamp();
    String getCustomerId();
    void setUuid(UUID uuid);
    UUID getUuid();
    Set<Approver> getApprovers();
    void setDecisionState(DecisionState approved);
    DecisionState getDecisionState();
}
