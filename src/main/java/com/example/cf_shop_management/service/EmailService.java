package com.example.cf_shop_management.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    public void sendResetPasswordEmail(String toEmail, String token) {
        String resetLink = frontendUrl + "/reset-password?token=" + token;
        String subject = "CoffeeHouse - Yêu cầu đặt lại mật khẩu";

        String htmlContent = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 24px; border: 1px solid #e7e5e4; border-radius: 12px;">
                    <h2 style="color: #92400e; margin-top: 0;">CoffeeHouse Management</h2>
                    <p style="color: #44403c; font-size: 15px;">Xin chào,</p>
                    <p style="color: #44403c; font-size: 15px;">
                        Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản liên kết với email <strong>%s</strong>.
                    </p>
                    <p style="color: #44403c; font-size: 15px;">
                        Nhấn vào nút bên dưới để thiết lập mật khẩu mới (liên kết có hiệu lực trong <strong>15 phút</strong>):
                    </p>
                    <div style="text-align: center; margin: 28px 0;">
                        <a href="%s" style="background-color: #b45309; color: #ffffff; padding: 12px 24px; text-decoration: none; border-radius: 8px; font-weight: bold; font-size: 15px; display: inline-block;">
                            Đặt lại mật khẩu
                        </a>
                    </div>
                    <p style="color: #78716c; font-size: 13px;">
                        Hoặc sao chép mã xác nhận sau nếu không bấm được nút:<br/>
                        <code style="background: #f5f5f4; padding: 4px 8px; border-radius: 4px; color: #1c1917;">%s</code>
                    </p>
                    <hr style="border: none; border-top: 1px solid #e7e5e4; margin: 20px 0;" />
                    <p style="color: #a8a29e; font-size: 12px; margin-bottom: 0;">
                        Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.
                    </p>
                </div>
                """.formatted(toEmail, resetLink, token);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "CoffeeHouse Support");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Đã gửi email đặt lại mật khẩu tới: {}", toEmail);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Lỗi khi gửi email tới {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Không thể gửi email đặt lại mật khẩu. Vui lòng thử lại sau.");
        }
    }
}