package phattrienungdungj2ee.example.ngonguyentiendat.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import phattrienungdungj2ee.example.ngonguyentiendat.model.AppUser;
import phattrienungdungj2ee.example.ngonguyentiendat.model.LoyaltyRedemption;
import phattrienungdungj2ee.example.ngonguyentiendat.model.Role;
import phattrienungdungj2ee.example.ngonguyentiendat.model.RoleName;
import phattrienungdungj2ee.example.ngonguyentiendat.repository.AppUserRepository;
import phattrienungdungj2ee.example.ngonguyentiendat.repository.LoyaltyRedemptionRepository;
import phattrienungdungj2ee.example.ngonguyentiendat.repository.RoleRepository;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class UserAccountServiceImpl implements UserAccountService {
    private static final int REDEEM_OPTION_10_PERCENT = 1000;
    private static final int REDEEM_OPTION_20_PERCENT = 2000;

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoyaltyRedemptionRepository loyaltyRedemptionRepository;

    public UserAccountServiceImpl(AppUserRepository appUserRepository,
                                  RoleRepository roleRepository,
                                  PasswordEncoder passwordEncoder,
                                  LoyaltyRedemptionRepository loyaltyRedemptionRepository) {
        this.appUserRepository = appUserRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.loyaltyRedemptionRepository = loyaltyRedemptionRepository;
    }

    @Override
    @Transactional
    public AppUser registerUser(String fullName, String email, String phoneNumber, String rawPassword) {
        return createUserWithRoles(fullName, email, phoneNumber, rawPassword, 0, Set.of(RoleName.ROLE_USER));
    }

    @Override
    @Transactional
    public AppUser createUserWithRoles(String fullName,
                                       String email,
                                       String phoneNumber,
                                       String rawPassword,
                                       Integer loyaltyPoints,
                                       Set<RoleName> roleNames) {
        String normalizedEmail = normalizeEmail(email);
        if (appUserRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalStateException("Email đã tồn tại.");
        }

        AppUser user = new AppUser();
        user.setFullName(fullName);
        user.setEmail(normalizedEmail);
        user.setPhoneNumber(phoneNumber);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setEnabled(true);
        user.setLoyaltyPoints(loyaltyPoints == null ? 0 : loyaltyPoints);
        user.setRoles(resolveRoles(roleNames));

        return appUserRepository.save(user);
    }

    @Override
    public AppUser getByEmail(String email) {
        return appUserRepository.findByEmail(normalizeEmail(email)).orElse(null);
    }

    @Override
    public AppUser getById(Long id) {
        return id == null ? null : appUserRepository.findById(id).orElse(null);
    }

    @Override
    public List<AppUser> getAllUsers() {
        return appUserRepository.findAllByOrderByIdDesc();
    }

    @Override
    public boolean existsByEmail(String email) {
        return appUserRepository.existsByEmail(normalizeEmail(email));
    }

    @Override
    @Transactional
    public AppUser createUserForApi(String fullName,
                                    String email,
                                    String phoneNumber,
                                    String rawPassword,
                                    Integer loyaltyPoints,
                                    Set<RoleName> roleNames) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalStateException("Mật khẩu không được để trống.");
        }

        return createUserWithRoles(
                fullName,
                email,
                phoneNumber,
                rawPassword,
                loyaltyPoints == null ? 0 : loyaltyPoints,
                roleNames == null || roleNames.isEmpty() ? Set.of(RoleName.ROLE_USER) : roleNames
        );
    }

    @Override
    @Transactional
    public AppUser updateUserForApi(Long id,
                                    String fullName,
                                    String email,
                                    String phoneNumber,
                                    String rawPassword,
                                    Integer loyaltyPoints,
                                    Set<RoleName> roleNames,
                                    Boolean enabled) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy user với id = " + id));

        String normalizedEmail = normalizeEmail(email);
        AppUser existingByEmail = appUserRepository.findByEmail(normalizedEmail).orElse(null);
        if (existingByEmail != null && !existingByEmail.getId().equals(id)) {
            throw new IllegalStateException("Email đã tồn tại.");
        }

        user.setFullName(fullName);
        user.setEmail(normalizedEmail);
        user.setPhoneNumber(phoneNumber);
        user.setLoyaltyPoints(loyaltyPoints == null ? 0 : Math.max(0, loyaltyPoints));

        if (enabled != null) {
            user.setEnabled(enabled);
        }

        if (rawPassword != null && !rawPassword.isBlank()) {
            user.setPassword(passwordEncoder.encode(rawPassword));
        }

        if (roleNames != null && !roleNames.isEmpty()) {
            user.setRoles(resolveRoles(roleNames));
        }

        return appUserRepository.save(user);
    }

    @Override
    public void deleteById(Long id) {
        if (id == null || !appUserRepository.existsById(id)) {
            return;
        }
        appUserRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void applyOrderPoints(Long userId, int usedPoints, int earnedPoints) {
        if (userId == null) {
            return;
        }
        AppUser user = appUserRepository.findById(userId).orElse(null);
        if (user == null) {
            return;
        }
        int current = user.getLoyaltyPoints();
        int next = Math.max(0, current - Math.max(0, usedPoints)) + Math.max(0, earnedPoints);
        user.setLoyaltyPoints(next);
        appUserRepository.save(user);
    }

    @Override
    @Transactional
    public LoyaltyRedemption redeemPoints(Long userId, int pointsToRedeem, String note) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy người dùng."));
        int sanitized = Math.max(0, pointsToRedeem);
        if (sanitized <= 0) {
            throw new IllegalStateException("Số điểm đổi phải lớn hơn 0.");
        }
        if (sanitized != REDEEM_OPTION_10_PERCENT && sanitized != REDEEM_OPTION_20_PERCENT) {
            throw new IllegalStateException("Hiện chỉ hỗ trợ đổi 1000 điểm lấy mã giảm 10% hoặc 2000 điểm lấy mã giảm 20%.");
        }
        if (user.getLoyaltyPoints() < sanitized) {
            throw new IllegalStateException("Bạn không đủ điểm tích lũy để đổi.");
        }

        int discountPercent = sanitized == REDEEM_OPTION_10_PERCENT ? 10 : 20;
        String couponCode = generateCouponCode(discountPercent);

        user.setLoyaltyPoints(user.getLoyaltyPoints() - sanitized);
        appUserRepository.save(user);

        LoyaltyRedemption redemption = new LoyaltyRedemption();
        redemption.setUser(user);
        redemption.setPointsRedeemed(sanitized);
        redemption.setValueAmount(BigDecimal.valueOf(discountPercent));
        redemption.setCouponCode(couponCode);
        redemption.setDiscountPercent(discountPercent);
        redemption.setStatus("AVAILABLE");
        redemption.setUsed(false);
        redemption.setNote(buildRedemptionNote(note, discountPercent, couponCode));
        return loyaltyRedemptionRepository.save(redemption);
    }

    private String buildRedemptionNote(String note, int discountPercent, String couponCode) {
        String prefix = note == null || note.isBlank() ? "Đổi điểm qua OTP email" : note.trim();
        return prefix + " | Mã " + discountPercent + "%: " + couponCode;
    }

    private String generateCouponCode(int discountPercent) {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "KM" + discountPercent + "-" + suffix;
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private Set<Role> resolveRoles(Set<RoleName> roleNames) {
        Set<RoleName> safeRoleNames = (roleNames == null || roleNames.isEmpty())
                ? Set.of(RoleName.ROLE_USER)
                : roleNames;

        Set<Role> roles = new LinkedHashSet<>();
        for (RoleName roleName : safeRoleNames) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new IllegalStateException("Thiếu role: " + roleName));
            roles.add(role);
        }
        return roles;
    }
}