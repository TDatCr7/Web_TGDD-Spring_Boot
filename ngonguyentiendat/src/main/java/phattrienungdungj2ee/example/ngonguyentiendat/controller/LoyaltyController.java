package phattrienungdungj2ee.example.ngonguyentiendat.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import phattrienungdungj2ee.example.ngonguyentiendat.dto.LoyaltyRedeemRequest;
import phattrienungdungj2ee.example.ngonguyentiendat.model.AppUser;
import phattrienungdungj2ee.example.ngonguyentiendat.model.LoyaltyRedemption;
import phattrienungdungj2ee.example.ngonguyentiendat.model.OtpPurpose;
import phattrienungdungj2ee.example.ngonguyentiendat.security.CurrentUser;
import phattrienungdungj2ee.example.ngonguyentiendat.service.CategoryService;
import phattrienungdungj2ee.example.ngonguyentiendat.service.LoyaltyService;
import phattrienungdungj2ee.example.ngonguyentiendat.service.OtpService;
import phattrienungdungj2ee.example.ngonguyentiendat.service.UserAccountService;

@Controller
public class LoyaltyController {
    public static final String PENDING_REDEEM_POINTS = "PENDING_REDEEM_POINTS";

    private final CategoryService categoryService;
    private final UserAccountService userAccountService;
    private final OtpService otpService;
    private final LoyaltyService loyaltyService;

    public LoyaltyController(CategoryService categoryService, UserAccountService userAccountService, OtpService otpService, LoyaltyService loyaltyService) {
        this.categoryService = categoryService;
        this.userAccountService = userAccountService;
        this.otpService = otpService;
        this.loyaltyService = loyaltyService;
    }

    @GetMapping("/loyalty")
    public String loyaltyPage(Authentication authentication, HttpSession session, Model model) {
        CurrentUser currentUser = (CurrentUser) authentication.getPrincipal();
        AppUser user = userAccountService.getById(currentUser.getId());
        Integer pendingPoints = (Integer) session.getAttribute(PENDING_REDEEM_POINTS);

        LoyaltyRedeemRequest redeemRequest = new LoyaltyRedeemRequest();
        if (pendingPoints != null) {
            redeemRequest.setPointsToRedeem(pendingPoints);
        }

        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("userAccount", user);
        model.addAttribute("redeemRequest", redeemRequest);
        model.addAttribute("redeemHistory", loyaltyService.getHistory(user.getId()));
        model.addAttribute("rewardOption10", LoyaltyService.REDEEM_OPTION_10_PERCENT);
        model.addAttribute("rewardOption20", LoyaltyService.REDEEM_OPTION_20_PERCENT);
        return "loyalty/index";
    }

    @PostMapping("/loyalty/send-otp")
    public String sendOtp(@ModelAttribute LoyaltyRedeemRequest request,
                          Authentication authentication,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        CurrentUser currentUser = (CurrentUser) authentication.getPrincipal();
        AppUser user = userAccountService.getById(currentUser.getId());
        int points = request.getPointsToRedeem() == null ? 0 : request.getPointsToRedeem();
        try {
            validateRedeemRequest(user, points);
            session.setAttribute(PENDING_REDEEM_POINTS, points);
            otpService.sendOtp(user.getEmail(), OtpPurpose.LOYALTY_REDEMPTION, "đổi điểm tích lũy thành mã khuyến mãi");
            redirectAttributes.addFlashAttribute("successMessage", "OTP xác nhận đổi điểm đã được gửi qua email.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/loyalty";
    }

    @PostMapping("/loyalty/confirm")
    public String confirmRedeem(@ModelAttribute LoyaltyRedeemRequest request,
                                Authentication authentication,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Integer points = (Integer) session.getAttribute(PENDING_REDEEM_POINTS);
        CurrentUser currentUser = (CurrentUser) authentication.getPrincipal();
        AppUser user = userAccountService.getById(currentUser.getId());
        if (points == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng gửi OTP trước khi xác nhận đổi điểm.");
            return "redirect:/loyalty";
        }
        try {
            validateRedeemRequest(user, points);
            if (!otpService.verifyOtp(user.getEmail(), OtpPurpose.LOYALTY_REDEMPTION, request.getOtpCode())) {
                throw new IllegalStateException("OTP không đúng hoặc đã hết hạn.");
            }
            LoyaltyRedemption redemption = loyaltyService.redeemAfterOtp(user.getId(), points);
            session.removeAttribute(PENDING_REDEEM_POINTS);
            String couponCode = redemption.getCouponCode() != null ? redemption.getCouponCode() : extractCouponCode(redemption.getNote());
            int remainingPoints = userAccountService.getById(user.getId()).getLoyaltyPoints();
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Đổi điểm thành công. Mã khuyến mãi của bạn: " + couponCode + ". Số điểm còn lại: " + remainingPoints
            );
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/loyalty";
    }

    private void validateRedeemRequest(AppUser user, int points) {
        if (points <= 0) {
            throw new IllegalStateException("Vui lòng chọn gói khuyến mãi muốn đổi.");
        }
        if (!loyaltyService.isSupportedRedeemPoints(points)) {
            throw new IllegalStateException("Hiện chỉ hỗ trợ 2 gói: 1000 điểm = mã giảm 10% và 2000 điểm = mã giảm 20%.");
        }
        if (user.getLoyaltyPoints() < points) {
            throw new IllegalStateException("Bạn không đủ điểm để đổi.");
        }
    }

    private String extractCouponCode(String note) {
        if (note == null || note.isBlank()) {
            return "(không xác định)";
        }
        int index = note.lastIndexOf(':');
        if (index < 0 || index >= note.length() - 1) {
            return note;
        }
        return note.substring(index + 1).trim();
    }
}
