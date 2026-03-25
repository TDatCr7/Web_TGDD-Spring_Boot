package phattrienungdungj2ee.example.ngonguyentiendat.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import phattrienungdungj2ee.example.ngonguyentiendat.model.Order;
import phattrienungdungj2ee.example.ngonguyentiendat.service.CategoryService;
import phattrienungdungj2ee.example.ngonguyentiendat.service.OrderService;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;
    private final CategoryService categoryService;

    public AdminOrderController(OrderService orderService, CategoryService categoryService) {
        this.orderService = orderService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String orderList(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("orders", orderService.getAllOrders());
        return "admin/order-list";
    }

    @GetMapping("/{id}")
    public String orderDetail(@PathVariable Long id, Model model) {
        Order order = orderService.getOrderForView(id);
        if (order == null) {
            return "redirect:/admin/orders";
        }

        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("order", order);
        return "admin/order-detail";
    }
}