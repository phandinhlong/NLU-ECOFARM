package jakarta.ecofarm_backend.infrastructure.service.impl;

import jakarta.ecofarm_backend.common.dto.request.UserCriteria;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.User;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.UserRepository;
import jakarta.ecofarm_backend.infrastructure.service.UserService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class UserServiceImpl implements UserService {
    @Inject
    private UserRepository userRepository;

    @Override
    public Optional<User> findByEmailAndFetchRoles(String email) {
        return this.userRepository.findByEmailAndFetchRoles(email);
    }

    @Override
    public Optional<User> findById(Long id) {
        return this.userRepository.findById(id);
    }

    @Override
    public Boolean hasUserByEmail(String email) {
        return this.userRepository.hasUserByEmail(email);
    }

    @Override
    public User save(User user) {
        return this.userRepository.save(user);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return this.userRepository.findByEmail(email);
    }

    @Override
    public List<User> findActiveWorkerPage(Long lastIndex, int limit) {
        return this.userRepository.findActiveWorkerPage(lastIndex, limit);
    }

    @Override
    public List<User> findPage(UserCriteria criteria) {
        return this.userRepository.findPage(criteria);
    }
}
