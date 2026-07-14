package com.baemin_mini.repository;

import com.baemin_mini.domain.entity.Role;
import com.baemin_mini.domain.enums.RoleName;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}
