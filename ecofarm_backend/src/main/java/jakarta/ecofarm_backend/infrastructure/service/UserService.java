package jakarta.ecofarm_backend.infrastructure.service;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.User;

import java.util.Optional;

public interface UserService {
    Optional<User> findByEmailAndFetchRoles(String email);
    Optional<User> findById(Long id);
    Boolean hasUserByEmail(String email);
}
