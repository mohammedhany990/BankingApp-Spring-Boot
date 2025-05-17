package com.bankingApp.bankingApp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bankingApp.bankingApp.dto.TransactionDto;
import com.bankingApp.bankingApp.entity.Transaction;
import com.bankingApp.bankingApp.repository.TransactionRepository;

import io.swagger.v3.oas.annotations.servers.Server;

@Service
public class TransactionServiceImpl implements TransactionService {

    private TransactionRepository transactionRepository;

    @Autowired
    public TransactionServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public void saveTransaction(TransactionDto transactionDto) {
       Transaction transaction = Transaction.builder()
                .accountNumber(transactionDto.getAccountNumber())
                .transactionType(transactionDto.getTransactionType())
                .amount(transactionDto.getAmount())
                .transactionDate(transactionDto.getTransactionDate())
                .status("SUCCESS")
                .build();


        transactionRepository.save(transaction);
    }

}
