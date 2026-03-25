package phattrienungdungj2ee.example.ngonguyentiendat.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import phattrienungdungj2ee.example.ngonguyentiendat.dto.ProductForm;
import phattrienungdungj2ee.example.ngonguyentiendat.model.Category;
import phattrienungdungj2ee.example.ngonguyentiendat.model.Product;
import phattrienungdungj2ee.example.ngonguyentiendat.service.CategoryService;
import phattrienungdungj2ee.example.ngonguyentiendat.service.FileStorageService;
import phattrienungdungj2ee.example.ngonguyentiendat.service.ProductService;
import phattrienungdungj2ee.example.ngonguyentiendat.service.importer.ExternalCatalogScraperService;

import java.io.IOException;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Controller
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final FileStorageService fileStorageService;
    private final ExternalCatalogScraperService externalCatalogScraperService;

    public ProductController(ProductService productService,
                             CategoryService categoryService,
                             FileStorageService fileStorageService,
                             ExternalCatalogScraperService externalCatalogScraperService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.fileStorageService = fileStorageService;
        this.externalCatalogScraperService = externalCatalogScraperService;
    }

    @GetMapping("/product/{slug}")
    public String productDetail(@PathVariable String slug, Model model) {
        Product product = productService.getProductBySlug(slug);
        if (product == null) {
            return "redirect:/";
        }

        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("product", product);
        model.addAttribute("relatedProducts", productService.getRelatedProducts(product));
        return "product/detail";
    }

    @GetMapping("/products")
    public String listProducts(@RequestParam(defaultValue = "0") int page, Model model) {
        Pageable pageable = PageRequest.of(page, 8);
        Page<Product> productPage = productService.getProductPage(pageable);

        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("targetCategories", categoryService.getAllCategories());
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("productPage", productPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());

        return "product/list";
    }


    @PostMapping("/products/import-from-url")
    public String importProductsFromUrl(@RequestParam("sourceUrl") String sourceUrl,
                                        @RequestParam("targetCategoryId") Long targetCategoryId,
                                        @RequestParam(value = "limit", defaultValue = "24") int limit,
                                        RedirectAttributes redirectAttributes) {
        try {
            List<?> results = externalCatalogScraperService.importProductsFromCategoryUrl(sourceUrl, targetCategoryId, limit);
            long importedCount = results.stream()
                    .map(item -> (phattrienungdungj2ee.example.ngonguyentiendat.dto.importer.ImportedProductResult) item)
                    .filter(phattrienungdungj2ee.example.ngonguyentiendat.dto.importer.ImportedProductResult::isImported)
                    .count();
            redirectAttributes.addFlashAttribute("productImportResults", results);
            redirectAttributes.addFlashAttribute("successMessage", "Đã import " + importedCount + " sản phẩm từ URL.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cào sản phẩm thất bại: " + e.getMessage());
        }
        return "redirect:/products";
    }

    @GetMapping("/products/add")
    public String addForm(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("productForm", new ProductForm());
        return "product/add";
    }

    @PostMapping("/products/upload-base64")
    @ResponseBody
    public ResponseEntity<?> uploadBase64Image(@RequestBody Map<String, String> payload) {
        try {
            String imageBase64 = payload.get("imageBase64");
            String imagePath = fileStorageService.saveProductBase64(imageBase64);

            Map<String, String> response = new HashMap<>();
            response.put("imagePath", imagePath);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Tải ảnh thất bại.");
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/products/add")
    public String addProduct(@Valid @ModelAttribute("productForm") ProductForm form,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes) {

        validateForm(form, result);

        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "product/add";
        }

        Category category = categoryService.getCategoryById(form.getCategoryId());
        if (category == null) {
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("errorMessage", "Danh mục không hợp lệ.");
            return "product/add";
        }

        Product product = new Product();
        mapFormToProduct(form, product, category);

        if (form.getUploadedImagePath() != null && !form.getUploadedImagePath().isBlank()) {
            product.setThumbnailUrl(form.getUploadedImagePath());
        } else {
            product.setThumbnailUrl("/images/product/default-product.png");
        }

        productService.save(product);
        redirectAttributes.addFlashAttribute("successMessage", "Thêm sản phẩm thành công.");
        return "redirect:/products";
    }

    @GetMapping("/products/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return "redirect:/products";
        }

        ProductForm form = new ProductForm();
        form.setName(product.getName());
        form.setSlug(product.getSlug());
        form.setOriginalPrice(product.getOriginalPrice());
        form.setPrice(product.getPrice());
        form.setDescription(product.getDescription());
        form.setCategoryId(product.getCategory() != null ? product.getCategory().getId() : null);
        form.setCurrentThumbnailUrl(product.getThumbnailUrl());
        form.setPromotionType(product.getPromotionType());
        form.setPromotionStock(product.getPromotionStock());
        form.setStockStatus(product.getStockStatus());
        form.setRating(product.getRating());

        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("product", product);
        model.addAttribute("productForm", form);
        return "product/edit";
    }

    @PostMapping("/products/edit/{id}")
    public String editProduct(@PathVariable Long id,
                              @Valid @ModelAttribute("productForm") ProductForm form,
                              BindingResult result,
                              Model model,
                              RedirectAttributes redirectAttributes) {

        Product oldProduct = productService.getProductById(id);
        if (oldProduct == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy sản phẩm.");
            return "redirect:/products";
        }

        validateForm(form, result);

        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("product", oldProduct);
            return "product/edit";
        }

        Category category = categoryService.getCategoryById(form.getCategoryId());
        if (category == null) {
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("product", oldProduct);
            model.addAttribute("errorMessage", "Danh mục không hợp lệ.");
            return "product/edit";
        }

        mapFormToProduct(form, oldProduct, category);

        if (form.getUploadedImagePath() != null && !form.getUploadedImagePath().isBlank()) {
            oldProduct.setThumbnailUrl(form.getUploadedImagePath());
        }

        productService.save(oldProduct);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sản phẩm thành công.");
        return "redirect:/products";
    }

    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa sản phẩm thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa sản phẩm.");
        }
        return "redirect:/products";
    }

    private void validateForm(ProductForm form, BindingResult result) {
        if (form.getOriginalPrice() != null && form.getPrice() != null
                && form.getPrice().compareTo(form.getOriginalPrice()) > 0) {
            result.rejectValue("price", "error.productForm", "Giá bán không được lớn hơn giá gốc.");
        }

        String promotionType = normalizePromotionType(form.getPromotionType());

        if (!"NONE".equals(promotionType)) {
            if (form.getPromotionStock() == null) {
                result.rejectValue("promotionStock", "error.productForm", "Vui lòng nhập số lượng suất khuyến mãi.");
            } else if (form.getPromotionStock() <= 0) {
                result.rejectValue("promotionStock", "error.productForm", "Số lượng suất khuyến mãi phải lớn hơn 0.");
            }
        }
    }

    private void mapFormToProduct(ProductForm form, Product product, Category category) {
        String promotionType = normalizePromotionType(form.getPromotionType());
        Integer promotionStock = normalizePromotionStock(promotionType, form.getPromotionStock());

        product.setName(form.getName());
        product.setSlug(toSlug(form.getSlug() == null || form.getSlug().isBlank() ? form.getName() : form.getSlug()));
        product.setOriginalPrice(form.getOriginalPrice());
        product.setPrice(form.getPrice());
        product.setDescription(form.getDescription());
        product.setPromotionType(promotionType);
        product.setPromotionStock(promotionStock);
        product.setStockStatus(form.getStockStatus());
        product.setRating(form.getRating() == null ? null : form.getRating());
        product.setCategory(category);
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
        return promotionStock;
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
                .replaceAll("-+", "-");
    }
}