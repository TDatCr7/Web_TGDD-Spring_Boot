package phattrienungdungj2ee.example.ngonguyentiendat.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import phattrienungdungj2ee.example.ngonguyentiendat.model.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderCode(String orderCode);

    @EntityGraph(attributePaths = {"orderDetails"})
    Optional<Order> findWithOrderDetailsById(Long id);

    @Override
    @EntityGraph(attributePaths = {"orderDetails"})
    List<Order> findAll();
}