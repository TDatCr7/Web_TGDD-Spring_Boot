package phattrienungdungj2ee.example.ngonguyentiendat.dto.api;

import phattrienungdungj2ee.example.ngonguyentiendat.model.AppUser;

import java.util.List;
import java.util.stream.Collectors;

public class UserApiResponse {

    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private Integer loyaltyPoints;
    private boolean enabled;
    private List<String> roles;
    private String primaryRole;

    public static UserApiResponse from(AppUser user) {
        UserApiResponse response = new UserApiResponse();
        response.id = user.getId();
        response.fullName = user.getFullName();
        response.email = user.getEmail();
        response.phoneNumber = user.getPhoneNumber();
        response.loyaltyPoints = user.getLoyaltyPoints();
        response.enabled = user.isEnabled();
        response.roles = user.getRoles()
                .stream()
                .map(role -> role.getName().name())
                .sorted()
                .collect(Collectors.toList());
        response.primaryRole = response.roles.isEmpty() ? null : response.roles.get(0);
        return response;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Integer getLoyaltyPoints() {
        return loyaltyPoints;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public List<String> getRoles() {
        return roles;
    }

    public String getPrimaryRole() {
        return primaryRole;
    }
}