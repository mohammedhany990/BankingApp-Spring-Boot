package com.bankingApp.bankingApp.utils;

import java.math.BigDecimal;
import java.time.Year;

public class AccountUtils {

    public static final String ACCOUNT_EXISTS_CODE = "001";
    public static final String ACCOUNT_EXISTS_MESSAGE = "This user already has an account!.";

    public static final String ACCOUNT_CREATION_SUCCESS_CODE = "002";
    public static final String ACCOUNT_CREATION_SUCCESS_MESSAGE = "Account has been created successfully!.";

    public static final String ACCOUNT_CREDIT_DEBIT_SUCCESS_CODE = "003";
    public static final String ACCOUNT_CREDIT_DEBIT_SUCCESS_MESSAGE = "Transaction completed successfully!";

    public static final String INSUFFICIENT_FUNDS_CODE = "004";
    public static final String INSUFFICIENT_FUNDS_MESSAGE = "Insufficient funds!";

    public static final String ACCOUNT_NOT_EXISTS_CODE = "005";
    public static final String ACCOUNT_NOT_EXISTS_MESSAGE = "This account does not exist!.";

    public static final String ACCOUNT_FOUND_CODE = "006";
    public static final String ACCOUNT_FOUND_MESSAGE = "This account exists!.";

    public static final String INVALID_AMOUNT_CODE = "007";
    public static final String INVALID_AMOUNT_MESSAGE = "Invalid transaction amount. Amount must be greater than zero!";

    public static final String TRANSACTION_LIMIT_EXCEEDED_CODE = "008";
    public static final String TRANSACTION_LIMIT_EXCEEDED_MESSAGE = "Transaction limit exceeded!";

    public static final String ACCOUNT_TRANSFER_SUCCESS_CODE = "009";
    public static final String ACCOUNT_TRANSFER_SUCCESS_MESSAGE = "Account transfer completed successfully!";

    public static final String LOGIN_SUCCESS_CODE = "200";
    public static final String LOGIN_SUCCESS_MESSAGE = "Login successful!";
    public static final String LOGIN_FAILED_CODE = "401";
    public static final String LOGIN_FAILED_MESSAGE = "Invalid email or password.";

    public static final BigDecimal TRANSACTION_LIMIT = new BigDecimal("1000000");

    public static String generateAccountNumber() {
        Year currentYear = Year.now();
        int min = 100000;
        int max = 999999;

        int random = (int) Math.floor(Math.random() * (max - min + 1) + min);

        String year = String.valueOf(currentYear);
        String randomNumber = String.valueOf(random);

        StringBuilder accountNumber = new StringBuilder();

        return accountNumber.append(year).append(randomNumber).toString();
    }
}
