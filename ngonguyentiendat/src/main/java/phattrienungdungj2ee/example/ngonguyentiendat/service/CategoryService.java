package phattrienungdungj2ee.example.ngonguyentiendat.service;

import phattrienungdungj2ee.example.ngonguyentiendat.model.Category;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface CategoryService {
    List<Category> getAllCategories();
    Category getCategoryById(Long id);
    Category getCategoryBySlug(String slug);
    Category save(Category category);
    void deleteById(Long id);

    boolean existsBySlug(String slug);
    boolean existsByName(String name);

    LinkedHashMap<String, String> getAccessoryGroupOptions();
    Map<String, List<Category>> getAccessoryCategoriesByGroup();
}