package phattrienungdungj2ee.example.ngonguyentiendat.service;

import jakarta.servlet.http.HttpSession;
import phattrienungdungj2ee.example.ngonguyentiendat.dto.CheckoutRequest;
import phattrienungdungj2ee.example.ngonguyentiendat.dto.MomoPaymentResult;
import phattrienungdungj2ee.example.ngonguyentiendat.model.Order;

import java.util.Map;

public interface MomoService {
    MomoPaymentResult createPayment(CheckoutRequest checkoutRequest, HttpSession session);

    Order handleReturn(Map<String, String> params, HttpSession session);

    Order handleIpn(Map<String, String> params);
}