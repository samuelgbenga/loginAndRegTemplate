package ng.samuel.regnlogintemplate.service;

import jakarta.mail.MessagingException;
import ng.samuel.regnlogintemplate.payload.request.EmailDetails;

public interface EmailService {

    void mimeMailMessage(EmailDetails emailDetails) throws MessagingException;
}
