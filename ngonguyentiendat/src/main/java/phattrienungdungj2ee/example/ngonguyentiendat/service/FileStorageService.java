package phattrienungdungj2ee.example.ngonguyentiendat.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Base64;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path baseUploadPath = Paths.get("src/main/resources/static/images")
            .toAbsolutePath()
            .normalize();

    public String saveProductFile(MultipartFile file) throws IOException {
        return saveFileToFolder(file, "product");
    }

    public String saveCategoryFile(MultipartFile file) throws IOException {
        return saveFileToFolder(file, "category");
    }

    public String saveProductBase64(String dataUrl) throws IOException {
        return saveBase64ToFolder(dataUrl, "product");
    }

    private String saveFileToFolder(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        Path uploadPath = baseUploadPath.resolve(folder);
        Files.createDirectories(uploadPath);

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = "";

        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalFilename.substring(dotIndex);
        }

        if (extension.isBlank()) {
            extension = ".png";
        }

        String newFileName = UUID.randomUUID() + extension;
        Path target = uploadPath.resolve(newFileName);

        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        return "/images/" + folder + "/" + newFileName;
    }

    private String saveBase64ToFolder(String dataUrl, String folder) throws IOException {
        if (dataUrl == null || dataUrl.isBlank()) {
            return null;
        }

        if (!dataUrl.startsWith("data:image/")) {
            throw new IOException("Dữ liệu ảnh không hợp lệ.");
        }

        int commaIndex = dataUrl.indexOf(',');
        if (commaIndex < 0) {
            throw new IOException("Dữ liệu ảnh base64 không hợp lệ.");
        }

        String meta = dataUrl.substring(0, commaIndex);
        String base64Data = dataUrl.substring(commaIndex + 1);

        String extension = getExtensionFromMeta(meta);

        byte[] imageBytes;
        try {
            imageBytes = Base64.getDecoder().decode(base64Data);
        } catch (IllegalArgumentException e) {
            throw new IOException("Không thể giải mã ảnh base64.", e);
        }

        Path uploadPath = baseUploadPath.resolve(folder);
        Files.createDirectories(uploadPath);

        String newFileName = UUID.randomUUID() + extension;
        Path target = uploadPath.resolve(newFileName);

        Files.write(target, imageBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        return "/images/" + folder + "/" + newFileName;
    }

    private String getExtensionFromMeta(String meta) {
        if (meta.contains("image/jpeg")) return ".jpg";
        if (meta.contains("image/png")) return ".png";
        if (meta.contains("image/gif")) return ".gif";
        if (meta.contains("image/webp")) return ".webp";
        return ".png";
    }
}