package phattrienungdungj2ee.example.ngonguyentiendat.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import phattrienungdungj2ee.example.ngonguyentiendat.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findTop12ByOrderByIdDesc();

    List<Product> findTop8ByOrderByIdDesc();

    Optional<Product> findBySlug(String slug);
    List<Product> findAllByOrderByIdDesc();

    List<Product> findByCategorySlug(String slug);

    List<Product> findTop8ByCategoryIdAndIdNotOrderByIdDesc(Long categoryId, Long id);

    Page<Product> findAllByOrderByIdDesc(Pageable pageable);

    boolean existsBySlug(String slug);

    boolean existsByNameIgnoreCase(String name);

    boolean existsBySlugStartingWith(String slugPrefix);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);
}