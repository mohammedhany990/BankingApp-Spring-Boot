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
import com.bankingApp.bankingApp.dto.CreditDebitRequest;
import com.bankingApp.bankingApp.dto.EmailDetails;
import com.bankingApp.bankingApp.dto.EnquiryRequest;
import com.bankingApp.bankingApp.dto.LoginDto;
import com.bankingApp.bankingApp.dto.LoginResponse;
import com.bankingApp.bankingApp.dto.TransactionDto;
import com.bankingApp.bankingApp.dto.TransferRequest;
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

        @Override
        public BankResponse creditAccount(CreditDebitRequest creditDebitRequest) {
                boolean accountExists = userRepository.existsByAccountNumber(creditDebitRequest.getAccountNumber());
                if (!accountExists) {
                        return BankResponse.builder()
                                        .statusCode(AccountUtils.ACCOUNT_NOT_EXISTS_CODE)
                                        .message(AccountUtils.ACCOUNT_NOT_EXISTS_MESSAGE)
                                        .accountInfo(null)
                                        .build();
                }
                // Check if the amount is valid
                if (creditDebitRequest.getAmount() == null
                                || creditDebitRequest.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                        return BankResponse.builder()
                                        .statusCode(AccountUtils.INVALID_AMOUNT_CODE)
                                        .message(AccountUtils.INVALID_AMOUNT_MESSAGE)
                                        .accountInfo(null)
                                        .build();
                }
                // Check if the transaction limit is exceeded
                // Assuming the transaction limit is 1,000,000
                if (creditDebitRequest.getAmount().compareTo(AccountUtils.TRANSACTION_LIMIT) > 0) {
                        return BankResponse.builder()
                                        .statusCode(AccountUtils.TRANSACTION_LIMIT_EXCEEDED_CODE)
                                        .message(AccountUtils.TRANSACTION_LIMIT_EXCEEDED_MESSAGE)
                                        .accountInfo(null)
                                        .build();
                }

                User user = userRepository.findByAccountNumber(creditDebitRequest.getAccountNumber());

                BigDecimal newBalance = user.getAccountBalance().add(creditDebitRequest.getAmount());

                user.setAccountBalance(newBalance);

                userRepository.save(user);

                // Save the transaction details
                TransactionDto transactionDto = TransactionDto.builder()
                                .accountNumber(user.getAccountNumber())
                                .transactionType("CREDIT")
                                .amount(creditDebitRequest.getAmount())
                                .transactionDate(LocalDateTime.now())
                                .build();
                transactionService.saveTransaction(transactionDto);

                EmailDetails emailDetails = EmailDetails.builder()
                                .to(user.getEmail())
                                .subject("Credit Transaction Alert")
                                .message(
                                                "Dear " + user.getFirstName() + " " + user.getLastName() + ",\n\n" +
                                                                "A credit transaction of "
                                                                + creditDebitRequest.getAmount()
                                                                + " has been added to your account.\n" +
                                                                "Your new account balance is: " + newBalance + "\n\n" +
                                                                "Thank you for using our services.\n\n" +
                                                                "Best regards,\n" +
                                                                "The Banking App Team")
                                .build();

                emailService.sendEmailAlert(emailDetails);

                return BankResponse.builder()
                                .statusCode(AccountUtils.ACCOUNT_CREDIT_DEBIT_SUCCESS_CODE)
                                .message(AccountUtils.ACCOUNT_CREDIT_DEBIT_SUCCESS_MESSAGE)
                                .accountInfo(
                                                AccountInfo.builder()
                                                                .accountName(user.getFirstName() + " "
                                                                                + user.getLastName())
                                                                .accountBalance(user.getAccountBalance())
                                                                .accountNumber(user.getAccountNumber())
                                                                .build())
                                .build();
        }

        @Override
        public BankResponse debitAccount(CreditDebitRequest creditDebitRequest) {
                boolean accountExists = userRepository.existsByAccountNumber(creditDebitRequest.getAccountNumber());

                // Check if the account exists
                if (!accountExists) {
                        return BankResponse.builder()
                                        .statusCode(AccountUtils.ACCOUNT_NOT_EXISTS_CODE)
                                        .message(AccountUtils.ACCOUNT_NOT_EXISTS_MESSAGE)
                                        .accountInfo(null)
                                        .build();
                }

                // Check if the amount is valid
                if (creditDebitRequest.getAmount() == null
                                || creditDebitRequest.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                        return BankResponse.builder()
                                        .statusCode(AccountUtils.INVALID_AMOUNT_CODE)
                                        .message(AccountUtils.INVALID_AMOUNT_MESSAGE)
                                        .accountInfo(null)
                                        .build();
                }

                // Check if the transaction limit is exceeded
                if (creditDebitRequest.getAmount().compareTo(AccountUtils.TRANSACTION_LIMIT) > 0) {
                        return BankResponse.builder()
                                        .statusCode(AccountUtils.TRANSACTION_LIMIT_EXCEEDED_CODE)
                                        .message(AccountUtils.TRANSACTION_LIMIT_EXCEEDED_MESSAGE)
                                        .accountInfo(null)
                                        .build();
                }

                User user = userRepository.findByAccountNumber(creditDebitRequest.getAccountNumber());

                BigDecimal newBalance = user.getAccountBalance().subtract(creditDebitRequest.getAmount());

                // Check if the new balance is negative
                if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                        return BankResponse.builder()
                                        .statusCode(AccountUtils.INSUFFICIENT_FUNDS_CODE)
                                        .message(AccountUtils.INSUFFICIENT_FUNDS_MESSAGE)
                                        .accountInfo(null)
                                        .build();
                }

                user.setAccountBalance(newBalance);

                userRepository.save(user);

                TransactionDto transactionDto = TransactionDto.builder()
                                .accountNumber(user.getAccountNumber())
                                .transactionType("DEBIT")
                                .transactionDate(LocalDateTime.now())
                                .amount(creditDebitRequest.getAmount())
                                .build();
                transactionService.saveTransaction(transactionDto);

                // Send email alert for debit
                EmailDetails emailDetails = EmailDetails.builder()
                                .to(user.getEmail())
                                .subject("Debit Transaction Alert")
                                .message(
                                                "Dear " + user.getFirstName() + " " + user.getLastName() + ",\n\n" +
                                                                "A debit transaction of "
                                                                + creditDebitRequest.getAmount()
                                                                + " has been made from your account.\n" +
                                                                "Your new account balance is: " + newBalance + "\n\n" +
                                                                "Thank you for using our services.\n\n" +
                                                                "Best regards,\n" +
                                                                "The Banking App Team")
                                .build();
                emailService.sendEmailAlert(emailDetails);

                return BankResponse.builder()
                                .statusCode(AccountUtils.ACCOUNT_CREDIT_DEBIT_SUCCESS_CODE)
                                .message(AccountUtils.ACCOUNT_CREDIT_DEBIT_SUCCESS_MESSAGE)
                                .accountInfo(
                                                AccountInfo.builder()
                                                                .accountName(user.getFirstName() + " "
                                                                                + user.getLastName())
                                                                .accountBalance(user.getAccountBalance())
                                                                .accountNumber(user.getAccountNumber())
                                                                .build())
                                .build();
        }

        @Override
        @Transactional
        public BankResponse transfer(TransferRequest transferRequest) {

                boolean senderAccountExists = userRepository
                                .existsByAccountNumber(transferRequest.getSenderAccountNumber());
                boolean receiverAccountExists = userRepository
                                .existsByAccountNumber(transferRequest.getReceiverAccountNumber());

                if (!senderAccountExists || !receiverAccountExists) {
                        return BankResponse.builder()
                                        .statusCode(AccountUtils.ACCOUNT_NOT_EXISTS_CODE)
                                        .message(AccountUtils.ACCOUNT_NOT_EXISTS_MESSAGE)
                                        .accountInfo(null)
                                        .build();
                }

                // Validate transfer amount
                if (transferRequest.getAmount() == null
                                || transferRequest.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                        return BankResponse.builder()
                                        .statusCode(AccountUtils.INVALID_AMOUNT_CODE)
                                        .message(AccountUtils.INVALID_AMOUNT_MESSAGE)
                                        .accountInfo(null)
                                        .build();
                }

                // Check transaction limit
                if (transferRequest.getAmount().compareTo(AccountUtils.TRANSACTION_LIMIT) > 0) {
                        return BankResponse.builder()
                                        .statusCode(AccountUtils.TRANSACTION_LIMIT_EXCEEDED_CODE)
                                        .message(AccountUtils.TRANSACTION_LIMIT_EXCEEDED_MESSAGE)
                                        .accountInfo(null)
                                        .build();
                }

                // Fetch both users
                User sender = userRepository.findByAccountNumber(transferRequest.getSenderAccountNumber());
                User receiver = userRepository.findByAccountNumber(transferRequest.getReceiverAccountNumber());

                // Check sender has sufficient funds
                if (sender.getAccountBalance().compareTo(transferRequest.getAmount()) < 0) {
                        return BankResponse.builder()
                                        .statusCode(AccountUtils.INSUFFICIENT_FUNDS_CODE)
                                        .message(AccountUtils.INSUFFICIENT_FUNDS_MESSAGE)
                                        .accountInfo(null)
                                        .build();
                }

                // Perform the transfer
                sender.setAccountBalance(sender.getAccountBalance().subtract(transferRequest.getAmount()));
                receiver.setAccountBalance(receiver.getAccountBalance().add(transferRequest.getAmount()));

                userRepository.save(sender);
                userRepository.save(receiver);

                // Save transaction for sender (debit)
                TransactionDto senderTransaction = TransactionDto.builder()
                                .accountNumber(sender.getAccountNumber())
                                .transactionType("DEBIT")
                                .amount(transferRequest.getAmount())
                                .transactionDate(LocalDateTime.now())
                                .build();
                transactionService.saveTransaction(senderTransaction);

                // Save transaction for receiver (credit)
                TransactionDto receiverTransaction = TransactionDto.builder()
                                .accountNumber(receiver.getAccountNumber())
                                .transactionType("CREDIT")
                                .amount(transferRequest.getAmount())
                                .transactionDate(LocalDateTime.now())
                                .build();
                transactionService.saveTransaction(receiverTransaction);

                // Send email notifications to sender and receiver
                EmailDetails senderEmail = EmailDetails.builder()
                                .to(sender.getEmail())
                                .subject("Debit Transaction Alert - Transfer")
                                .message(
                                                "Dear " + sender.getFirstName() + " " + sender.getLastName() + ",\n\n" +
                                                                "A transfer of " + transferRequest.getAmount()
                                                                + " has been debited from your account.\n" +
                                                                "Your new account balance is: "
                                                                + sender.getAccountBalance() + "\n\n" +
                                                                "Thank you for using our services.\n\n" +
                                                                "Best regards,\n" +
                                                                "The Banking App Team")
                                .build();
                emailService.sendEmailAlert(senderEmail);

                EmailDetails receiverEmail = EmailDetails.builder()
                                .to(receiver.getEmail())
                                .subject("Credit Transaction Alert - Transfer")
                                .message(
                                                "Dear " + receiver.getFirstName() + " " + receiver.getLastName()
                                                                + ",\n\n" +
                                                                "A transfer of " + transferRequest.getAmount()
                                                                + " has been credited to your account.\n"
                                                                +
                                                                "Your new account balance is: "
                                                                + receiver.getAccountBalance() + "\n\n" +
                                                                "Thank you for using our services.\n\n" +
                                                                "Best regards,\n" +
                                                                "The Banking App Team")
                                .build();
                emailService.sendEmailAlert(receiverEmail);

                return BankResponse.builder()
                                .statusCode(AccountUtils.ACCOUNT_TRANSFER_SUCCESS_CODE)
                                .message(AccountUtils.ACCOUNT_TRANSFER_SUCCESS_MESSAGE)
                                .accountInfo(AccountInfo.builder()
                                                .accountName(sender.getFirstName() + " " + sender.getLastName())
                                                .accountBalance(sender.getAccountBalance())
                                                .accountNumber(sender.getAccountNumber())
                                                .build())
                                .build();
        }

}
