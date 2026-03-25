package phattrienungdungj2ee.example.ngonguyentiendat.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import phattrienungdungj2ee.example.ngonguyentiendat.dto.CartPricingResult;
import phattrienungdungj2ee.example.ngonguyentiendat.dto.CheckoutRequest;
import phattrienungdungj2ee.example.ngonguyentiendat.dto.MomoPaymentResult;
import phattrienungdungj2ee.example.ngonguyentiendat.model.AppUser;
import phattrienungdungj2ee.example.ngonguyentiendat.model.Order;
import phattrienungdungj2ee.example.ngonguyentiendat.model.PaymentMethod;
import phattrienungdungj2ee.example.ngonguyentiendat.model.PaymentStatus;
import phattrienungdungj2ee.example.ngonguyentiendat.service.CartService;
import phattrienungdungj2ee.example.ngonguyentiendat.service.CategoryService;
import phattrienungdungj2ee.example.ngonguyentiendat.service.MomoService;
import phattrienungdungj2ee.example.ngonguyentiendat.service.OrderService;
import phattrienungdungj2ee.example.ngonguyentiendat.service.UserAccountService;
import phattrienungdungj2ee.example.ngonguyentiendat.model.LoyaltyRedemption;
import phattrienungdungj2ee.example.ngonguyentiendat.service.LoyaltyService;

import java.util.List;
@Controller
@RequestMapping
public class CheckoutController {

    private final CartService cartService;
    private final OrderService orderService;
    private final CategoryService categoryService;
    private final MomoService momoService;
    private final UserAccountService userAccountService;
    private final LoyaltyService loyaltyService;

    public CheckoutController(CartService cartService,
                              OrderService orderService,
                              CategoryService categoryService,
                              MomoService momoService,
                              UserAccountService userAccountService,
                              LoyaltyService loyaltyService) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.categoryService = categoryService;
        this.momoService = momoService;
        this.userAccountService = userAccountService;
        this.loyaltyService = loyaltyService;
    }

    @GetMapping("/checkout")
    public String checkoutPage(@RequestParam(defaultValue = "0") int usePoints,
                               @RequestParam(required = false) String paymentMethod,
                               @RequestParam(required = false) String voucherCode,
                               @ModelAttribute("currentUserAccount") Object currentUserAccount,
                               HttpSession session,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        CartPricingResult pricing;
        try {
            pricing = cartService.calculateCart(session, usePoints, voucherCode);
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/cart";
        }

        if (pricing.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Giỏ hàng đang trống.");
            return "redirect:/cart";
        }

        CheckoutRequest request = new CheckoutRequest();
        request.setLoyaltyPointsToUse(usePoints);
        request.setVoucherCode(voucherCode);

        if (currentUserAccount instanceof AppUser user) {
            request.setCustomerName(user.getFullName());
            request.setEmail(user.getEmail());
            request.setPhoneNumber(user.getPhoneNumber());
        }

        if ("MOMO".equalsIgnoreCase(paymentMethod)) {
            request.setPaymentMethod(PaymentMethod.MOMO);
        } else if ("BANK_TRANSFER".equalsIgnoreCase(paymentMethod)) {
            request.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        } else {
            request.setPaymentMethod(PaymentMethod.COD);
        }

        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("checkoutRequest", request);
        model.addAttribute("cartPricing", pricing);
        if (currentUserAccount instanceof AppUser user) {
            List<LoyaltyRedemption> availableVouchers = loyaltyService.getHistory(user.getId()).stream()
                    .filter(item -> !item.isUsed())
                    .filter(item -> item.getCouponCode() != null && !item.getCouponCode().isBlank())
                    .filter(item -> item.getDiscountPercent() != null && item.getDiscountPercent() > 0)
                    .toList();
            model.addAttribute("availableVouchers", availableVouchers);
        } else {
            model.addAttribute("availableVouchers", List.of());
        }
        return "order/checkout";
    }

    @PostMapping("/checkout")
    public String submitCheckout(@ModelAttribute CheckoutRequest checkoutRequest,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        try {
            if (checkoutRequest.getPaymentMethod() == null) {
                checkoutRequest.setPaymentMethod(PaymentMethod.COD);
            }

            if (checkoutRequest.getPaymentMethod() == PaymentMethod.MOMO) {
                MomoPaymentResult momoPayment = momoService.createPayment(checkoutRequest, session);
                return "redirect:" + momoPayment.getPayUrl();
            }

            Order order = orderService.createOrderFromCart(checkoutRequest, session);
            return "redirect:/orders/confirmation/" + order.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

            String paymentMethod = checkoutRequest.getPaymentMethod() == null
                    ? "COD"
                    : checkoutRequest.getPaymentMethod().name();

            int usePoints = checkoutRequest.getLoyaltyPointsToUse() == null
                    ? 0
                    : checkoutRequest.getLoyaltyPointsToUse();

            String voucherCode = checkoutRequest.getVoucherCode() == null ? "" : checkoutRequest.getVoucherCode().trim();
            String voucherQuery = voucherCode.isEmpty() ? "" : "&voucherCode=" + voucherCode;

            return "redirect:/checkout?usePoints=" + usePoints + "&paymentMethod=" + paymentMethod + voucherQuery;
        }
    }

    @GetMapping("/orders/confirmation/{id}")
    public String orderConfirmation(@PathVariable Long id,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        Order order = orderService.getOrderForView(id);
        if (order == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng.");
            return "redirect:/";
        }

        if (order.getPaymentMethod() == PaymentMethod.MOMO && order.getPaymentStatus() != PaymentStatus.PAID) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đơn hàng MoMo chưa thanh toán thành công.");
            return "redirect:/orders/" + order.getId();
        }

        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("order", order);
        return "order/confirmation";
    }

    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Order order = orderService.getOrderForView(id);
        if (order == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng.");
            return "redirect:/";
        }

        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("order", order);
        return "order/detail";
    }
}
