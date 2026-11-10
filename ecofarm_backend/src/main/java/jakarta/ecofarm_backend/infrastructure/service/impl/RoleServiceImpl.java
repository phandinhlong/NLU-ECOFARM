package jakarta.ecofarm_backend.infrastructure.service.impl;

import jakarta.ecofarm_backend.common.enums.RoleEnum;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Role;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.RoleRepository;
import jakarta.ecofarm_backend.infrastructure.service.RoleService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Optional;

@ApplicationScoped
public class RoleServiceImpl implements RoleService {
    @Inject
    private RoleRepository roleRepository;

    @Override
    public Optional<Role> findByRoleEnum(RoleEnum roleEnum) {
        return this.roleRepository.findByRoleEnum(roleEnum);
    }
}
