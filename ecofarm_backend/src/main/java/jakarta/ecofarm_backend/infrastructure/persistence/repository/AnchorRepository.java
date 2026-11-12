package jakarta.ecofarm_backend.infrastructure.persistence.repository;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.Anchor;

@Stateless
public class AnchorRepository {

    @PersistenceContext
    private EntityManager em;

    public void persist(Anchor anchor) {
        em.persist(anchor);
    }

    public Anchor findById(Long id) {
        return em.find(Anchor.class, id);
    }

    public List<Anchor> findByStatus(String status) {
        TypedQuery<Anchor> q = em.createQuery("select a from Anchor a where a.status = :status", Anchor.class);
        q.setParameter("status", status);
        return q.getResultList();
    }

    public void updateAnchorStatus(Long id, String status) {
        Anchor anchor = em.find(Anchor.class, id);
        if (anchor != null) {
            anchor.setStatus(status);
            em.merge(anchor);
        }
    }

    public List<Anchor> findAll() {
        return em.createQuery("select a from Anchor a", Anchor.class).getResultList();
    }

    public List<Anchor> findBySnapshotId(Long snapshotId) {
        TypedQuery<Anchor> q = em.createQuery(
            "select a from Anchor a where a.snapshotId = :snapshotId",
            Anchor.class);
        q.setParameter("snapshotId", snapshotId);
        return q.getResultList();
    }

    public Optional<Anchor> findLatestConfirmedBySnapshotId(Long snapshotId) {
        return em.createQuery(
                "select a from Anchor a where a.snapshotId = :snapshotId and a.status = 'CONFIRMED' "
                        + "order by a.id desc",
                Anchor.class)
                .setParameter("snapshotId", snapshotId)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }
}
