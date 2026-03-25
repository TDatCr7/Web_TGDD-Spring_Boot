package phattrienungdungj2ee.example.ngonguyentiendat.dto.api;

import phattrienungdungj2ee.example.ngonguyentiendat.model.Category;

public class CategoryApiResponse {

    private Long id;
    private String name;
    private String slug;
    private String menuGroup;
    private String imageUrl;

    public static CategoryApiResponse from(Category category) {
        CategoryApiResponse response = new CategoryApiResponse();
        response.id = category.getId();
        response.name = category.getName();
        response.slug = category.getSlug();
        response.menuGroup = category.getMenuGroup();
        response.imageUrl = category.getImageUrl();
        return response;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public String getMenuGroup() {
        return menuGroup;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}