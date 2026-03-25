package phattrienungdungj2ee.example.ngonguyentiendat.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import phattrienungdungj2ee.example.ngonguyentiendat.model.Category;
import phattrienungdungj2ee.example.ngonguyentiendat.service.CategoryService;
import phattrienungdungj2ee.example.ngonguyentiendat.service.FileStorageService;
import phattrienungdungj2ee.example.ngonguyentiendat.service.ProductService;
import phattrienungdungj2ee.example.ngonguyentiendat.service.importer.ExternalCatalogScraperService;

import java.util.List;

import java.io.IOException;

@Controller
public class CategoryController {

    private final CategoryService categoryService;
    private final ProductService productService;
    private final FileStorageService fileStorageService;
    private final ExternalCatalogScraperService externalCatalogScraperService;

    public CategoryController(CategoryService categoryService,
                              ProductService productService,
                              FileStorageService fileStorageService,
                              ExternalCatalogScraperService externalCatalogScraperService) {
        this.categoryService = categoryService;
        this.productService = productService;
        this.fileStorageService = fileStorageService;
        this.externalCatalogScraperService = externalCatalogScraperService;
    }

    @GetMapping("/category/{slug}")
    public String categoryPage(@PathVariable String slug, Model model) {
        Category category = categoryService.getCategoryBySlug(slug);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("category", category);
        model.addAttribute("products", productService.getProductsByCategory(slug));
        return "category/index";
    }

    @GetMapping("/categories")
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        return "category/list";
    }

    @GetMapping("/categories/add")
    public String addCategoryForm(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("category", new Category());
        return "category/add";
    }

    @PostMapping("/categories/add")
    public String addCategory(@Valid @ModelAttribute("category") Category category,
                              BindingResult result,
                              @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "category/add";
        }

        if (category.getSlug() == null || category.getSlug().isBlank()) {
            category.setSlug(toSlug(category.getName()));
        } else {
            category.setSlug(toSlug(category.getSlug()));
        }

        try {
            String imagePath = fileStorageService.saveCategoryFile(imageFile);
            if (imagePath != null) {
                category.setImageUrl(imagePath);
            }
        } catch (IOException e) {
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("errorMessage", "Upload ảnh danh mục thất bại.");
            return "category/add";
        }

        categoryService.save(category);
        redirectAttributes.addFlashAttribute("successMessage", "Thêm danh mục thành công.");
        return "redirect:/categories";
    }

    @GetMapping("/categories/edit/{id}")
    public String editCategoryForm(@PathVariable Long id, Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("category", categoryService.getCategoryById(id));
        return "category/edit";
    }

    @PostMapping("/categories/edit/{id}")
    public String editCategory(@PathVariable Long id,
                               @Valid @ModelAttribute("category") Category category,
                               BindingResult result,
                               @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "category/edit";
        }

        Category oldCategory = categoryService.getCategoryById(id);
        if (oldCategory != null) {
            oldCategory.setName(category.getName());
            oldCategory.setMenuGroup(category.getMenuGroup());

            if (category.getSlug() == null || category.getSlug().isBlank()) {
                oldCategory.setSlug(toSlug(category.getName()));
            } else {
                oldCategory.setSlug(toSlug(category.getSlug()));
            }

            try {
                String imagePath = fileStorageService.saveCategoryFile(imageFile);
                if (imagePath != null) {
                    oldCategory.setImageUrl(imagePath);
                }
            } catch (IOException e) {
                model.addAttribute("categories", categoryService.getAllCategories());
                model.addAttribute("errorMessage", "Upload ảnh danh mục thất bại.");
                return "category/edit";
            }

            categoryService.save(oldCategory);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật danh mục thành công.");
        }

        return "redirect:/categories";
    }


    @PostMapping("/categories/import-from-url")
    public String importCategoriesFromUrl(@RequestParam("sourceUrl") String sourceUrl,
                                          RedirectAttributes redirectAttributes) {
        try {
            List<?> results = externalCatalogScraperService.importCategoriesFromUrl(sourceUrl);
            long importedCount = results.stream()
                    .map(item -> (phattrienungdungj2ee.example.ngonguyentiendat.dto.importer.ImportedCategoryResult) item)
                    .filter(phattrienungdungj2ee.example.ngonguyentiendat.dto.importer.ImportedCategoryResult::isImported)
                    .count();
            redirectAttributes.addFlashAttribute("categoryImportResults", results);
            redirectAttributes.addFlashAttribute("successMessage", "Đã import " + importedCount + " danh mục từ URL.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cào danh mục thất bại: " + e.getMessage());
        }
        return "redirect:/categories";
    }

    @GetMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa danh mục thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/categories";
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