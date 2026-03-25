package phattrienungdungj2ee.example.ngonguyentiendat.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "momo")
public class MomoProperties {

    private String partnerCode;
    private String accessKey;
    private String secretKey;
    private String endpoint;
    private String createEndpoint;
    private String queryEndpoint;
    private String redirectUrl;
    private String ipnUrl;
    private String requestType = "captureWallet";
    private String lang = "vi";
    private String storeName;
    private boolean enabled = true;

    public String getPartnerCode() {
        return partnerCode;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getEndpoint() {
        if (createEndpoint != null && !createEndpoint.isBlank()) {
            return createEndpoint;
        }
        return endpoint;
    }

    public String getCreateEndpoint() {
        return createEndpoint;
    }

    public String getQueryEndpoint() { return queryEndpoint; }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public String getIpnUrl() {
        return ipnUrl;
    }

    public String getRequestType() {
        return requestType;
    }

    public String getLang() {
        return lang;
    }

    public String getStoreName() {
        return storeName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setPartnerCode(String partnerCode) {
        this.partnerCode = partnerCode;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public void setCreateEndpoint(String createEndpoint) {
        this.createEndpoint = createEndpoint;
    }

    public void setQueryEndpoint(String queryEndpoint) { this.queryEndpoint = queryEndpoint; }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public void setIpnUrl(String ipnUrl) {
        this.ipnUrl = ipnUrl;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
