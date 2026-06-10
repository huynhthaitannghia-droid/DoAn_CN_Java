package com.huit.CN_Java.service;

import com.huit.CN_Java.entity.Category;
import com.huit.CN_Java.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> getAllActive() {
        return categoryRepository.findByActiveTrue();
    }

    public Category getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));
    }

    // --- CÁC HÀM BỔ SUNG CHO ADMIN TỪ BƯỚC 4 ---

    public List<Category> findAllWithProductCount() {
        List<Category> cats = categoryRepository.findAll();
        cats.forEach(c -> c.setProductCount(
                (long) c.getProducts().size()
        ));
        return cats;
    }

    public boolean existsByName(String name) {
        return categoryRepository.existsByNameIgnoreCase(name);
    }

    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    public Category findByIdOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));
    }

    public void deleteById(Long id) {
        categoryRepository.deleteById(id);
    }
}
