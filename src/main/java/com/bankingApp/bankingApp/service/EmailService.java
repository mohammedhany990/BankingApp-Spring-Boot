package com.bankingApp.bankingApp.service;

import com.bankingApp.bankingApp.dto.EmailDetails;

public interface EmailService {
    void sendEmailAlert(EmailDetails emailDetails);
    void sendEmailWithAttachment(EmailDetails emailDetails, String attachmentPath);

}
