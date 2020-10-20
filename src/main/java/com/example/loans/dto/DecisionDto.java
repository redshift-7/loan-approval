package com.example.loans.dto;

import com.example.loans.model.DecisionState;
import com.example.loans.validation.ValueOfEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DecisionDto {

    @NotNull
    @Pattern(regexp = "^[a-zA-Z0-9]{2}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{3}$")
    String customerId;

    @NotEmpty
    String approverUsername;

    @ValueOfEnum(enumClass = DecisionState.class)
    String state;
}
