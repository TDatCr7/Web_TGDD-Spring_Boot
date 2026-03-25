package phattrienungdungj2ee.example.ngonguyentiendat.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import phattrienungdungj2ee.example.ngonguyentiendat.model.Product;
import phattrienungdungj2ee.example.ngonguyentiendat.repository.ProductRepository;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<Product> getHomeProducts() {
        return productRepository.findTop12ByOrderByIdDesc();
    }

    @Override
    public Page<Product> getProductPage(Pageable pageable) {
        return productRepository.findAllByOrderByIdDesc(pageable);
    }

    @Override
    public List<Product> getProductsByCategory(String categorySlug) {
        return productRepository.findByCategorySlug(categorySlug);
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAllByOrderByIdDesc();
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    @Override
    public Product getProductBySlug(String slug) {
        return productRepository.findBySlug(slug).orElse(null);
    }

    @Override
    public Product save(Product product) {
        return productRepository.save(product);
    }

    @Override
    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }

    @Override
    public List<Product> getRelatedProducts(Product product) {
        if (product == null || product.getCategory() == null) {
            return List.of();
        }
        return productRepository.findTop8ByCategoryIdAndIdNotOrderByIdDesc(
                product.getCategory().getId(),
                product.getId()
        );
    }
}