package com.bankingApp.bankingApp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {

    private String email;
    private String role;
    private String message;
    private String statusCode;
    private String token;
    private long expiresIn;
}
