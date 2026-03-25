package phattrienungdungj2ee.example.ngonguyentiendat.service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import phattrienungdungj2ee.example.ngonguyentiendat.config.SecurityProperties;
import phattrienungdungj2ee.example.ngonguyentiendat.model.Role;
import phattrienungdungj2ee.example.ngonguyentiendat.model.RoleName;
import phattrienungdungj2ee.example.ngonguyentiendat.repository.RoleRepository;

import java.util.Set;

@Component
public class SecurityDataSeeder implements CommandLineRunner {
    private final RoleRepository roleRepository;
    private final UserAccountService userAccountService;
    private final SecurityProperties securityProperties;

    public SecurityDataSeeder(RoleRepository roleRepository, UserAccountService userAccountService, SecurityProperties securityProperties) {
        this.roleRepository = roleRepository;
        this.userAccountService = userAccountService;
        this.securityProperties = securityProperties;
    }

    @Override
    public void run(String... args) {
        ensureRole(RoleName.ROLE_ADMIN);
        ensureRole(RoleName.ROLE_MANAGER);
        ensureRole(RoleName.ROLE_USER);

        if (userAccountService.getByEmail(securityProperties.getDefaultAdminEmail()) == null) {
            userAccountService.createUserWithRoles("Admin", securityProperties.getDefaultAdminEmail(), null,
                    securityProperties.getDefaultAdminPassword(), 0, Set.of(RoleName.ROLE_ADMIN));
        }
        if (userAccountService.getByEmail(securityProperties.getDefaultManagerEmail()) == null) {
            userAccountService.createUserWithRoles("Manager", securityProperties.getDefaultManagerEmail(), null,
                    securityProperties.getDefaultManagerPassword(), 0, Set.of(RoleName.ROLE_MANAGER));
        }
        if (userAccountService.getByEmail(securityProperties.getDefaultUserEmail()) == null) {
            userAccountService.createUserWithRoles("User", securityProperties.getDefaultUserEmail(), null,
                    securityProperties.getDefaultUserPassword(), 20, Set.of(RoleName.ROLE_USER));
        }
    }

    private void ensureRole(RoleName roleName) {
        roleRepository.findByName(roleName).orElseGet(() -> roleRepository.save(new Role(roleName)));
    }
}
