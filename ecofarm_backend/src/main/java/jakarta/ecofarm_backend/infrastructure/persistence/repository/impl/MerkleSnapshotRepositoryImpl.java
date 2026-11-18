package jakarta.ecofarm_backend.infrastructure.persistence.repository.impl;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.MerkleSnapshot;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.MerkleSnapshotRepository;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;

@Stateless
public class MerkleSnapshotRepositoryImpl implements MerkleSnapshotRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void persist(MerkleSnapshot snapshot) {
        em.persist(snapshot);
    }

    @Override
    public void updateLeavesJson(Long snapshotId, String leavesJson) {
        em.flush();
        em.createNativeQuery("UPDATE merkle_snapshots SET leaves = CAST(:leaves AS json), is_active = true, version = COALESCE(version, 1) WHERE id = :id")
                .setParameter("leaves", leavesJson)
                .setParameter("id", snapshotId)
                .executeUpdate();
    }

    @Override
    public MerkleSnapshot findById(Long id) {
        return em.find(MerkleSnapshot.class, id);
    }

    @Override
    public List<MerkleSnapshot> findAll() {
        TypedQuery<MerkleSnapshot> q = em.createQuery("select s from MerkleSnapshot s", MerkleSnapshot.class);
        return q.getResultList();
    }

    @Override
    public MerkleSnapshot findByBatchId(Long batchId) {
        TypedQuery<MerkleSnapshot> q = em.createQuery(
            "select s from MerkleSnapshot s where s.batch.id = :batchId order by s.id desc",
            MerkleSnapshot.class);
        q.setParameter("batchId", batchId);
        List<MerkleSnapshot> results = q.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }
    
}
