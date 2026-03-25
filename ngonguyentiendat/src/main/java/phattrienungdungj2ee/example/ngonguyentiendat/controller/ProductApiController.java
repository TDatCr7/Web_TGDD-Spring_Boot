package phattrienungdungj2ee.example.ngonguyentiendat.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import phattrienungdungj2ee.example.ngonguyentiendat.dto.api.ProductApiRequest;
import phattrienungdungj2ee.example.ngonguyentiendat.dto.api.ProductApiResponse;
import phattrienungdungj2ee.example.ngonguyentiendat.model.Category;
import phattrienungdungj2ee.example.ngonguyentiendat.model.Product;
import phattrienungdungj2ee.example.ngonguyentiendat.model.ProductStockStatus;
import phattrienungdungj2ee.example.ngonguyentiendat.service.CategoryService;
import phattrienungdungj2ee.example.ngonguyentiendat.service.ProductService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@CrossOrigin
public class ProductApiController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public ProductApiController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping("/products/api")
    public String productApiPage() {
        return "redirect:/api/tester?tab=products";
    }

    @GetMapping("/api/products")
    @ResponseBody
    public List<ProductApiResponse> getAllProducts() {
        return productService.getAllProducts()
                .stream()
                .map(ProductApiResponse::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/api/products/{id}")
    @ResponseBody
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Không tìm thấy sản phẩm với id = " + id));
        }
        return ResponseEntity.ok(ProductApiResponse.from(product));
    }

    @PostMapping("/api/products")
    @ResponseBody
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductApiRequest request) {
        Category category = categoryService.getCategoryById(request.getCategoryId());
        if (category == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Danh mục không hợp lệ."));
        }

        Product product = new Product();
        mapRequestToProduct(product, request, category);
        Product saved = productService.save(product);

        return ResponseEntity.status(HttpStatus.CREATED).body(ProductApiResponse.from(saved));
    }

    @PutMapping("/api/products/{id}")
    @ResponseBody
    public ResponseEntity<?> updateProduct(@PathVariable Long id,
                                           @Valid @RequestBody ProductApiRequest request) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Không tìm thấy sản phẩm với id = " + id));
        }

        Category category = categoryService.getCategoryById(request.getCategoryId());
        if (category == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Danh mục không hợp lệ."));
        }

        mapRequestToProduct(product, request, category);
        Product saved = productService.save(product);

        return ResponseEntity.ok(ProductApiResponse.from(saved));
    }

    @DeleteMapping("/api/products/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Không tìm thấy sản phẩm với id = " + id));
        }

        productService.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Xóa sản phẩm thành công."));
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

    private void mapRequestToProduct(Product product, ProductApiRequest request, Category category) {
        String promotionType = normalizePromotionType(request.getPromotionType());
        String slugSource = (request.getSlug() == null || request.getSlug().isBlank())
                ? request.getName()
                : request.getSlug();

        product.setName(request.getName());
        product.setSlug(makeUniqueSlug(toSlug(slugSource), product.getId()));
        product.setPrice(request.getPrice());
        product.setOriginalPrice(request.getOriginalPrice());
        product.setDescription(request.getDescription());
        product.setThumbnailUrl(
                request.getThumbnailUrl() == null || request.getThumbnailUrl().isBlank()
                        ? "/images/product/default-product.png"
                        : request.getThumbnailUrl()
        );
        product.setPromotionType(promotionType);
        product.setPromotionStock(normalizePromotionStock(promotionType, request.getPromotionStock()));
        product.setRating(request.getRating());
        product.setStockStatus(parseStockStatus(request.getStockStatus()));
        product.setCategory(category);
    }

    private ProductStockStatus parseStockStatus(String stockStatus) {
        if (stockStatus == null || stockStatus.isBlank()) {
            return ProductStockStatus.CON_HANG;
        }
        try {
            return ProductStockStatus.valueOf(stockStatus.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return ProductStockStatus.CON_HANG;
        }
    }

    private String normalizePromotionType(String promotionType) {
        if (promotionType == null || promotionType.isBlank()) {
            return "NONE";
        }
        String normalized = promotionType.trim().toUpperCase();
        Set<String> allowed = Set.of("NONE", "DISCOUNT", "FLASHSALE", "ONLINE_ONLY", "GIFT");
        return allowed.contains(normalized) ? normalized : "NONE";
    }

    private Integer normalizePromotionStock(String promotionType, Integer promotionStock) {
        if ("NONE".equalsIgnoreCase(promotionType)) {
            return null;
        }
        if (promotionStock == null || promotionStock <= 0) {
            return null;
        }
        return promotionStock;
    }

    private String makeUniqueSlug(String baseSlug, Long currentId) {
        String rootSlug = (baseSlug == null || baseSlug.isBlank()) ? "san-pham" : baseSlug;
        String candidate = rootSlug;
        int suffix = 1;

        while (true) {
            Product found = productService.getProductBySlug(candidate);
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