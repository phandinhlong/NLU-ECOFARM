package jakarta.ecofarm_backend.infrastructure.persistence.repository.impl;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.Shipment;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.ShipmentRepository;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;

@Stateless
public class ShipmentRepositoryImpl implements ShipmentRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void persist(Shipment shipment) {
        em.persist(shipment);
    }

    @Override
    public Shipment findById(Long id) {
        return em.createQuery("""
                SELECT s FROM Shipment s
                LEFT JOIN FETCH s.batch
                WHERE s.id = :id
                """, Shipment.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Shipment> findByBatchId(Long batchId) {
        return em.createQuery("""
                SELECT s FROM Shipment s
                WHERE s.batch.id = :batchId
                  AND (s.isActive IS NULL OR s.isActive = true)
                ORDER BY s.id DESC
                """, Shipment.class)
                .setParameter("batchId", batchId)
                .getResultList();
    }

    @Override
    public List<Shipment> findByStatus(String status) {
        return em.createQuery("""
                SELECT s FROM Shipment s
                WHERE s.status = :status
                  AND (s.isActive IS NULL OR s.isActive = true)
                ORDER BY s.id DESC
                """, Shipment.class)
                .setParameter("status", status)
                .getResultList();
    }

    @Override
    public List<Shipment> findAll() {
        return em.createQuery("""
                SELECT s FROM Shipment s
                WHERE (s.isActive IS NULL OR s.isActive = true)
                ORDER BY s.id DESC
                """, Shipment.class)
                .getResultList();
    }

    @Override
    public Boolean existsShipmentInProgressForBatch(Long batchId) {
        TypedQuery<Long> query = em.createQuery("""
                SELECT COUNT(s) FROM Shipment s
                WHERE s.batch.id = :batchId AND s.status IN ('SHIPPED', 'IN_TRANSIT')
                """, Long.class);
        query.setParameter("batchId", batchId);
        return query.getSingleResult() > 0;
    }

    @Override
    public Shipment merge(Shipment shipment) {
        return em.merge(shipment);
    }
}
