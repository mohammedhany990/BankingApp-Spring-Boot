package com.bankingApp.bankingApp.service;

import com.bankingApp.bankingApp.dto.BankResponse;
import com.bankingApp.bankingApp.dto.ChangePasswordRequest;
import com.bankingApp.bankingApp.dto.EnquiryRequest;
import com.bankingApp.bankingApp.dto.LoginDto;
import com.bankingApp.bankingApp.dto.LoginResponse;

import com.bankingApp.bankingApp.dto.UserRequest;

public interface UserService {
    BankResponse createAccount(UserRequest userRequest);
    BankResponse balanceEnquiry(EnquiryRequest enquiryRequest);
    String nameEnquiry(EnquiryRequest enquiryRequest);
    String changePassword(String username, ChangePasswordRequest changePasswordRequest);

    LoginResponse login(LoginDto loginDto);
    
}
