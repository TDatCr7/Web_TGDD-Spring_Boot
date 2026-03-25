package phattrienungdungj2ee.example.ngonguyentiendat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import phattrienungdungj2ee.example.ngonguyentiendat.model.LoyaltyRedemption;
import java.util.List;

public interface LoyaltyRedemptionRepository extends JpaRepository<LoyaltyRedemption, Long> {
    List<LoyaltyRedemption> findByUserIdOrderByCreatedAtDesc(Long userId);
    java.util.Optional<LoyaltyRedemption> findByUserIdAndCouponCodeIgnoreCase(Long userId, String couponCode);
}
