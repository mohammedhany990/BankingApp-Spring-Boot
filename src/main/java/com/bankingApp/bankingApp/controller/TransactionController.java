package com.bankingApp.bankingApp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bankingApp.bankingApp.dto.BankResponse;
import com.bankingApp.bankingApp.dto.DepositWithdrawRequest;
import com.bankingApp.bankingApp.dto.TransferRequest;
import com.bankingApp.bankingApp.entity.Transaction;
import com.bankingApp.bankingApp.service.BankStatement;
import com.bankingApp.bankingApp.service.TransactionService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final BankStatement bankStatement;
    private final TransactionService transactionService;

    @Autowired
    public TransactionController(BankStatement bankStatement, TransactionService transactionService) {
        this.bankStatement = bankStatement;
        this.transactionService = transactionService;
    }

    @PostMapping("/deposit")
    public BankResponse creditAccount(@RequestBody DepositWithdrawRequest creditDebitRequest) {
        return transactionService.deposit(creditDebitRequest);
    }

    @PostMapping("/withdraw")
    public BankResponse withdraw(@RequestBody DepositWithdrawRequest creditDebitRequest) {
        return transactionService.withdraw(creditDebitRequest);
    }

    @PostMapping("/transfer")
    public BankResponse transfer(@RequestBody TransferRequest transferRequest) {
        return transactionService.transfer(transferRequest);
    }

    @GetMapping("/statement")
    public List<Transaction> generateStatement(
            @RequestParam String accountNumber,
            @RequestParam String startDate,
            @RequestParam String endDate) {

        List<Transaction> transactions = bankStatement.generateStatement(accountNumber, startDate, endDate);
        return transactions;
    }
}
