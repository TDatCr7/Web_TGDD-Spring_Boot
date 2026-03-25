package phattrienungdungj2ee.example.ngonguyentiendat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path imageDir = Paths.get("src/main/resources/static/images")
                .toAbsolutePath()
                .normalize();

        String imageLocation = imageDir.toUri().toString();

        registry.addResourceHandler("/images/**")
                .addResourceLocations(imageLocation, "classpath:/static/images/")
                .setCachePeriod(0);
    }
}