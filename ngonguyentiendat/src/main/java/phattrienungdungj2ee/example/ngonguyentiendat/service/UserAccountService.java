package phattrienungdungj2ee.example.ngonguyentiendat.service;

import phattrienungdungj2ee.example.ngonguyentiendat.model.AppUser;
import phattrienungdungj2ee.example.ngonguyentiendat.model.LoyaltyRedemption;
import phattrienungdungj2ee.example.ngonguyentiendat.model.RoleName;

import java.util.List;
import java.util.Set;

public interface UserAccountService {
    AppUser registerUser(String fullName, String email, String phoneNumber, String rawPassword);

    AppUser createUserWithRoles(String fullName,
                                String email,
                                String phoneNumber,
                                String rawPassword,
                                Integer loyaltyPoints,
                                Set<RoleName> roleNames);

    AppUser getByEmail(String email);
    AppUser getById(Long id);

    void applyOrderPoints(Long userId, int usedPoints, int earnedPoints);
    LoyaltyRedemption redeemPoints(Long userId, int pointsToRedeem, String note);

    List<AppUser> getAllUsers();
    boolean existsByEmail(String email);

    AppUser createUserForApi(String fullName,
                             String email,
                             String phoneNumber,
                             String rawPassword,
                             Integer loyaltyPoints,
                             Set<RoleName> roleNames);

    AppUser updateUserForApi(Long id,
                             String fullName,
                             String email,
                             String phoneNumber,
                             String rawPassword,
                             Integer loyaltyPoints,
                             Set<RoleName> roleNames,
                             Boolean enabled);

    void deleteById(Long id);
}