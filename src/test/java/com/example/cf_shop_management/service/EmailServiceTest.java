package com.example.cf_shop_management.service;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "shop@gmail.com");
        ReflectionTestUtils.setField(emailService, "frontendUrl", "http://localhost:5173");
    }

    @Test
    void sendResetPasswordEmail_ValidInput_SendsHtmlEmail() throws Exception {
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendResetPasswordEmail("customer@gmail.com", "sample-token-123");

        verify(mailSender).send(mimeMessage);
        assertEquals("customer@gmail.com", mimeMessage.getAllRecipients()[0].toString());
        assertEquals("CoffeeHouse - Yêu cầu đặt lại mật khẩu", mimeMessage.getSubject());
    }

    @Test
    void sendResetPasswordEmail_MailSenderThrowsException_ThrowsRuntimeException() {
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MailSendException("SMTP connection failed"))
                .when(mailSender).send(any(MimeMessage.class));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> emailService.sendResetPasswordEmail("customer@gmail.com", "sample-token-123")
        );

        assertEquals("Không thể gửi email đặt lại mật khẩu. Vui lòng thử lại sau.", exception.getMessage());
    }
}