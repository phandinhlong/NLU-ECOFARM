package jakarta.ecofarm_backend.infrastructure.service;

import jakarta.ecofarm_backend.common.enums.RoleEnum;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Role;

import java.util.Optional;

public interface RoleService {
    Optional<Role> findByRoleEnum(RoleEnum roleEnum);
}
