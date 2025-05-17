package com.bankingApp.bankingApp.dto;

import lombok.*;

import java.math.BigDecimal;


@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AccountInfo {

    private String accountName;
    private BigDecimal accountBalance;
    private String accountNumber;

}
