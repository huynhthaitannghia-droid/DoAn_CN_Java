package com.huit.CN_Java.controller.admin;

import com.huit.CN_Java.entity.Order;
import com.huit.CN_Java.service.OrderService;
import com.huit.CN_Java.service.ProductService;
import com.huit.CN_Java.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final OrderService orderService;
    private final ProductService productService;
    private final UserService userService;

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model) {
        long pending = orderService.countByStatus(Order.OrderStatus.PENDING);
        long confirmed = orderService.countByStatus(Order.OrderStatus.CONFIRMED);
        long shipping = orderService.countByStatus(Order.OrderStatus.SHIPPING);
        long completed = orderService.countByStatus(Order.OrderStatus.COMPLETED);
        long cancelled = orderService.countByStatus(Order.OrderStatus.CANCELLED);

        Map<String, Double> revenueByDay = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            revenueByDay.put(date.toString(), orderService.getRevenueByDay(date));
        }

        model.addAttribute("totalRevenue", orderService.getTotalRevenue());
        model.addAttribute("totalOrders", orderService.countAllOrders());
        model.addAttribute("pendingOrders", pending);
        model.addAttribute("processingOrders", confirmed + shipping);
        model.addAttribute("completedOrders", completed);
        model.addAttribute("cancelledOrders", cancelled);
        model.addAttribute("pendingCount", pending);
        model.addAttribute("totalProducts", productService.searchAdmin("", null, PageRequest.of(0, 1)).getTotalElements());
        model.addAttribute("totalUsers", userService.countAllUsers());
        model.addAttribute("recentOrders", orderService.getRecentOrders(PageRequest.of(0, 5)));
        model.addAttribute("lowStockProducts", productService.getLowStockProducts(10));
        model.addAttribute("revenueByDay", revenueByDay);
        return "admin/dashboard";
    }
}
