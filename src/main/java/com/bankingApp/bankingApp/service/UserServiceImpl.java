package com.bankingApp.bankingApp.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.bankingApp.bankingApp.configuration.JwtTokenProvider;
import com.bankingApp.bankingApp.dto.AccountInfo;
import com.bankingApp.bankingApp.dto.BankResponse;
import com.bankingApp.bankingApp.dto.ChangePasswordRequest;
import com.bankingApp.bankingApp.dto.EmailDetails;
import com.bankingApp.bankingApp.dto.EnquiryRequest;
import com.bankingApp.bankingApp.dto.LoginDto;
import com.bankingApp.bankingApp.dto.LoginResponse;

import com.bankingApp.bankingApp.dto.UserRequest;
import com.bankingApp.bankingApp.entity.Role;
import com.bankingApp.bankingApp.entity.User;
import com.bankingApp.bankingApp.repository.UserRepository;
import com.bankingApp.bankingApp.utils.AccountUtils;

import jakarta.transaction.Transactional;

@Service
public class UserServiceImpl implements UserService {

        public UserServiceImpl() {
        }

        private UserRepository userRepository;
        private EmailServiceImpl emailService;
        private TransactionServiceImpl transactionService;
        private BCryptPasswordEncoder passwordEncoder;
        private AuthenticationManager authenticationManager;
        private JwtTokenProvider jwtTokenProvider;

        @Autowired
        public UserServiceImpl(UserRepository userRepository,
                        EmailServiceImpl emailService,
                        TransactionServiceImpl transactionService,
                        BCryptPasswordEncoder passwordEncoder,
                        AuthenticationManager authenticationManager,
                        JwtTokenProvider jwtTokenProvider) {

                this.jwtTokenProvider = jwtTokenProvider;
                this.userRepository = userRepository;
                this.emailService = emailService;
                this.transactionService = transactionService;
                this.passwordEncoder = passwordEncoder;
                this.authenticationManager = authenticationManager;
        }

        @Override
        public LoginResponse login(LoginDto loginDto) {
                Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                loginDto.getEmail(),
                                                loginDto.getPassword()));

                if (authentication.isAuthenticated()) {
                        User user = userRepository.findByEmail(loginDto.getEmail())
                                        .orElseThrow(() -> new UsernameNotFoundException("User not found"));

                        String token = jwtTokenProvider.generateToken(authentication);

                        return LoginResponse.builder()
                                        .email(user.getEmail())
                                        .role(user.getRole().toString())
                                        .message(AccountUtils.LOGIN_SUCCESS_MESSAGE)
                                        .statusCode(AccountUtils.LOGIN_SUCCESS_CODE)
                                        .token(token)
                                        .expiresIn(jwtTokenProvider.getExpirationMs())
                                        .build();
                }

                return LoginResponse.builder()
                                .statusCode(AccountUtils.LOGIN_FAILED_CODE)
                                .message(AccountUtils.LOGIN_FAILED_MESSAGE)
                                .build();
        }

        @Override
        public BankResponse createAccount(UserRequest userRequest) {
                if (userRepository.existsByEmail(userRequest.getEmail())) {
                        return BankResponse.builder()
                                        .statusCode(AccountUtils.ACCOUNT_EXISTS_CODE)
                                        .message(AccountUtils.ACCOUNT_EXISTS_MESSAGE)
                                        .accountInfo(null)
                                        .build();
                }
                User newUser = User.builder()
                                .firstName(userRequest.getFirstName())
                                .lastName(userRequest.getLastName())
                                .otherName(userRequest.getOtherName())
                                .gender(userRequest.getGender())
                                .address(userRequest.getAddress())
                                .email(userRequest.getEmail())
                                .stateOfOrigin(userRequest.getStateOfOrigin())
                                .accountNumber(AccountUtils.generateAccountNumber())
                                .accountBalance(BigDecimal.ZERO)
                                .phoneNumber(userRequest.getPhoneNumber())
                                .password(passwordEncoder.encode(userRequest.getPassword()))
                                .status("ACTIVE")
                                .role(Role.valueOf("ROLE_ADMIN"))
                                .alternativePhoneNumber(userRequest.getAlternativePhoneNumber())
                                .build();

                User savedUser = userRepository.save(newUser);
                EmailDetails emailDetails = EmailDetails.builder()
                                .to(savedUser.getEmail())
                                .subject("Account Creation Successful!")
                                .message(
                                                "Dear " + savedUser.getFirstName() + " " + savedUser.getLastName()
                                                                + ",\n\n" +
                                                                "We’re excited to let you know that your account has been successfully created.\n\n"
                                                                +
                                                                "Your Account Number: " + savedUser.getAccountNumber()
                                                                + "\n\n" +
                                                                "Thank you for choosing our services — we're thrilled to have you onboard.\n\n"
                                                                +
                                                                "Warm regards,\n" +
                                                                "The Banking App Team")
                                .build();

                emailService.sendEmailAlert(emailDetails);

                return BankResponse.builder()
                                .statusCode(AccountUtils.ACCOUNT_CREATION_SUCCESS_CODE)
                                .message(AccountUtils.ACCOUNT_CREATION_SUCCESS_MESSAGE)
                                .accountInfo(AccountInfo.builder()
                                                .accountBalance(savedUser.getAccountBalance())
                                                .accountName(savedUser.getFirstName() + " " + savedUser.getLastName())
                                                .accountNumber(savedUser.getAccountNumber())
                                                .build())
                                .build();

        }

        @Override
        public BankResponse balanceEnquiry(EnquiryRequest enquiryRequest) {
                boolean accountExists = userRepository.existsByAccountNumber(enquiryRequest.getAccountNumber());
                if (!accountExists) {
                        return BankResponse.builder()
                                        .statusCode(AccountUtils.ACCOUNT_NOT_EXISTS_CODE)
                                        .message(AccountUtils.ACCOUNT_NOT_EXISTS_MESSAGE)
                                        .accountInfo(null)
                                        .build();
                }
                User user = userRepository.findByAccountNumber(enquiryRequest.getAccountNumber());
                return BankResponse.builder()
                                .statusCode(AccountUtils.ACCOUNT_FOUND_CODE)
                                .message(AccountUtils.ACCOUNT_FOUND_MESSAGE)
                                .accountInfo(
                                                AccountInfo
                                                                .builder()
                                                                .accountBalance(user.getAccountBalance())
                                                                .accountName(user.getFirstName() + " "
                                                                                + user.getLastName())
                                                                .accountNumber(user.getAccountNumber())
                                                                .build())
                                .build();
        }

        @Override
        public String nameEnquiry(EnquiryRequest enquiryRequest) {
                boolean accountExists = userRepository.existsByAccountNumber(enquiryRequest.getAccountNumber());
                if (!accountExists) {
                        return AccountUtils.ACCOUNT_NOT_EXISTS_MESSAGE;
                }
                User user = userRepository.findByAccountNumber(enquiryRequest.getAccountNumber());
                return user.getFirstName() + " " + user.getLastName();
        }

        public String changePassword(String username, ChangePasswordRequest changePasswordRequest) {
                User user = userRepository.findByEmail(username)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

                if (!passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPassword())) {
                        throw new IllegalArgumentException("Old password is incorrect");
                }

                user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
                userRepository.save(user);
                return "Password changed successfully";
        }

}
