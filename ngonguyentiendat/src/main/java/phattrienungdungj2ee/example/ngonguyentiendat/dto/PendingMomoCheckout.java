package phattrienungdungj2ee.example.ngonguyentiendat.dto;

public class PendingMomoCheckout {
    private String momoOrderId;
    private String requestId;
    private CheckoutRequest checkoutRequest;
    private CartPricingResult cartPricing;
    private Long savedOrderId;
    private boolean completed;
    private boolean failed;
    private String paymentReference;

    public String getMomoOrderId() {
        return momoOrderId;
    }

    public void setMomoOrderId(String momoOrderId) {
        this.momoOrderId = momoOrderId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public CheckoutRequest getCheckoutRequest() {
        return checkoutRequest;
    }

    public void setCheckoutRequest(CheckoutRequest checkoutRequest) {
        this.checkoutRequest = checkoutRequest;
    }

    public CartPricingResult getCartPricing() {
        return cartPricing;
    }

    public void setCartPricing(CartPricingResult cartPricing) {
        this.cartPricing = cartPricing;
    }

    public Long getSavedOrderId() {
        return savedOrderId;
    }

    public void setSavedOrderId(Long savedOrderId) {
        this.savedOrderId = savedOrderId;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }
}