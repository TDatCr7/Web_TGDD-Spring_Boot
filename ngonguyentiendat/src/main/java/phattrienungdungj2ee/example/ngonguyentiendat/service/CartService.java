package phattrienungdungj2ee.example.ngonguyentiendat.service;

import jakarta.servlet.http.HttpSession;
import phattrienungdungj2ee.example.ngonguyentiendat.dto.CartItem;
import phattrienungdungj2ee.example.ngonguyentiendat.dto.CartPricingResult;

import java.util.List;

public interface CartService {
    List<CartItem> getCartItems(HttpSession session);
    void addToCart(HttpSession session, Long productId, int quantity);
    void updateItemQuantity(HttpSession session, Long productId, int quantity);
    void removeItem(HttpSession session, Long productId);
    void clearCart(HttpSession session);
    int getTotalQuantity(HttpSession session);
    CartPricingResult calculateCart(HttpSession session, int loyaltyPointsToUse, String voucherCode);
}