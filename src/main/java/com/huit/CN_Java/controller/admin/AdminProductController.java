package com.huit.CN_Java.controller.admin;

import com.huit.CN_Java.entity.Category;
import com.huit.CN_Java.entity.Product;
import com.huit.CN_Java.service.CategoryService;
import com.huit.CN_Java.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private static final Path UPLOAD_DIR = Path.of("src/main/resources/static/uploads/products");

    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String keyword,
                       @RequestParam(required = false) Long categoryId,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "id"));
        model.addAttribute("products", productService.searchAdmin(keyword, categoryId, pageable));
        model.addAttribute("categories", categoryService.getAllActive());
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("currentPage", page);
        return "admin/product/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        Product product = new Product();
        product.setCategory(new Category());
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllActive());
        return "admin/product/form";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute Product product,
                         BindingResult result,
                         @RequestParam(required = false) List<MultipartFile> imageFiles,
                         @RequestParam(defaultValue = "0") int primaryImageIndex,
                         Model model,
                         RedirectAttributes redirectAttributes) throws IOException {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllActive());
            return "admin/product/form";
        }
        normalizeCategory(product);
        Product saved = productService.save(product);
        productService.saveImages(saved, storeImages(imageFiles), primaryImageIndex);
        redirectAttributes.addFlashAttribute("successMsg", "Đã thêm sản phẩm");
        return "redirect:/admin/products";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Product product = productService.findByIdOrThrow(id);
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllActive());
        model.addAttribute("existingImages", product.getImages());
        return "admin/product/form";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                       @Valid @ModelAttribute Product form,
                       BindingResult result,
                       @RequestParam(required = false) List<MultipartFile> imageFiles,
                       @RequestParam(required = false) List<Long> deleteImageIds,
                       @RequestParam(defaultValue = "0") int primaryImageIndex,
                       @RequestParam(required = false) Long primaryExistingImageId,
                       Model model,
                       RedirectAttributes redirectAttributes) throws IOException {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllActive());
            model.addAttribute("existingImages", productService.findByIdOrThrow(id).getImages());
            return "admin/product/form";
        }

        Product product = productService.findByIdOrThrow(id);
        product.setName(form.getName());
        product.setDescription(form.getDescription());
        product.setPrice(form.getPrice());
        product.setSalePrice(form.getSalePrice());
        product.setStock(form.getStock());
        product.setActive(form.isActive());
        product.setFeatured(form.isFeatured());
        product.setCategory(categoryService.findByIdOrThrow(form.getCategory().getId()));
        productService.save(product);
        productService.deleteImagesByIds(deleteImageIds);

        // Nếu admin chọn ảnh chính từ ảnh cũ còn lại
        if (primaryExistingImageId != null) {
            productService.setPrimaryImage(product.getId(), primaryExistingImageId);
        }

        // Lưu ảnh mới, ảnh đầu tiên upload là primary nếu không có ảnh cũ được chọn
        List<String> newPaths = storeImages(imageFiles);
        if (!newPaths.isEmpty()) {
            productService.saveImages(product, newPaths, primaryImageIndex);
            // Nếu có ảnh mới và chưa chọn ảnh cũ làm primary → ảnh mới được chọn làm primary
            if (primaryExistingImageId == null) {
                productService.clearOtherPrimary(product.getId(), null);
            }
        }

        redirectAttributes.addFlashAttribute("successMsg", "Đã cập nhật sản phẩm");
        return "redirect:/admin/products";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productService.toggleActive(id);
        redirectAttributes.addFlashAttribute("successMsg", "Đã cập nhật trạng thái sản phẩm");
        return "redirect:/admin/products";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMsg", "Đã xóa sản phẩm");
        return "redirect:/admin/products";
    }

    private void normalizeCategory(Product product) {
        Category category = product.getCategory();
        if (category != null && category.getId() != null) {
            product.setCategory(categoryService.findByIdOrThrow(category.getId()));
        }
    }

    private List<String> storeImages(List<MultipartFile> files) throws IOException {
        List<String> paths = new ArrayList<>();
        if (files == null) {
            return paths;
        }
        Files.createDirectories(UPLOAD_DIR);
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
            String extension = original.contains(".") ? original.substring(original.lastIndexOf('.')) : "";
            String filename = UUID.randomUUID() + extension;
            Files.copy(file.getInputStream(), UPLOAD_DIR.resolve(filename));
            paths.add("/uploads/products/" + filename);
        }
        return paths;
    }
}