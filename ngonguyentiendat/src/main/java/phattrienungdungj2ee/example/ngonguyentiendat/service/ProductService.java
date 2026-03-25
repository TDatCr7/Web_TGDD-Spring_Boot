package phattrienungdungj2ee.example.ngonguyentiendat.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import phattrienungdungj2ee.example.ngonguyentiendat.model.Product;

import java.util.List;

public interface ProductService {
    List<Product> getHomeProducts();
    Page<Product> getProductPage(Pageable pageable);
    List<Product> getProductsByCategory(String categorySlug);
    Product getProductById(Long id);
    Product getProductBySlug(String slug);
    Product save(Product product);
    void deleteById(Long id);
    List<Product> getRelatedProducts(Product product);
    List<Product> getAllProducts();

}