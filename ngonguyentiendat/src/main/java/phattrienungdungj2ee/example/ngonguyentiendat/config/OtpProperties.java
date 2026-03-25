package phattrienungdungj2ee.example.ngonguyentiendat.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.otp")
public class OtpProperties {
    private int expiryMinutes = 5;
    private int resendSeconds = 60;
    private int length = 6;

    public int getExpiryMinutes() { return expiryMinutes; }
    public void setExpiryMinutes(int expiryMinutes) { this.expiryMinutes = expiryMinutes; }
    public int getResendSeconds() { return resendSeconds; }
    public void setResendSeconds(int resendSeconds) { this.resendSeconds = resendSeconds; }
    public int getLength() { return length; }
    public void setLength(int length) { this.length = length; }
}
