package phattrienungdungj2ee.example.ngonguyentiendat.service;

public interface EmailService {
    void sendOtpEmail(String to, String subject, String otpCode, String purposeLabel, int expiryMinutes);
}
