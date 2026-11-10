package jakarta.ecofarm_backend.infrastructure.persistence.repository.impl;

import jakarta.ecofarm_backend.common.enums.RoleEnum;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Role;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.RoleRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.Optional;

@ApplicationScoped
public class RoleRepositoryImpl implements RoleRepository {
    @PersistenceContext
    @jakarta.inject.Inject
    private EntityManager entityManager;


    @Override
    public Optional<Role> findByRoleEnum(RoleEnum roleEnum) {
        return this.entityManager.createQuery(
                        """
                        SELECT r
                        FROM Role r
                        WHERE r.roleName = :roleName
                        """, Role.class)
                .setParameter("roleName", roleEnum)
                .getResultStream()
                .findFirst();
    }
}
