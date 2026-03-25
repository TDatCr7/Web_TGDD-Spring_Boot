package phattrienungdungj2ee.example.ngonguyentiendat.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.servlet.autoconfigure.MultipartProperties;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class MultipartDebugConfig {

    private final MultipartProperties multipartProperties;

    public MultipartDebugConfig(MultipartProperties multipartProperties) {
        this.multipartProperties = multipartProperties;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logMultipartConfig() {
        System.out.println("=== MULTIPART CONFIG ===");
        System.out.println("max-file-size = " + multipartProperties.getMaxFileSize());
        System.out.println("max-request-size = " + multipartProperties.getMaxRequestSize());
        System.out.println("========================");
    }
}