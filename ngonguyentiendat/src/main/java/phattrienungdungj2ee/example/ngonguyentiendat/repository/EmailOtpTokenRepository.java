package phattrienungdungj2ee.example.ngonguyentiendat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import phattrienungdungj2ee.example.ngonguyentiendat.model.EmailOtpToken;
import phattrienungdungj2ee.example.ngonguyentiendat.model.OtpPurpose;

import java.util.Optional;

public interface EmailOtpTokenRepository extends JpaRepository<EmailOtpToken, Long> {
    Optional<EmailOtpToken> findTopByEmailAndPurposeOrderByCreatedAtDesc(String email, OtpPurpose purpose);
}
