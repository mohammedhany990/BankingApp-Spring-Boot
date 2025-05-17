package com.bankingApp.bankingApp.service;

import com.bankingApp.bankingApp.dto.BankResponse;
import com.bankingApp.bankingApp.dto.DepositWithdrawRequest;
import com.bankingApp.bankingApp.dto.TransactionDto;
import com.bankingApp.bankingApp.dto.TransferRequest;
import com.bankingApp.bankingApp.entity.Transaction;

public interface TransactionService {
    void saveTransaction(TransactionDto transactionDto);

    BankResponse deposit(DepositWithdrawRequest creditDebitRequest);

    BankResponse withdraw(DepositWithdrawRequest creditDebitRequest);

    BankResponse transfer(TransferRequest transferRequest);
}
