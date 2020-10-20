package com.example.loans.dto;

import lombok.*;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LoanApprovalRequestDto {

    @NotNull
    @Pattern(regexp = "^[a-zA-Z0-9]{2}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{3}$")
    String customerId;

    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer=3, fraction=2)
    BigDecimal loanAmount;

    @Size(min=1, max=3)
    List<String> approvers;

}
