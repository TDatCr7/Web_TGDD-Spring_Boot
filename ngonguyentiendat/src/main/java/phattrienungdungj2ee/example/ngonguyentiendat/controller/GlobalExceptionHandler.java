package phattrienungdungj2ee.example.ngonguyentiendat.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex,
                                              HttpServletRequest request,
                                              RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("errorMessage",
                "Ảnh tải lên quá lớn. Vui lòng chọn ảnh nhỏ hơn giới hạn cho phép.");

        String uri = request.getRequestURI();

        if (uri.startsWith("/products/edit/")) {
            return "redirect:" + uri;
        }

        if ("/products/add".equals(uri)) {
            return "redirect:/products/add";
        }

        return "redirect:/admin";
    }
}