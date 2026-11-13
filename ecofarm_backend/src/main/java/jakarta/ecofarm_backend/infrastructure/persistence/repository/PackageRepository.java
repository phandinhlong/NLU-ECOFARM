package jakarta.ecofarm_backend.infrastructure.persistence.repository;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.PackageEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Stateless
public class PackageRepository {

    @PersistenceContext
    private EntityManager em;

    public void persist(PackageEntity pkg) {
        em.persist(pkg);
    }

    public PackageEntity merge(PackageEntity pkg) {
        return em.merge(pkg);
    }

    public PackageEntity findById(Long id) {
        return em.find(PackageEntity.class, id);
    }

    public PackageEntity findLatestByBatchId(Long batchId) {
        List<PackageEntity> results = em.createQuery(
                        "select p from PackageEntity p where p.batchId = :batchId and (p.active is null or p.active = true) order by p.id desc",
                        PackageEntity.class)
                .setParameter("batchId", batchId)
                .setMaxResults(1)
                .getResultList();
        return results.isEmpty() ? null : results.get(0);
    }
}
