package com.bankingApp.bankingApp.service;

import com.bankingApp.bankingApp.dto.BankResponse;
import com.bankingApp.bankingApp.dto.CreditDebitRequest;
import com.bankingApp.bankingApp.dto.EnquiryRequest;
import com.bankingApp.bankingApp.dto.LoginDto;
import com.bankingApp.bankingApp.dto.LoginResponse;

import com.bankingApp.bankingApp.dto.TransferRequest;
import com.bankingApp.bankingApp.dto.UserRequest;

public interface UserService {
    BankResponse createAccount(UserRequest userRequest);
    BankResponse balanceEnquiry(EnquiryRequest enquiryRequest);
    String nameEnquiry(EnquiryRequest enquiryRequest);

    BankResponse creditAccount(CreditDebitRequest creditDebitRequest);
    BankResponse debitAccount(CreditDebitRequest creditDebitRequest);
    BankResponse transfer(TransferRequest  transferRequest);
    LoginResponse login(LoginDto loginDto);
    
}
