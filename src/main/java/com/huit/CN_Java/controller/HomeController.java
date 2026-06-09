package com.huit.CN_Java.controller;

import com.huit.CN_Java.service.CategoryService;
import com.huit.CN_Java.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductService productService;
    private final CategoryService categoryService;

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
    public String productDetail(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.getById(id));
        return "product/detail";
    }
}