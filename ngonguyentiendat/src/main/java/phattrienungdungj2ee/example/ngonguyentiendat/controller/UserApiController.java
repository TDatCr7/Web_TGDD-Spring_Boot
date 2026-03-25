package phattrienungdungj2ee.example.ngonguyentiendat.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import phattrienungdungj2ee.example.ngonguyentiendat.dto.api.UserApiRequest;
import phattrienungdungj2ee.example.ngonguyentiendat.dto.api.UserApiResponse;
import phattrienungdungj2ee.example.ngonguyentiendat.model.AppUser;
import phattrienungdungj2ee.example.ngonguyentiendat.model.RoleName;
import phattrienungdungj2ee.example.ngonguyentiendat.service.UserAccountService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@CrossOrigin
public class UserApiController {

    private final UserAccountService userAccountService;

    public UserApiController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @GetMapping("/users/api")
    public String userApiPage() {
        return "redirect:/api/tester?tab=users";
    }

    @GetMapping("/api/users")
    @ResponseBody
    public List<UserApiResponse> getAllUsers() {
        return userAccountService.getAllUsers()
                .stream()
                .map(UserApiResponse::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/api/users/{id}")
    @ResponseBody
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        AppUser user = userAccountService.getById(id);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Không tìm thấy user với id = " + id));
        }
        return ResponseEntity.ok(UserApiResponse.from(user));
    }

    @PostMapping("/api/users")
    @ResponseBody
    public ResponseEntity<?> createUser(@Valid @RequestBody UserApiRequest request) {
        AppUser saved = userAccountService.createUserForApi(
                request.getFullName(),
                request.getEmail(),
                request.getPhoneNumber(),
                request.getPassword(),
                request.getLoyaltyPoints(),
                parseRoles(request.getRoles())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(UserApiResponse.from(saved));
    }

    @PutMapping("/api/users/{id}")
    @ResponseBody
    public ResponseEntity<?> updateUser(@PathVariable Long id,
                                        @Valid @RequestBody UserApiRequest request) {
        AppUser saved = userAccountService.updateUserForApi(
                id,
                request.getFullName(),
                request.getEmail(),
                request.getPhoneNumber(),
                request.getPassword(),
                request.getLoyaltyPoints(),
                parseRoles(request.getRoles()),
                request.getEnabled()
        );
        return ResponseEntity.ok(UserApiResponse.from(saved));
    }

    @DeleteMapping("/api/users/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        AppUser user = userAccountService.getById(id);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Không tìm thấy user với id = " + id));
        }

        userAccountService.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Xóa user thành công."));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Dữ liệu không hợp lệ.");
        response.put("errors", errors);

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseBody
    public ResponseEntity<?> handleIllegalStateException(IllegalStateException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }

    private Set<RoleName> parseRoles(Set<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return Set.of(RoleName.ROLE_USER);
        }

        return roles.stream()
                .filter(role -> role != null && !role.isBlank())
                .map(String::trim)
                .map(String::toUpperCase)
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .map(role -> {
                    try {
                        return RoleName.valueOf(role);
                    } catch (IllegalArgumentException ex) {
                        throw new IllegalStateException("Role không hợp lệ: " + role);
                    }
                })
                .collect(Collectors.toSet());
    }
}