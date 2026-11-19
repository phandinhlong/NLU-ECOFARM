package jakarta.ecofarm_backend.infrastructure.persistence.repository;

import jakarta.ecofarm_backend.common.dto.request.UserCriteria;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findByEmailAndFetchRoles(String email);

    Optional<User> findById(Long id);

    Boolean hasUserByEmail(String email);

    User save(User user);

    Optional<User> findByEmail(String email);

    List<User> findActiveWorkerPage(Long lastIndex, int limit);

    List<User> findPage(UserCriteria criteria);
}
