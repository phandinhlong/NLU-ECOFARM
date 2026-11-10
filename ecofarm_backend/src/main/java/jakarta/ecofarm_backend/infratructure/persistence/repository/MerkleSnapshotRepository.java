package jakarta.ecofarm_backend.infratructure.persistence.repository;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;

import jakarta.ecofarm_backend.infratructure.persistence.entities.MerkleSnapshot;

@Stateless
public class MerkleSnapshotRepository {

    @PersistenceContext
    private EntityManager em;

    public void persist(MerkleSnapshot snapshot) {
        em.persist(snapshot);
    }

    public MerkleSnapshot findById(Long id) {
        return em.find(MerkleSnapshot.class, id);
    }

    public List<MerkleSnapshot> findAll() {
        TypedQuery<MerkleSnapshot> q = em.createQuery("select s from MerkleSnapshot s", MerkleSnapshot.class);
        return q.getResultList();
    }

    public MerkleSnapshot findByBatchId(Long batchId) {
        TypedQuery<MerkleSnapshot> q = em.createQuery(
            "select s from MerkleSnapshot s where s.batchId = :batchId",
            MerkleSnapshot.class);
        q.setParameter("batchId", batchId);
        List<MerkleSnapshot> results = q.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public MerkleSnapshot findLatest() {
        List<MerkleSnapshot> all = findAll();
        return all.isEmpty() ? null : all.get(all.size() - 1);
    }
}
