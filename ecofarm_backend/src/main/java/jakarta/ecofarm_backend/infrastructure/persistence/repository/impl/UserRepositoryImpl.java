package jakarta.ecofarm_backend.infrastructure.persistence.repository.impl;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.User;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.UserRepository;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

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
}
