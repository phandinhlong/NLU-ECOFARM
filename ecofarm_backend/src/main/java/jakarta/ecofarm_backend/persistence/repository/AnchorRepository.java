package jakarta.ecofarm_backend.persistence.repository;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;
import jakarta.ecofarm_backend.persistence.entities.Anchor;

@Stateless
public class AnchorRepository {

    @PersistenceContext
    private EntityManager em;

    public void persist(Anchor anchor) {
        em.merge(anchor);
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
}
