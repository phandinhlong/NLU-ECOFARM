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
        jpql.append(" AND (:lastIndex IS NULL OR c.id < :lastIndex) ORDER BY c.id DESC");

        TypedQuery<Certificate> query = em.createQuery(jpql.toString(), Certificate.class);
        bindFilters(query, criteria);
        query.setParameter("lastIndex", criteria.getLastIndexFilter().orElse(null));
        query.setMaxResults(CertificateCriteria.LENGTH + 1);
        return query.getResultList();
    }

    @Override
    public long countActive(CertificateCriteria criteria) {
        StringBuilder jpql = new StringBuilder("SELECT COUNT(c) FROM Certificate c WHERE c.isActive = true");
        appendFilters(jpql, criteria);

        TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);
        bindFilters(query, criteria);
        return query.getSingleResult();
    }

    @Override
    public boolean existsByCertNo(String certNo) {
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(c) FROM Certificate c WHERE c.isActive = true AND c.certNo = :certNo",
                Long.class);
        query.setParameter("certNo", certNo);
        return query.getSingleResult() > 0;
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
