package jakarta.ecofarm_backend.persistence.repository;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;
import jakarta.ecofarm_backend.persistence.entities.MerkleSnapshot;

@Stateless
public class MerkleSnapshotRepository {

    @PersistenceContext
    private EntityManager em;

    public void persist(MerkleSnapshot snapshot) {
        em.merge(snapshot);
    }

    public MerkleSnapshot findById(Long id) {
        return em.find(MerkleSnapshot.class, id);
    }

    public List<MerkleSnapshot> findAll() {
        TypedQuery<MerkleSnapshot> q = em.createQuery("select s from MerkleSnapshot s", MerkleSnapshot.class);
        return q.getResultList();
    }
}
