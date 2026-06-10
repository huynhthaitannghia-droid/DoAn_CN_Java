package com.huit.CN_Java.controller;

import com.huit.CN_Java.entity.Role;
import com.huit.CN_Java.entity.User;
import com.huit.CN_Java.repository.OrderRepository;
import com.huit.CN_Java.repository.UserRepository;
import com.huit.CN_Java.service.CategoryService;
import com.huit.CN_Java.service.ProductService;
import com.huit.CN_Java.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final ReviewService reviewService;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("featuredProducts", productService.getFeaturedProducts());
        model.addAttribute("saleProducts", productService.getSaleProducts());
        model.addAttribute("categories", categoryService.getAllActive());
        return "home";
    }

    @GetMapping("/products")
    public String products(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false, defaultValue = "newest") String sortBy,
            Model model) {

        model.addAttribute("products",
                productService.filterProducts(name, categoryId, minPrice, maxPrice, sortBy));
        model.addAttribute("categories", categoryService.getAllActive());
        model.addAttribute("name", name);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sortBy", sortBy);
        return "product/list";
    }

    @GetMapping("/products/{id}")
    public String productDetail(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails userDetails,
                                Model model) {
        model.addAttribute("product", productService.getById(id));
        model.addAttribute("reviews", reviewService.getReviewsByProduct(id));

        if (userDetails != null) {
            User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
            if (user != null) {
                boolean isAdmin = user.getRole() == Role.ADMIN;
                boolean hasPurchased = !isAdmin && orderRepository.hasPurchasedProduct(user.getId(), id);
                boolean alreadyReviewed = !isAdmin && reviewService.getReviewsByProduct(id)
                        .stream().anyMatch(r -> r.getUser().getId().equals(user.getId()));

                model.addAttribute("isAdmin", isAdmin);
                model.addAttribute("canReview", hasPurchased && !alreadyReviewed);
                model.addAttribute("hasPurchased", hasPurchased);
                model.addAttribute("alreadyReviewed", alreadyReviewed);
            }
        }

        return "product/detail";
    }
}
