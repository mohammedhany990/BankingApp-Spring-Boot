package com.bankingApp.bankingApp.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {

    private String accountNumber;

    private String transactionType;

    private BigDecimal amount;

    private String status;  
    
     private LocalDateTime transactionDate;
}
