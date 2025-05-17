package com.bankingApp.bankingApp.service;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bankingApp.bankingApp.dto.EmailDetails;
import com.bankingApp.bankingApp.entity.Transaction;
import com.bankingApp.bankingApp.entity.User;
import com.bankingApp.bankingApp.repository.TransactionRepository;
import com.bankingApp.bankingApp.repository.UserRepository;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BankStatementImpl implements BankStatement {

    private final TransactionRepository transactionRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;

    // Directory where PDFs will be saved (folder only, no filename)
    private final String FILE_DIRECTORY = "E:\\CodePractice\\Java\\bankingApp\\src\\main\\resources\\pdf";

    @Autowired
    public BankStatementImpl(TransactionRepository transactionRepository, UserRepository userRepository,
            EmailService emailService) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Override
    public List<Transaction> generateStatement(String accountNumber, String startDate, String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(LocalTime.MAX);

        List<Transaction> transactions = transactionRepository
                .findByAccountNumberAndTransactionDateBetween(accountNumber, startDateTime, endDateTime);

        User user = userRepository.findByAccountNumber(accountNumber);
        // generate PDF and get its path
        String pdfPath = designStatement(transactions, user, startDate, endDate);

        EmailDetails emailDetails = new EmailDetails();
        emailDetails.setTo(user.getEmail());
        emailDetails.setSubject("Your Bank Statement");
        emailDetails.setMessage("Dear " + user.getFirstName() + ",\n\nPlease find attached your bank statement from "
                + startDate + " to " + endDate + ".\n\nRegards,\nBanking App Team");

        emailService.sendEmailWithAttachment(emailDetails, pdfPath);

        return transactions;
    }

    // Returns the path of the generated PDF file
    private String designStatement(List<Transaction> transactions, User user, String startDate, String endDate) {
        // Compose the file name dynamically
        String fileName = user.getAccountNumber() + "_BankStatement.pdf";
        String fullPath = FILE_DIRECTORY + "\\" + fileName;

        try {
            Document document = new Document(PageSize.A4);
            OutputStream os = new FileOutputStream(fullPath);
            PdfWriter.getInstance(document, os);
            document.open();

            // Font config
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.WHITE);
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
            Font normalFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);

            // Bank Header
            PdfPTable bankInfoTable = new PdfPTable(1);
            PdfPCell bankNameCell = new PdfPCell(new Phrase("Banking App", titleFont));
            bankNameCell.setBackgroundColor(BaseColor.BLUE);
            bankNameCell.setBorder(0);
            bankNameCell.setPadding(20f);
            bankNameCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            bankInfoTable.addCell(bankNameCell);

            document.add(bankInfoTable);

            // Customer Info
            PdfPTable customerTable = new PdfPTable(1);
            customerTable.setSpacingBefore(20f);

            PdfPCell customerInfoCell = new PdfPCell(new Phrase(
                    "Statement Date Range: " + startDate + " to " + endDate +
                            "\nAccount Number: " + user.getAccountNumber() +
                            "\nCustomer Name: " + user.getFirstName() + " " + user.getLastName() +
                            "\nAddress: " + user.getAddress(),
                    normalFont));
            customerInfoCell.setBorder(0);
            customerInfoCell.setPadding(10f);

            customerTable.addCell(customerInfoCell);
            document.add(customerTable);

            // Transactions Table
            PdfPTable transactionTable = new PdfPTable(4);
            transactionTable.setSpacingBefore(30f);
            transactionTable.setWidthPercentage(100);
            transactionTable.setWidths(new float[] { 3f, 3f, 2f, 3f });

            // Table headers
            String[] headers = { "Transaction ID", "Date", "Type", "Amount" };
            for (String header : headers) {
                PdfPCell headerCell = new PdfPCell(new Phrase(header, headerFont));
                headerCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                headerCell.setPadding(8f);
                transactionTable.addCell(headerCell);
            }

            // Transaction records
            for (Transaction transaction : transactions) {
                transactionTable
                        .addCell(new PdfPCell(new Phrase(transaction.getTransactionId().toString(), normalFont)));
                transactionTable
                        .addCell(new PdfPCell(new Phrase(transaction.getTransactionDate().toString(), normalFont)));
                transactionTable.addCell(new PdfPCell(new Phrase(transaction.getTransactionType(), normalFont)));
                transactionTable.addCell(new PdfPCell(new Phrase(transaction.getAmount().toString(), normalFont)));
            }

            document.add(transactionTable);
            document.close();
            os.close();

            log.info("PDF Bank statement generated successfully at: {}", fullPath);

        } catch (Exception e) {
            log.error("Error generating PDF statement: ", e);
        }

        return fullPath;
    }
}
