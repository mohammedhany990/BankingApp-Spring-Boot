package com.bankingApp.bankingApp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.bankingApp.bankingApp.dto.EmailDetails;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String sender;

    @Autowired
    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendEmailAlert(EmailDetails emailDetails) {
        try {
            if (emailDetails == null) {
                logger.error("EmailDetails is null. Cannot send email.");
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(sender);
            message.setTo(emailDetails.getTo());
            message.setSubject(emailDetails.getSubject());
            message.setText(emailDetails.getMessage());

            mailSender.send(message);
            logger.info("Email sent successfully to {}", emailDetails.getTo());

        } catch (MailException e) {
            logger.error("Failed to send email to {}. Error: {}", emailDetails.getTo(), e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error while sending email. Error: {}", e.getMessage(), e);
        }
    }

    @Override
    public void sendEmailWithAttachment(EmailDetails emailDetails, String attachmentPath) {
        Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);
        try {
            if (emailDetails == null) {
                log.error("EmailDetails is null. Cannot send email.");
                return;
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true); 

            helper.setFrom(sender);
            helper.setTo(emailDetails.getTo());
            helper.setSubject(emailDetails.getSubject());
            helper.setText(emailDetails.getMessage());

            FileSystemResource file = new FileSystemResource(attachmentPath);
            helper.addAttachment(file.getFilename(), file);

            mailSender.send(message);
            log.info("Email with attachment sent successfully to {}", emailDetails.getTo());

        } catch (MailException e) {
            log.error("Failed to send email to {}. Error: {}", emailDetails.getTo(), e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error while sending email. Error: {}", e.getMessage(), e);
        }
    }
}
