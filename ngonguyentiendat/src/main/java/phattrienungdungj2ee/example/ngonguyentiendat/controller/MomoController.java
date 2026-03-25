package phattrienungdungj2ee.example.ngonguyentiendat.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import phattrienungdungj2ee.example.ngonguyentiendat.model.Order;
import phattrienungdungj2ee.example.ngonguyentiendat.service.MomoService;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/momo")
public class MomoController {

    private final MomoService momoService;

    public MomoController(MomoService momoService) {
        this.momoService = momoService;
    }

    @GetMapping("/return")
    public String handleReturn(HttpServletRequest request,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Map<String, String> params = extractParams(request);
        Order order = momoService.handleReturn(params, session);

        if (order != null) {
            redirectAttributes.addFlashAttribute("successMessage", "Thanh toán MoMo thành công.");
            return "redirect:/orders/confirmation/" + order.getId();
        }

        redirectAttributes.addFlashAttribute(
                "errorMessage",
                "Thanh toán MoMo thất bại hoặc đã bị hủy."
        );
        return "redirect:/checkout?paymentMethod=MOMO";
    }

    @PostMapping("/ipn")
    @ResponseBody
    public ResponseEntity<Void> handleIpn(@RequestBody Map<String, Object> body) {
        Map<String, String> params = new HashMap<>();
        if (body != null) {
            body.forEach((key, value) -> params.put(key, value == null ? "" : String.valueOf(value)));
        }

        momoService.handleIpn(params);
        return ResponseEntity.noContent().build();
    }

    private Map<String, String> extractParams(HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();
        request.getParameterMap().forEach((key, value) -> {
            if (value != null && value.length > 0) {
                map.put(key, value[0]);
            }
        });
        return map;
    }
}