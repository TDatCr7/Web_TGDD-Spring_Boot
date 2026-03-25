package phattrienungdungj2ee.example.ngonguyentiendat.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.mail")
public class MailProperties {
    private String from;
    private String appName = "TheGioiDiDong Demo";

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }
    public String getAppName() { return appName; }
    public void setAppName(String appName) { this.appName = appName; }
}
