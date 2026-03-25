package phattrienungdungj2ee.example.ngonguyentiendat.dto;

import phattrienungdungj2ee.example.ngonguyentiendat.model.PaymentMethod;

public class CheckoutRequest {
    private String customerName;
    private String email;
    private String phoneNumber;
    private String address;
    private String notes;
    private PaymentMethod paymentMethod = PaymentMethod.COD;
    private Integer loyaltyPointsToUse = 0;
    private String voucherCode;

    public String getCustomerName() {
        return customerName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public String getNotes() {
        return notes;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public Integer getLoyaltyPointsToUse() {
        return loyaltyPointsToUse;
    }

    public String getVoucherCode() {
        return voucherCode;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void setLoyaltyPointsToUse(Integer loyaltyPointsToUse) {
        this.loyaltyPointsToUse = loyaltyPointsToUse;
    }

    public void setVoucherCode(String voucherCode) {
        this.voucherCode = voucherCode;
    }
}