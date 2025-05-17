package com.bankingApp.bankingApp.dto;

import lombok.*;


@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankResponse
{
    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public AccountInfo getAccountInfo() {
        return accountInfo;
    }

    public void setAccountInfo(AccountInfo accountInfo) {
        this.accountInfo = accountInfo;
    }

    private String statusCode;
    private String message;

    private AccountInfo accountInfo;
}
