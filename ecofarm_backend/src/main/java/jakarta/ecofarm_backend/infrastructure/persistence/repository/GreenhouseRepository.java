package jakarta.ecofarm_backend.infrastructure.persistence.repository;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.Greenhouse;

import java.util.List;

public interface GreenhouseRepository {

    List<Greenhouse> findPage(int page, int size, String status, String q);

    long countActive(String status, String q);

    Greenhouse findById(Long id);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    long countBatches(Long greenhouseId);

    void persist(Greenhouse greenhouse);

    Greenhouse merge(Greenhouse greenhouse);
}
