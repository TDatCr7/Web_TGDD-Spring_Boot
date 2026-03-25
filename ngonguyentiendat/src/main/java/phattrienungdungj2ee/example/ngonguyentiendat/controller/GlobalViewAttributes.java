package phattrienungdungj2ee.example.ngonguyentiendat.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import phattrienungdungj2ee.example.ngonguyentiendat.model.Category;
import phattrienungdungj2ee.example.ngonguyentiendat.security.CurrentUser;
import phattrienungdungj2ee.example.ngonguyentiendat.service.CartService;
import phattrienungdungj2ee.example.ngonguyentiendat.service.CategoryService;
import phattrienungdungj2ee.example.ngonguyentiendat.service.UserAccountService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalViewAttributes {

    private final CategoryService categoryService;
    private final CartService cartService;
    private final UserAccountService userAccountService;

    public GlobalViewAttributes(CategoryService categoryService,
                                CartService cartService,
                                UserAccountService userAccountService) {
        this.categoryService = categoryService;
        this.cartService = cartService;
        this.userAccountService = userAccountService;
    }

    @ModelAttribute("menuGroupOptions")
    public LinkedHashMap<String, String> menuGroupOptions() {
        return categoryService.getAccessoryGroupOptions();
    }

    @ModelAttribute("accessoryCategoriesByGroup")
    public Map<String, List<Category>> accessoryCategoriesByGroup() {
        return categoryService.getAccessoryCategoriesByGroup();
    }

    @ModelAttribute("cartTotalQuantity")
    public Integer cartTotalQuantity(HttpSession session) {
        return cartService.getTotalQuantity(session);
    }

    @ModelAttribute("currentUserAccount")
    public Object currentUserAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CurrentUser currentUser)) {
            return null;
        }
        return userAccountService.getById(currentUser.getId());
    }

    @ModelAttribute("isAdmin")
    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }

    @ModelAttribute("isManager")
    public boolean isManager() {
        return hasRole("ROLE_MANAGER");
    }

    @ModelAttribute("isUserRole")
    public boolean isUserRole() {
        return hasRole("ROLE_USER");
    }

    @ModelAttribute("isAuthenticatedUser")
    public boolean isAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(String.valueOf(authentication.getPrincipal()));
    }

    private boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(role));
    }
}
