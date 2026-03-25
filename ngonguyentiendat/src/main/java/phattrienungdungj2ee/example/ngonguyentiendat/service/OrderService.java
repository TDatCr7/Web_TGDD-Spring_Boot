package phattrienungdungj2ee.example.ngonguyentiendat.service;

import jakarta.servlet.http.HttpSession;
import phattrienungdungj2ee.example.ngonguyentiendat.dto.CartPricingResult;
import phattrienungdungj2ee.example.ngonguyentiendat.dto.CheckoutRequest;
import phattrienungdungj2ee.example.ngonguyentiendat.model.Order;
import phattrienungdungj2ee.example.ngonguyentiendat.model.PaymentStatus;

import java.util.List;

public interface OrderService {
    Order createOrderFromCart(CheckoutRequest request, HttpSession session);

    Order createPaidOrderFromCheckout(CheckoutRequest request,
                                      CartPricingResult pricing,
                                      String paymentReference,
                                      HttpSession session);

    Order finalizePaidOrder(Long orderId, String paymentReference, HttpSession session);

    Order failOrder(Long orderId, String paymentReference);

    Order getOrderById(Long id);

    Order getOrderForView(Long id);

    List<Order> getAllOrders();

    void updatePaymentStatus(Long orderId, PaymentStatus paymentStatus);
}