package com.example.loans.controller;

import com.example.loans.dto.DecisionDto;
import com.example.loans.dto.LoanApprovalRequestDto;
import com.example.loans.exception.ErrorHandlingControllerAdvice;
import com.example.loans.service.LoanService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;

import static com.example.loans.model.DecisionState.APPROVED;
import static com.example.loans.model.DecisionState.DECLINED;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class LoanControllerTest {

    private MockMvc mvc;

    @Mock
    private LoanService loanService;

    @InjectMocks
    LoanController loanController;

    private static ObjectWriter ow;

    @BeforeAll
    public static void setup() {
        ObjectMapper mapper = new ObjectMapper();
        ow = mapper.writer().withDefaultPrettyPrinter();
    }

    @BeforeEach
    public void each() {
        mvc = MockMvcBuilders.standaloneSetup(loanController)
                .setControllerAdvice(new ErrorHandlingControllerAdvice())
                .build();
    }

    @Test
    public void whenLoanApprovalRequestWithInvalidCustomerIdAndLoanAmountAndEmptyApproverList_thenReturnsStatus400() throws Exception {
        LoanApprovalRequestDto input = LoanApprovalRequestDto.builder()
                .customerId("XX-XXXX-XX")
                .loanAmount(BigDecimal.ZERO)
                .approvers(Collections.emptyList())
                .build();

        String body = ow.writeValueAsString(input);

        mvc.perform(post("/api/loans/approval-request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void whenLoanApprovalRequestIsValid_thenReturnsStatus201() throws Exception {
        LoanApprovalRequestDto input = LoanApprovalRequestDto.builder()
                .customerId("XX-XXXX-X5X")
                .loanAmount(BigDecimal.valueOf(123.45))
                .approvers(Arrays.asList("Senior Approver"))
                .build();

        String body = ow.writeValueAsString(input);

        mvc.perform(post("/api/loans/approval-request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .characterEncoding(StandardCharsets.UTF_8.name()))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    public void whenMakingValidDecision_then201() throws Exception {
        DecisionDto dto = DecisionDto.builder()
                .customerId("XX-XXXX-X7X")
                .approverUsername("Uncle Bob")
                .state(APPROVED.name())
                .build();
        String body = ow.writeValueAsString(dto);

        mvc.perform(post("/api/loans/decision")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .characterEncoding(StandardCharsets.UTF_8.name()))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Loan APPROVED for clientId: XX-XXXX-X7X"));
    }

    @Test
    public void whenMakingInvalidDecision_then400() throws Exception {
        DecisionDto dto = DecisionDto.builder()
                .customerId("XX-XX-X7X")
                .approverUsername("Uncle Bob")
                .state(APPROVED.name())
                .build();
        String body = ow.writeValueAsString(dto);

        MvcResult response = mvc.perform(post("/api/loans/decision")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .characterEncoding(StandardCharsets.UTF_8.name()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();

        String result = response.getResponse().getContentAsString();

        assertTrue(result.contains("MethodArgumentNotValidException occurred"));
        assertTrue(result.contains("customerId, must match"));
    }

    @Test
    public void whenMakingValidDeclineDecision_then200() throws Exception {
        DecisionDto dto = DecisionDto.builder()
                .customerId("XX-XXXX-X7X")
                .approverUsername("Uncle Bob")
                .state(DECLINED.name())
                .build();
        String body = ow.writeValueAsString(dto);

        mvc.perform(post("/api/loans/decision")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .characterEncoding(StandardCharsets.UTF_8.name()))
                .andDo(print())
                .andExpect(jsonPath("$.message").value("Loan DECLINED for clientId: XX-XXXX-X7X"));
    }

}
