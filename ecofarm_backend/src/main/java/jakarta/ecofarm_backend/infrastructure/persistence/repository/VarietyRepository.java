package jakarta.ecofarm_backend.infrastructure.persistence.repository;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.Variety;

import java.util.List;

public interface VarietyRepository {

    List<Variety> findPage(int page, int size, String q);

    long countActive(String q);

    Variety findById(Long id);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    long countBatches(Long varietyId);

    void persist(Variety variety);

    Variety merge(Variety variety);
}
