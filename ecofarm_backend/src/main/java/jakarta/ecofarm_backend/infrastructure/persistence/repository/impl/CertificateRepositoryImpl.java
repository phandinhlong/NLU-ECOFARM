package jakarta.ecofarm_backend.infrastructure.persistence.repository.impl;

import jakarta.ecofarm_backend.common.dto.request.CertificateCriteria;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Certificate;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.CertificateRepository;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.util.List;

@Stateless
public class CertificateRepositoryImpl implements CertificateRepository {

    @PersistenceContext
    private EntityManager em;

    private void appendFilters(StringBuilder jpql, CertificateCriteria criteria) {
        criteria.getGreenhouseFilter().ifPresent(value -> jpql.append(" AND c.greenhouse.id = :greenhouseId"));
        criteria.getExpiringFilter().ifPresent(value -> jpql.append(" AND c.validTo >= :fromDate AND c.validTo <= :toDate"));
    }

    private void bindFilters(TypedQuery<?> query, CertificateCriteria criteria) {
        criteria.getGreenhouseFilter().ifPresent(value -> query.setParameter("greenhouseId", value));
        criteria.getExpiringFilter().ifPresent(value -> {
            LocalDate fromDate = LocalDate.now();
            query.setParameter("fromDate", fromDate);
            query.setParameter("toDate", fromDate.plusDays(value));
        });
    }

    @Override
    public List<Certificate> findPage(CertificateCriteria criteria) {
        StringBuilder jpql = new StringBuilder("""
                SELECT c FROM Certificate c
                LEFT JOIN FETCH c.greenhouse
                WHERE c.isActive = true
                """);
        appendFilters(jpql, criteria);
        jpql.append(" AND (:lastId IS NULL OR c.id < :lastId) ORDER BY c.id DESC");

        TypedQuery<Certificate> query = em.createQuery(jpql.toString(), Certificate.class);
        bindFilters(query, criteria);
        query.setParameter("lastId", criteria.getLastIdFilter().orElse(null));
        query.setMaxResults(CertificateCriteria.LENGTH + 1);
        return query.getResultList();
    }

    @Override
    public Long countActive(CertificateCriteria criteria) {
        StringBuilder jpql = new StringBuilder("SELECT COUNT(c) FROM Certificate c WHERE c.isActive = true");
        appendFilters(jpql, criteria);

        TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);
        bindFilters(query, criteria);
        return query.getSingleResult();
    }

    @Override
    public boolean existsByCertNo(String certNo) {
        return existsByCertNoAndIdNot(certNo, null);
    }

    @Override
    public boolean existsByCertNoAndIdNot(String certNo, Long id) {
        StringBuilder jpql = new StringBuilder("SELECT COUNT(c) FROM Certificate c WHERE c.certNo = :certNo");
        if (id != null) {
            jpql.append(" AND c.id <> :id");
        }
        TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);
        query.setParameter("certNo", certNo);
        if (id != null) {
            query.setParameter("id", id);
        }
        return query.getSingleResult() > 0;
    }

    @Override
    public Certificate findById(Long id) {
        return em.find(Certificate.class, id);
    }

    @Override
    public Certificate save(Certificate certificate) {
        if (certificate.getId() == null) {
            em.persist(certificate);
            return certificate;
        }
        return em.merge(certificate);
    }
}
