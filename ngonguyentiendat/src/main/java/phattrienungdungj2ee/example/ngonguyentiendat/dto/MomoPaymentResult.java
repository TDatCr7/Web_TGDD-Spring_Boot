package phattrienungdungj2ee.example.ngonguyentiendat.dto;

public class MomoPaymentResult {
    private boolean success;
    private String payUrl;
    private String qrCodeUrl;
    private String deeplink;
    private String message;
    private Long orderId;

    public boolean isSuccess() {
        return success;
    }

    public String getPayUrl() {
        return payUrl;
    }

    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    public String getDeeplink() {
        return deeplink;
    }

    public String getMessage() {
        return message;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setPayUrl(String payUrl) {
        this.payUrl = payUrl;
    }

    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
    }

    public void setDeeplink(String deeplink) {
        this.deeplink = deeplink;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
}