package com.bankingApp.bankingApp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserRequest {
    private String firstName;
    private String lastName;
    private String otherName;
    private String gender;
    private String address;
    private String password;

    private String stateOfOrigin;
    
    private String email;
    private String phoneNumber;
    private String alternativePhoneNumber;
}
