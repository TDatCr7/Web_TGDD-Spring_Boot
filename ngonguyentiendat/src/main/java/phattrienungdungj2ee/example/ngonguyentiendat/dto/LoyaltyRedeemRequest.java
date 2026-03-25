package phattrienungdungj2ee.example.ngonguyentiendat.dto;

public class LoyaltyRedeemRequest {
    private Integer pointsToRedeem;
    private String otpCode;

    public Integer getPointsToRedeem() { return pointsToRedeem; }
    public void setPointsToRedeem(Integer pointsToRedeem) { this.pointsToRedeem = pointsToRedeem; }
    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }
}
