package jakarta.ecofarm_backend.infrastructure.persistence.repository;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.User;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findByEmailAndFetchRoles(String email);
    Optional<User> findById(Long id);
    Boolean hasUserByEmail(String email);
}
