package com.bankingApp.bankingApp.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bankingApp.bankingApp.dto.AccountInfo;
import com.bankingApp.bankingApp.dto.BankResponse;
import com.bankingApp.bankingApp.dto.DepositWithdrawRequest;
import com.bankingApp.bankingApp.dto.EmailDetails;
import com.bankingApp.bankingApp.dto.TransactionDto;
import com.bankingApp.bankingApp.dto.TransferRequest;
import com.bankingApp.bankingApp.entity.Transaction;
import com.bankingApp.bankingApp.entity.User;
import com.bankingApp.bankingApp.repository.TransactionRepository;
import com.bankingApp.bankingApp.repository.UserRepository;
import com.bankingApp.bankingApp.utils.AccountUtils;

import io.swagger.v3.oas.annotations.servers.Server;
import jakarta.transaction.Transactional;

@Service
public class TransactionServiceImpl implements TransactionService {

    private TransactionRepository transactionRepository;

    private UserRepository userRepository;
    private EmailService emailService;

    @Autowired
    public TransactionServiceImpl(TransactionRepository transactionRepository, UserRepository userRepository,
            EmailService emailService) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Override
    public void saveTransaction(TransactionDto transactionDto) {
        Transaction transaction = Transaction.builder()
                .accountNumber(transactionDto.getAccountNumber())
                .transactionType(transactionDto.getTransactionType())
                .amount(transactionDto.getAmount())
                .transactionDate(transactionDto.getTransactionDate())
                .status("SUCCESS")
                .build();

        transactionRepository.save(transaction);
    }

    @Override
    public BankResponse deposit(DepositWithdrawRequest depositWithdrawRequest) {
        boolean accountExists = userRepository.existsByAccountNumber(depositWithdrawRequest.getAccountNumber());
        if (!accountExists) {
            return BankResponse.builder()
                    .statusCode(AccountUtils.ACCOUNT_NOT_EXISTS_CODE)
                    .message(AccountUtils.ACCOUNT_NOT_EXISTS_MESSAGE)
                    .accountInfo(null)
                    .build();
        }
        // Check if the amount is valid
        if (depositWithdrawRequest.getAmount() == null
                || depositWithdrawRequest.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return BankResponse.builder()
                    .statusCode(AccountUtils.INVALID_AMOUNT_CODE)
                    .message(AccountUtils.INVALID_AMOUNT_MESSAGE)
                    .accountInfo(null)
                    .build();
        }
        // Check if the transaction limit is exceeded
        // Assuming the transaction limit is 1,000,000
        if (depositWithdrawRequest.getAmount().compareTo(AccountUtils.TRANSACTION_LIMIT) > 0) {
            return BankResponse.builder()
                    .statusCode(AccountUtils.TRANSACTION_LIMIT_EXCEEDED_CODE)
                    .message(AccountUtils.TRANSACTION_LIMIT_EXCEEDED_MESSAGE)
                    .accountInfo(null)
                    .build();
        }

        User user = userRepository.findByAccountNumber(depositWithdrawRequest.getAccountNumber());

        BigDecimal newBalance = user.getAccountBalance().add(depositWithdrawRequest.getAmount());

        user.setAccountBalance(newBalance);

        userRepository.save(user);

        // Save the transaction details
        TransactionDto transactionDto = TransactionDto.builder()
                .accountNumber(user.getAccountNumber())
                .transactionType("CREDIT")
                .amount(depositWithdrawRequest.getAmount())
                .transactionDate(LocalDateTime.now())
                .build();
        saveTransaction(transactionDto);

        EmailDetails emailDetails = EmailDetails.builder()
                .to(user.getEmail())
                .subject("Credit Transaction Alert")
                .message(
                        "Dear " + user.getFirstName() + " " + user.getLastName() + ",\n\n" +
                                "A credit transaction of "
                                + depositWithdrawRequest.getAmount()
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
    public BankResponse withdraw(DepositWithdrawRequest depositWithdrawRequest) {
        boolean accountExists = userRepository.existsByAccountNumber(depositWithdrawRequest.getAccountNumber());

        // Check if the account exists
        if (!accountExists) {
            return BankResponse.builder()
                    .statusCode(AccountUtils.ACCOUNT_NOT_EXISTS_CODE)
                    .message(AccountUtils.ACCOUNT_NOT_EXISTS_MESSAGE)
                    .accountInfo(null)
                    .build();
        }

        // Check if the amount is valid
        if (depositWithdrawRequest.getAmount() == null
                || depositWithdrawRequest.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return BankResponse.builder()
                    .statusCode(AccountUtils.INVALID_AMOUNT_CODE)
                    .message(AccountUtils.INVALID_AMOUNT_MESSAGE)
                    .accountInfo(null)
                    .build();
        }

        // Check if the transaction limit is exceeded
        if (depositWithdrawRequest.getAmount().compareTo(AccountUtils.TRANSACTION_LIMIT) > 0) {
            return BankResponse.builder()
                    .statusCode(AccountUtils.TRANSACTION_LIMIT_EXCEEDED_CODE)
                    .message(AccountUtils.TRANSACTION_LIMIT_EXCEEDED_MESSAGE)
                    .accountInfo(null)
                    .build();
        }

        User user = userRepository.findByAccountNumber(depositWithdrawRequest.getAccountNumber());

        BigDecimal newBalance = user.getAccountBalance().subtract(depositWithdrawRequest.getAmount());

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
                .amount(depositWithdrawRequest.getAmount())
                .build();
        saveTransaction(transactionDto);

        // Send email alert for debit
        EmailDetails emailDetails = EmailDetails.builder()
                .to(user.getEmail())
                .subject("Debit Transaction Alert")
                .message(
                        "Dear " + user.getFirstName() + " " + user.getLastName() + ",\n\n" +
                                "A debit transaction of "
                                + depositWithdrawRequest.getAmount()
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
        saveTransaction(senderTransaction);

        // Save transaction for receiver (credit)
        TransactionDto receiverTransaction = TransactionDto.builder()
                .accountNumber(receiver.getAccountNumber())
                .transactionType("CREDIT")
                .amount(transferRequest.getAmount())
                .transactionDate(LocalDateTime.now())
                .build();
        saveTransaction(receiverTransaction);

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
