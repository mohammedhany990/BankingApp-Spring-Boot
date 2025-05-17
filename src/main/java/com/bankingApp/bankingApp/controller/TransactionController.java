package com.bankingApp.bankingApp.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bankingApp.bankingApp.entity.Transaction;
import com.bankingApp.bankingApp.service.BankStatement;
import com.bankingApp.bankingApp.service.TransactionService;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final BankStatement bankStatement;

    public TransactionController(BankStatement bankStatement) {
        this.bankStatement = bankStatement;
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
