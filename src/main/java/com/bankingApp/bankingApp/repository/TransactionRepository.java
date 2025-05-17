package com.bankingApp.bankingApp.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bankingApp.bankingApp.entity.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
     List<Transaction> findByAccountNumberAndTransactionDateBetween(
        String accountNumber, LocalDateTime startDate, LocalDateTime endDate);
}
