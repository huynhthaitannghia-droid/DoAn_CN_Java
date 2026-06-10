package com.huit.CN_Java.controller.admin;

import com.huit.CN_Java.entity.Category;
import com.huit.CN_Java.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.Normalizer;
import java.util.Locale;

@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("categories", categoryService.findAllWithProductCount());
        model.addAttribute("newCategory", new Category());
        return "admin/category/list";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("newCategory") Category category,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.findAllWithProductCount());
            return "admin/category/list";
        }
        ensureSlug(category);
        categoryService.save(category);
        redirectAttributes.addFlashAttribute("successMsg", "Da them danh muc");
        return "redirect:/admin/categories";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("category", categoryService.findByIdOrThrow(id));
        return "admin/category/edit";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                       @Valid @ModelAttribute Category form,
                       BindingResult result,
                       RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/category/edit";
        }
        Category category = categoryService.findByIdOrThrow(id);
        category.setName(form.getName());
        category.setDescription(form.getDescription());
        category.setSlug(form.getSlug());
        category.setSortOrder(form.getSortOrder());
        ensureSlug(category);
        categoryService.save(category);
        redirectAttributes.addFlashAttribute("successMsg", "Da cap nhat danh muc");
        return "redirect:/admin/categories";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        categoryService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMsg", "Da xoa danh muc");
        return "redirect:/admin/categories";
    }

    private void ensureSlug(Category category) {
        if (category.getSlug() == null || category.getSlug().isBlank()) {
            String slug = Normalizer.normalize(category.getName(), Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "")
                    .replace('đ', 'd')
                    .replace('Đ', 'D')
                    .toLowerCase(Locale.ROOT)
                    .replaceAll("[^a-z0-9]+", "-")
                    .replaceAll("(^-|-$)", "");
            category.setSlug(slug);
        }
    }
}
