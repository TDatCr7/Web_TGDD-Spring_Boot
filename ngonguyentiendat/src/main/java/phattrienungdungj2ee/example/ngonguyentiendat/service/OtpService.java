package phattrienungdungj2ee.example.ngonguyentiendat.service;

import phattrienungdungj2ee.example.ngonguyentiendat.model.OtpPurpose;

public interface OtpService {
    void sendOtp(String email, OtpPurpose purpose, String purposeLabel);
    boolean verifyOtp(String email, OtpPurpose purpose, String code);
}
