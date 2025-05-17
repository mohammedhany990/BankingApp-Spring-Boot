package com.bankingApp.bankingApp.service;

import com.bankingApp.bankingApp.dto.TransactionDto;
import com.bankingApp.bankingApp.entity.Transaction;

public interface TransactionService {
    void saveTransaction(TransactionDto transactionDto);
}
