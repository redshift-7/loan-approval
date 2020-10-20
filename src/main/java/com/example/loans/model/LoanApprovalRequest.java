package com.example.loans.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static com.example.loans.model.DecisionState.PENDING;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanApprovalRequest implements ApprovalRequest {

    String customerId;
    BigDecimal loanAmount;
    Set<Approver> approvers;
    ZonedDateTime timestamp;
    DecisionState decisionState;
    UUID uuid;

    @Override
    public String toString() {
        return "LoanApprovalRequest{" +
                "customerId='" + customerId + '\'' +
                ", loanAmount=" + loanAmount +
                ", uuid=" + uuid +
                // ", approvers=[" + approvers.stream().map(Approver::getName).collect(Collectors.joining(",")) + "]" +
                // ", timestamp=" + timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:nnnnnnnnn").withZone(ZoneId.of("UTC"))) +
                ", decisionState=" + decisionState +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoanApprovalRequest that = (LoanApprovalRequest) o;

        if (decisionState == PENDING) {
            return Objects.equals(customerId, that.customerId);
        } else {
            return Objects.equals(customerId, that.customerId) &&
                    Objects.equals(loanAmount, that.loanAmount) &&
                    Objects.equals(approvers, that.approvers) &&
                    Objects.equals(timestamp, that.timestamp) &&
                    decisionState == that.decisionState &&
                    Objects.equals(uuid, that.uuid);
        }
    }

    @Override
    public int hashCode() {
        if (decisionState == PENDING) {
            return Objects.hash(customerId);
        } else {
            return Objects.hash(customerId, loanAmount, approvers, timestamp, decisionState, uuid);
        }
    }

}
