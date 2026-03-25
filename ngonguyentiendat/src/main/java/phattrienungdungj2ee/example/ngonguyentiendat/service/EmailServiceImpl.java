package phattrienungdungj2ee.example.ngonguyentiendat.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import phattrienungdungj2ee.example.ngonguyentiendat.config.MailProperties;

@Service
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;

    public EmailServiceImpl(JavaMailSender mailSender, MailProperties mailProperties) {
        this.mailSender = mailSender;
        this.mailProperties = mailProperties;
    }

    @Override
    public void sendOtpEmail(String to, String subject, String otpCode, String purposeLabel, int expiryMinutes) {
        SimpleMailMessage message = new SimpleMailMessage();
        if (mailProperties.getFrom() != null && !mailProperties.getFrom().isBlank()) {
            message.setFrom(mailProperties.getFrom());
        }
        message.setTo(to);
        message.setSubject(subject);
        message.setText("Xin chào, " + "Mã OTP để " + purposeLabel + " là: " + otpCode + " " + "Mã có hiệu lực trong " + expiryMinutes + " phút. " + "Nếu bạn không thực hiện yêu cầu này, hãy bỏ qua email. " +
                mailProperties.getAppName());
        mailSender.send(message);
    }
}
