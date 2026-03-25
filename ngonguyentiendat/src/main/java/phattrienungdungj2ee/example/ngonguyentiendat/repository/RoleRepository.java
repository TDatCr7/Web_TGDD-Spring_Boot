package phattrienungdungj2ee.example.ngonguyentiendat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import phattrienungdungj2ee.example.ngonguyentiendat.model.Role;
import phattrienungdungj2ee.example.ngonguyentiendat.model.RoleName;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}
