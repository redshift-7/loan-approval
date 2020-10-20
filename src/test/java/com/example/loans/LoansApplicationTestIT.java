package com.example.loans;

import com.example.loans.dto.DecisionDto;
import com.example.loans.dto.LoanApprovalRequestDto;
import com.example.loans.dto.ResponseDto;
import com.example.loans.exception.ApiError;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.util.Collections;

import static com.example.loans.model.DecisionState.APPROVED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class LoansApplicationTestIT {

    @Autowired
    private WebTestClient webTestClient;

    @LocalServerPort
    int localServerPort;

    private static ObjectWriter ow;

    @BeforeAll
    public static void setup() {
        ObjectMapper mapper = new ObjectMapper();
        ow = mapper.writer().withDefaultPrettyPrinter();
    }

    @Test
    void contextLoads() {
    }

    @Test
    public void givenNonExistentClientIdWithPendingState_whenMakingDecision_expectBusinessRuleExceptionMappedToBadRequest() throws JsonProcessingException {
        DecisionDto decisionDto = DecisionDto.builder()
                .customerId("XX-XXXX-X3X")
                .approverUsername("Uncle Bob")
                .state(APPROVED.name())
                .build();

        System.out.println(ow.writeValueAsString(decisionDto));

        webTestClient.post()
                .uri("/api/loans/decision")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(decisionDto)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ApiError.class)
                .consumeWith(result -> {
                    assertThat(result.getResponseBody()).isNotNull();
                    assertEquals("Decision cannot be saved. There is no related ApprovalRequest with customerId: XX-XXXX-X3X", result.getResponseBody().getErrors());
                });
    }

    @Test
    public void whenAddingLoanApprovalRequestAndAddingApprovedDecision_expectSuccess() {
        LoanApprovalRequestDto approvalRequestDto = LoanApprovalRequestDto.builder()
                .customerId("XX-XXXX-X7X")
                .loanAmount(BigDecimal.valueOf(123.45))
                .approvers(Collections.singletonList("Senior Approver"))
                .build();

        webTestClient.post()
                .uri("/api/loans/approval-request")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(approvalRequestDto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ResponseDto.class)
                .consumeWith(result -> {
                    assertThat(result.getResponseBody()).isNotNull();
                    assertEquals("Approval request successfully created for client: XX-XXXX-X7X", result.getResponseBody().getMessage());
                });
    }
}
