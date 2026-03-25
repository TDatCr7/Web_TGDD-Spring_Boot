package phattrienungdungj2ee.example.ngonguyentiendat.service;

import org.springframework.stereotype.Service;
import phattrienungdungj2ee.example.ngonguyentiendat.config.OtpProperties;
import phattrienungdungj2ee.example.ngonguyentiendat.model.EmailOtpToken;
import phattrienungdungj2ee.example.ngonguyentiendat.model.OtpPurpose;
import phattrienungdungj2ee.example.ngonguyentiendat.repository.EmailOtpTokenRepository;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class OtpServiceImpl implements OtpService {
    private final EmailOtpTokenRepository tokenRepository;
    private final EmailService emailService;
    private final OtpProperties otpProperties;
    private final Random random = new Random();

    public OtpServiceImpl(EmailOtpTokenRepository tokenRepository, EmailService emailService, OtpProperties otpProperties) {
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.otpProperties = otpProperties;
    }

    @Override
    public void sendOtp(String email, OtpPurpose purpose, String purposeLabel) {
        String normalized = normalizeEmail(email);
        EmailOtpToken latest = tokenRepository.findTopByEmailAndPurposeOrderByCreatedAtDesc(normalized, purpose).orElse(null);
        if (latest != null && latest.getCreatedAt() != null && latest.getCreatedAt().plusSeconds(otpProperties.getResendSeconds()).isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Bạn vừa gửi OTP. Vui lòng chờ thêm trước khi gửi lại.");
        }
        EmailOtpToken token = new EmailOtpToken();
        token.setEmail(normalized);
        token.setPurpose(purpose);
        token.setCode(generateCode(otpProperties.getLength()));
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusMinutes(otpProperties.getExpiryMinutes()));
        tokenRepository.save(token);
        emailService.sendOtpEmail(normalized, "Mã OTP xác thực", token.getCode(), purposeLabel, otpProperties.getExpiryMinutes());
    }

    @Override
    public boolean verifyOtp(String email, OtpPurpose purpose, String code) {
        String normalized = normalizeEmail(email);
        EmailOtpToken token = tokenRepository.findTopByEmailAndPurposeOrderByCreatedAtDesc(normalized, purpose).orElse(null);
        if (token == null || token.isUsed() || token.isExpired()) {
            return false;
        }
        if (code == null || !token.getCode().equals(code.trim())) {
            return false;
        }
        token.setUsedAt(LocalDateTime.now());
        tokenRepository.save(token);
        return true;
    }

    private String generateCode(int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append(random.nextInt(10));
        }
        return builder.toString();
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
