package phattrienungdungj2ee.example.ngonguyentiendat.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import phattrienungdungj2ee.example.ngonguyentiendat.service.CategoryService;

@Controller
public class AdminController {

    private final CategoryService categoryService;

    public AdminController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/admin")
    public String adminHome(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/index";
    }
}