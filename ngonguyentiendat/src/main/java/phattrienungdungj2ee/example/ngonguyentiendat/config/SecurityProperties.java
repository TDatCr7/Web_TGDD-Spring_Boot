package phattrienungdungj2ee.example.ngonguyentiendat.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {
    private String defaultAdminEmail = "admin@tgdd.local";
    private String defaultAdminPassword = "Admin@123";
    private String defaultManagerEmail = "manager@tgdd.local";
    private String defaultManagerPassword = "Manager@123";
    private String defaultUserEmail = "user@tgdd.local";
    private String defaultUserPassword = "User@123";

    public String getDefaultAdminEmail() { return defaultAdminEmail; }
    public void setDefaultAdminEmail(String defaultAdminEmail) { this.defaultAdminEmail = defaultAdminEmail; }
    public String getDefaultAdminPassword() { return defaultAdminPassword; }
    public void setDefaultAdminPassword(String defaultAdminPassword) { this.defaultAdminPassword = defaultAdminPassword; }
    public String getDefaultManagerEmail() { return defaultManagerEmail; }
    public void setDefaultManagerEmail(String defaultManagerEmail) { this.defaultManagerEmail = defaultManagerEmail; }
    public String getDefaultManagerPassword() { return defaultManagerPassword; }
    public void setDefaultManagerPassword(String defaultManagerPassword) { this.defaultManagerPassword = defaultManagerPassword; }
    public String getDefaultUserEmail() { return defaultUserEmail; }
    public void setDefaultUserEmail(String defaultUserEmail) { this.defaultUserEmail = defaultUserEmail; }
    public String getDefaultUserPassword() { return defaultUserPassword; }
    public void setDefaultUserPassword(String defaultUserPassword) { this.defaultUserPassword = defaultUserPassword; }
}
