package phattrienungdungj2ee.example.ngonguyentiendat.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import phattrienungdungj2ee.example.ngonguyentiendat.dto.api.CategoryApiRequest;
import phattrienungdungj2ee.example.ngonguyentiendat.dto.api.CategoryApiResponse;
import phattrienungdungj2ee.example.ngonguyentiendat.model.Category;
import phattrienungdungj2ee.example.ngonguyentiendat.service.CategoryService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@CrossOrigin
public class CategoryApiController {

    private final CategoryService categoryService;

    public CategoryApiController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/categories/api")
    public String categoryApiPage() {
        return "redirect:/api/tester?tab=categories";
    }

    @GetMapping("/api/categories")
    @ResponseBody
    public List<CategoryApiResponse> getAllCategories() {
        return categoryService.getAllCategories()
                .stream()
                .map(CategoryApiResponse::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/api/categories/{id}")
    @ResponseBody
    public ResponseEntity<?> getCategoryById(@PathVariable Long id) {
        Category category = categoryService.getCategoryById(id);
        if (category == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Không tìm thấy danh mục với id = " + id));
        }
        return ResponseEntity.ok(CategoryApiResponse.from(category));
    }

    @PostMapping("/api/categories")
    @ResponseBody
    public ResponseEntity<?> createCategory(@Valid @RequestBody CategoryApiRequest request) {
        validateCategoryName(null, request.getName());

        Category category = new Category();
        mapRequestToCategory(category, request);
        Category saved = categoryService.save(category);

        return ResponseEntity.status(HttpStatus.CREATED).body(CategoryApiResponse.from(saved));
    }

    @PutMapping("/api/categories/{id}")
    @ResponseBody
    public ResponseEntity<?> updateCategory(@PathVariable Long id,
                                            @Valid @RequestBody CategoryApiRequest request) {
        Category category = categoryService.getCategoryById(id);
        if (category == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Không tìm thấy danh mục với id = " + id));
        }

        validateCategoryName(id, request.getName());
        mapRequestToCategory(category, request);
        Category saved = categoryService.save(category);

        return ResponseEntity.ok(CategoryApiResponse.from(saved));
    }

    @DeleteMapping("/api/categories/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        Category category = categoryService.getCategoryById(id);
        if (category == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Không tìm thấy danh mục với id = " + id));
        }

        try {
            categoryService.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Xóa danh mục thành công."));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
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

    private void validateCategoryName(Long currentId, String name) {
        String normalized = name == null ? "" : name.trim();
        boolean duplicate = categoryService.getAllCategories().stream()
                .anyMatch(category ->
                        category.getName() != null
                                && category.getName().trim().equalsIgnoreCase(normalized)
                                && (currentId == null || !category.getId().equals(currentId))
                );

        if (duplicate) {
            throw new IllegalStateException("Tên danh mục đã tồn tại.");
        }
    }

    private void mapRequestToCategory(Category category, CategoryApiRequest request) {
        String slugSource = (request.getSlug() == null || request.getSlug().isBlank())
                ? request.getName()
                : request.getSlug();

        category.setName(request.getName().trim());
        category.setSlug(makeUniqueSlug(toSlug(slugSource), category.getId()));
        category.setMenuGroup(normalizeMenuGroup(request.getMenuGroup()));
        category.setImageUrl(
                request.getImageUrl() == null || request.getImageUrl().isBlank()
                        ? null
                        : request.getImageUrl().trim()
        );
    }

    private String normalizeMenuGroup(String menuGroup) {
        if (menuGroup == null || menuGroup.isBlank()) {
            return null;
        }
        String normalized = menuGroup.trim().toUpperCase();
        return categoryService.getAccessoryGroupOptions().containsKey(normalized) ? normalized : null;
    }

    private String makeUniqueSlug(String baseSlug, Long currentId) {
        String rootSlug = (baseSlug == null || baseSlug.isBlank()) ? "danh-muc" : baseSlug;
        String candidate = rootSlug;
        int suffix = 1;

        while (true) {
            Category found = categoryService.getCategoryBySlug(candidate);
            if (found == null || (currentId != null && currentId.equals(found.getId()))) {
                return candidate;
            }
            candidate = rootSlug + "-" + suffix++;
        }
    }

    private String toSlug(String input) {
        if (input == null) return "";

        return input.toLowerCase()
                .replace("đ", "d")
                .replace("á", "a").replace("à", "a").replace("ả", "a").replace("ã", "a").replace("ạ", "a")
                .replace("ă", "a").replace("ắ", "a").replace("ằ", "a").replace("ẳ", "a").replace("ẵ", "a").replace("ặ", "a")
                .replace("â", "a").replace("ấ", "a").replace("ầ", "a").replace("ẩ", "a").replace("ẫ", "a").replace("ậ", "a")
                .replace("é", "e").replace("è", "e").replace("ẻ", "e").replace("ẽ", "e").replace("ẹ", "e")
                .replace("ê", "e").replace("ế", "e").replace("ề", "e").replace("ể", "e").replace("ễ", "e").replace("ệ", "e")
                .replace("í", "i").replace("ì", "i").replace("ỉ", "i").replace("ĩ", "i").replace("ị", "i")
                .replace("ó", "o").replace("ò", "o").replace("ỏ", "o").replace("õ", "o").replace("ọ", "o")
                .replace("ô", "o").replace("ố", "o").replace("ồ", "o").replace("ổ", "o").replace("ỗ", "o").replace("ộ", "o")
                .replace("ơ", "o").replace("ớ", "o").replace("ờ", "o").replace("ở", "o").replace("ỡ", "o").replace("ợ", "o")
                .replace("ú", "u").replace("ù", "u").replace("ủ", "u").replace("ũ", "u").replace("ụ", "u")
                .replace("ư", "u").replace("ứ", "u").replace("ừ", "u").replace("ử", "u").replace("ữ", "u").replace("ự", "u")
                .replace("ý", "y").replace("ỳ", "y").replace("ỷ", "y").replace("ỹ", "y").replace("ỵ", "y")
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
}