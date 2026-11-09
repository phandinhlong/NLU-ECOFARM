package jakarta.ecofarm_backend.infrastructure.persistence.repository.impl;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.User;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.Optional;

@ApplicationScoped
public class UserRepositoryImpl implements UserRepository {

    @PersistenceContext
    @jakarta.inject.Inject
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
}
