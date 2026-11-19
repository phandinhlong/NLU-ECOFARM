package jakarta.ecofarm_backend.infrastructure.persistence.repository.impl;

import jakarta.ecofarm_backend.common.dto.request.BaseCriteria;
import jakarta.ecofarm_backend.common.dto.request.UserCriteria;
import jakarta.ecofarm_backend.common.enums.RoleEnum;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.User;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.UserRepository;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

@Stateless
public class UserRepositoryImpl implements UserRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public Optional<User> findByEmailAndFetchRoles(String email) {
        return this.entityManager.createQuery(
                        """
                        SELECT DISTINCT u
                        FROM User u
                        LEFT JOIN FETCH u.roles
                        WHERE u.email = :email
                        """, User.class)
                .setParameter("email", email)
                .getResultStream()
                .findFirst();
    }

    @Override
    public Optional<User> findById(Long id) {
        return this.entityManager.createQuery(
                        """
                        SELECT  u
                        FROM User u
                        WHERE u.id =:id
                        """, User.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }

    @Override
    public Boolean hasUserByEmail(String email) {
        Long count = this.entityManager.createQuery(
                        """
                        SELECT COUNT(u)
                        FROM User u
                        WHERE LOWER(u.email) = LOWER(:email)
                        """, Long.class)
                .setParameter("email", email)
                .getSingleResult();

        return count > 0;
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            this.entityManager.persist(user);
            return user;
        }
        return this.entityManager.merge(user);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return this.entityManager.createQuery(
                        """
                        SELECT u
                        FROM User u
                        WHERE u.email = :email
                        """, User.class)
                .setParameter("email", email)
                .getResultStream()
                .findFirst();
    }

    @Override
    public List<User> findActiveWorkerPage(Long lastIndex, int limit) {
        String jpql = """
            SELECT DISTINCT u
            FROM User u
            LEFT JOIN FETCH u.roles
            WHERE u.isActive = true
              AND EXISTS (SELECT r FROM Role r WHERE r MEMBER OF u.roles AND r.roleName = :role)
              AND (:lastIndex IS NULL OR u.id < :lastIndex)
            ORDER BY u.id DESC
        """;
        TypedQuery<User> query = this.entityManager.createQuery(jpql, User.class)
                .setParameter("role", RoleEnum.FARMER)
                .setParameter("lastIndex", lastIndex);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    @Override
    public List<User> findPage(UserCriteria criteria) {
        StringBuilder jpql = new StringBuilder("""
                SELECT DISTINCT u FROM User u
                LEFT JOIN FETCH u.roles
                WHERE u.isActive = true
                """);

        RoleEnum role = criteria.getRoleFilter().map(this::parseRole).orElse(null);
        if (role != null) {
            jpql.append(" AND EXISTS (SELECT r FROM Role r WHERE r MEMBER OF u.roles AND r.roleName = :role)");
        }
        criteria.getKeywordFilter().ifPresent(value ->
                jpql.append(" AND (LOWER(u.fullName) LIKE :keyword OR LOWER(u.email) LIKE :keyword)"));
        jpql.append(" AND (:lastId IS NULL OR u.id < :lastId) ORDER BY u.id DESC");

        TypedQuery<User> query = this.entityManager.createQuery(jpql.toString(), User.class);
        if (role != null) {
            query.setParameter("role", role);
        }
        criteria.getKeywordFilter().ifPresent(value ->
                query.setParameter("keyword", "%" + value.trim().toLowerCase() + "%"));
        query.setParameter("lastId", criteria.getLastIdFilter().orElse(null));
        query.setMaxResults(BaseCriteria.LENGTH + 1);
        return query.getResultList();
    }

    private RoleEnum parseRole(String value) {
        try {
            return RoleEnum.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
