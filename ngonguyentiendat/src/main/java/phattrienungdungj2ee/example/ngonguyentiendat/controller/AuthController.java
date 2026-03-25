package phattrienungdungj2ee.example.ngonguyentiendat.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import phattrienungdungj2ee.example.ngonguyentiendat.dto.RegisterRequest;
import phattrienungdungj2ee.example.ngonguyentiendat.model.OtpPurpose;
import phattrienungdungj2ee.example.ngonguyentiendat.service.CategoryService;
import phattrienungdungj2ee.example.ngonguyentiendat.service.OtpService;
import phattrienungdungj2ee.example.ngonguyentiendat.service.UserAccountService;

@Controller
public class AuthController {
    public static final String PENDING_REGISTER_SESSION_KEY = "PENDING_REGISTER_REQUEST";

    private final CategoryService categoryService;
    private final OtpService otpService;
    private final UserAccountService userAccountService;

    public AuthController(CategoryService categoryService, OtpService otpService, UserAccountService userAccountService) {
        this.categoryService = categoryService;
        this.otpService = otpService;
        this.userAccountService = userAccountService;
    }

    @GetMapping("/auth/login")
    public String loginPage(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        return "auth/login";
    }

    @GetMapping("/auth/register")
    public String registerPage(HttpSession session, Model model) {
        RegisterRequest request = (RegisterRequest) session.getAttribute(PENDING_REGISTER_SESSION_KEY);
        if (request == null) {
            request = new RegisterRequest();
        }
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("registerRequest", request);
        return "auth/register";
    }

    @PostMapping("/auth/register/send-otp")
    public String sendRegisterOtp(@ModelAttribute RegisterRequest registerRequest,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        try {
            validateRegisterInput(registerRequest);
            if (userAccountService.getByEmail(registerRequest.getEmail()) != null) {
                throw new IllegalStateException("Email đã tồn tại.");
            }
            session.setAttribute(PENDING_REGISTER_SESSION_KEY, sanitize(registerRequest));
            otpService.sendOtp(registerRequest.getEmail(), OtpPurpose.REGISTER, "đăng ký tài khoản");
            redirectAttributes.addFlashAttribute("successMessage", "Đã gửi OTP đến email của bạn.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/auth/register";
    }

    @PostMapping("/auth/register/verify")
    public String verifyRegister(@ModelAttribute RegisterRequest form,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        RegisterRequest pending = (RegisterRequest) session.getAttribute(PENDING_REGISTER_SESSION_KEY);
        if (pending == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng nhập thông tin và gửi OTP trước.");
            return "redirect:/auth/register";
        }
        try {
            if (!otpService.verifyOtp(pending.getEmail(), OtpPurpose.REGISTER, form.getOtpCode())) {
                throw new IllegalStateException("OTP không đúng hoặc đã hết hạn.");
            }
            userAccountService.registerUser(pending.getFullName(), pending.getEmail(), pending.getPhoneNumber(), pending.getPassword());
            session.removeAttribute(PENDING_REGISTER_SESSION_KEY);
            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký tài khoản thành công. Hãy đăng nhập.");
            return "redirect:/auth/login";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/auth/register";
        }
    }

    @GetMapping("/auth/access-denied")
    public String accessDenied(HttpServletResponse response, Model model) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "auth/access-denied";
    }

    private RegisterRequest sanitize(RegisterRequest request) {
        RegisterRequest copy = new RegisterRequest();
        copy.setFullName(request.getFullName());
        copy.setEmail(request.getEmail());
        copy.setPhoneNumber(request.getPhoneNumber());
        copy.setPassword(request.getPassword());
        copy.setConfirmPassword(request.getConfirmPassword());
        return copy;
    }

    private void validateRegisterInput(RegisterRequest request) {
        if (request.getFullName() == null || request.getFullName().isBlank()) {
            throw new IllegalStateException("Vui lòng nhập họ tên.");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalStateException("Vui lòng nhập email.");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new IllegalStateException("Mật khẩu phải có ít nhất 6 ký tự.");
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalStateException("Xác nhận mật khẩu chưa khớp.");
        }
    }
}