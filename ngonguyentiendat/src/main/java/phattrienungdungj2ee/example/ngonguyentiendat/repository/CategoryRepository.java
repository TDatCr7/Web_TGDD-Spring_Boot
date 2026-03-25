package phattrienungdungj2ee.example.ngonguyentiendat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import phattrienungdungj2ee.example.ngonguyentiendat.model.Category;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findBySlug(String slug);
    boolean existsBySlug(String slug);
    boolean existsByName(String name);
}