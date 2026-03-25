package phattrienungdungj2ee.example.ngonguyentiendat.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import phattrienungdungj2ee.example.ngonguyentiendat.model.AppUser;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class CurrentUser implements UserDetails {
    private final Long id;
    private final String fullName;
    private final String email;
    private final String password;
    private final boolean enabled;
    private final Integer loyaltyPoints;
    private final Set<GrantedAuthority> authorities;

    public CurrentUser(AppUser user) {
        this.id = user.getId();
        this.fullName = user.getFullName();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.enabled = user.isEnabled();
        this.loyaltyPoints = user.getLoyaltyPoints();
        this.authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toSet());
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmailAddress() { return email; }
    public Integer getLoyaltyPoints() { return loyaltyPoints; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return enabled; }
}
