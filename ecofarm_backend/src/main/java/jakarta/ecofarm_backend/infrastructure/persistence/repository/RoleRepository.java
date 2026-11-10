package jakarta.ecofarm_backend.infrastructure.persistence.repository;

import jakarta.ecofarm_backend.common.enums.RoleEnum;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Role;

import java.util.Optional;

public interface RoleRepository {

    Optional<Role> findByRoleEnum(RoleEnum roleEnum);
}
