package phattrienungdungj2ee.example.ngonguyentiendat.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import phattrienungdungj2ee.example.ngonguyentiendat.dto.CartPricingResult;
import phattrienungdungj2ee.example.ngonguyentiendat.service.CartService;
import phattrienungdungj2ee.example.ngonguyentiendat.service.CategoryService;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final CategoryService categoryService;

    public CartController(CartService cartService, CategoryService categoryService) {
        this.cartService = cartService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String cartPage(HttpSession session, Model model) {
        CartPricingResult pricing = cartService.calculateCart(session, 0, null);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("cartPricing", pricing);
        return "cart/cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam Long productId,
                            @RequestParam(defaultValue = "1") int quantity,
                            @RequestParam(required = false) String redirect,
                            HttpServletRequest request,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        try {
            cartService.addToCart(session, productId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm sản phẩm vào giỏ hàng.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        if ("cart".equalsIgnoreCase(redirect)) {
            return "redirect:/cart";
        }

        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }

    @PostMapping("/buy-now")
    public String buyNow(@RequestParam Long productId,
                         @RequestParam(defaultValue = "1") int quantity,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        try {
            cartService.addToCart(session, productId, quantity);
            return "redirect:/checkout";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/cart";
        }
    }

    @PostMapping("/update")
    public String updateQuantity(@RequestParam Long productId,
                                 @RequestParam int quantity,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        try {
            cartService.updateItemQuantity(session, productId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật giỏ hàng.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String removeItem(@RequestParam Long productId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        cartService.removeItem(session, productId);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa sản phẩm khỏi giỏ hàng.");
        return "redirect:/cart";
    }

    @PostMapping("/clear")
    public String clearCart(HttpSession session, RedirectAttributes redirectAttributes) {
        cartService.clearCart(session);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa toàn bộ giỏ hàng.");
        return "redirect:/cart";
    }
}