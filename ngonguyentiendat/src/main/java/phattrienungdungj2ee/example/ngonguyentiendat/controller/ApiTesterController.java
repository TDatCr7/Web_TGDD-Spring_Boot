package phattrienungdungj2ee.example.ngonguyentiendat.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import phattrienungdungj2ee.example.ngonguyentiendat.service.CategoryService;

@Controller
public class ApiTesterController {

    private final CategoryService categoryService;

    public ApiTesterController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/api/tester")
    public String apiTesterPage(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("productCategories", categoryService.getAllCategories());
        model.addAttribute("menuGroupOptions", categoryService.getAccessoryGroupOptions());
        return "admin/apis";
    }
}