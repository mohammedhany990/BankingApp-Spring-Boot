package com.bankingApp.bankingApp.service;

import java.time.LocalDateTime;
import java.util.List;

import com.bankingApp.bankingApp.entity.Transaction;

public interface BankStatement {

    public List<Transaction> generateStatement(String accountNumber, String startDate, String endDate);

   

}
