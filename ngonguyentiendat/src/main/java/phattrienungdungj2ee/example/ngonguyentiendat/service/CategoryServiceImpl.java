package phattrienungdungj2ee.example.ngonguyentiendat.service;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import phattrienungdungj2ee.example.ngonguyentiendat.model.Category;
import phattrienungdungj2ee.example.ngonguyentiendat.repository.CategoryRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    @Override
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }

    @Override
    public Category getCategoryBySlug(String slug) {
        return categoryRepository.findBySlug(slug).orElse(null);
    }

    @Override
    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    public void deleteById(Long id) {
        Category category = categoryRepository.findById(id).orElse(null);
        if (category == null) {
            return;
        }

        if (category.getProducts() != null && !category.getProducts().isEmpty()) {
            throw new RuntimeException("Danh mục đang chứa sản phẩm, không thể xóa.");
        }

        categoryRepository.delete(category);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return slug != null && categoryRepository.existsBySlug(slug);
    }

    @Override
    public boolean existsByName(String name) {
        return name != null && categoryRepository.existsByName(name);
    }

    @Override
    public LinkedHashMap<String, String> getAccessoryGroupOptions() {
        LinkedHashMap<String, String> options = new LinkedHashMap<>();
        options.put("PHU_KIEN_DI_DONG", "Phụ kiện di động");
        options.put("PHU_KIEN_LAPTOP_PC", "Phụ kiện laptop, PC");
        options.put("THIET_BI_AM_THANH", "Thiết bị âm thanh");
        options.put("CAMERA", "Camera");
        options.put("PHU_KIEN_GAMING", "Phụ kiện gaming");
        options.put("THIET_BI_LUU_TRU", "Thiết bị lưu trữ");
        options.put("PHU_KIEN_KHAC", "Phụ kiện khác");
        options.put("THUONG_HIEU_HANG_DAU", "Thương hiệu hàng đầu");
        return options;
    }

    @Override
    public Map<String, List<Category>> getAccessoryCategoriesByGroup() {
        LinkedHashMap<String, List<Category>> groupedCategories = new LinkedHashMap<>();
        LinkedHashMap<String, String> groupOptions = getAccessoryGroupOptions();

        for (String groupKey : groupOptions.keySet()) {
            groupedCategories.put(groupKey, new ArrayList<>());
        }

        for (Category category : getAllCategories()) {
            if (category.getMenuGroup() == null || category.getMenuGroup().isBlank()) {
                continue;
            }

            List<Category> categories = groupedCategories.computeIfAbsent(
                    category.getMenuGroup(),
                    key -> new ArrayList<>()
            );
            categories.add(category);
        }

        groupedCategories.entrySet().removeIf(entry -> entry.getValue() == null || entry.getValue().isEmpty());
        return groupedCategories;
    }
}