package com.huit.CN_Java.controller;

import com.huit.CN_Java.entity.Product;
import com.huit.CN_Java.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class QuickViewController {

    private final ProductService productService;

    /**
     * Trả thông tin sản phẩm dạng JSON cho modal Quick View.
     * GET /api/products/{id}/quickview
     */
    @GetMapping("/{id}/quickview")
    public ResponseEntity<Map<String, Object>> quickView(@PathVariable Long id) {
        Product p = productService.getById(id);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id",          p.getId());
        data.put("name",        p.getName());
        data.put("description", p.getDescription());
        data.put("price",       p.getPrice());
        data.put("salePrice",   p.getSalePrice());
        data.put("displayPrice",p.getDisplayPrice());
        data.put("stock",       p.getStock());
        data.put("thumbnail",   p.getThumbnail());
        data.put("category",    p.getCategory() != null ? p.getCategory().getName() : "");

        return ResponseEntity.ok(data);
    }
}