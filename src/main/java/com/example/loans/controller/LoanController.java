package com.example.loans.controller;

import com.example.loans.dto.DecisionDto;
import com.example.loans.dto.LoanApprovalRequestDto;
import com.example.loans.dto.ResponseDto;
import com.example.loans.model.Statistics;
import com.example.loans.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    @Autowired
    LoanService loanService;

    @PostMapping("/approval-request")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseDto createApprovalRequest(@Validated @RequestBody LoanApprovalRequestDto dto) {
        loanService.saveApproval(dto);
        return new ResponseDto("Approval request successfully created for client: " + dto.getCustomerId());
    }

    @PostMapping("/decision")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseDto makeDecision(@Valid @RequestBody DecisionDto dto) {
        loanService.makeDecision(dto);
        return new ResponseDto("Loan " + dto.getState() + " for clientId: "  + dto.getCustomerId());
    }

    @GetMapping("/statistics")
    @ResponseStatus(HttpStatus.OK)
    public Statistics getStatistics() {
        return loanService.getStatistics();
    }

}
