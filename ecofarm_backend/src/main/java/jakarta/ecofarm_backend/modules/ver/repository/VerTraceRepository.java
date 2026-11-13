package jakarta.ecofarm_backend.modules.ver.repository;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.Batch;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Greenhouse;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.PackageEntity;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.QrCode;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Variety;
import jakarta.ecofarm_backend.common.dto.response.CertificateInfo;
import jakarta.ecofarm_backend.common.dto.response.EventData;
import jakarta.ecofarm_backend.common.dto.response.PublicChainStatus;
import jakarta.ecofarm_backend.common.dto.response.QcMetricInfo;
import jakarta.ecofarm_backend.common.dto.response.RecallInfo;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Stateless
public class VerTraceRepository {

    @PersistenceContext
    private EntityManager em;

    public TraceData load(String code) {
        QrCode qr = findQrByCode(code);
        if (qr == null) {
            return null;
        }

        TraceData data = new TraceData();
        data.setCode(qr.getCode());
        data.setSerial(qr.getSerial());
        data.setQrStatus(qr.getStatus());

        PackageEntity pkg = qr.getPkg();
        if (pkg != null) {
            data.setPackageCode(pkg.getPackageCode());
            Batch batch = pkg.getBatch();
            if (batch != null) {
                fillBatch(data, batch);
            }
        }
        return data;
    }

    private QrCode findQrByCode(String code) {
        try {
            return em.createQuery("select q from QrCode q where q.code = :code", QrCode.class)
                    .setParameter("code", code)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    private void fillBatch(TraceData data, Batch batch) {
        data.setBatchId(batch.getId());
        data.setBatchCode(batch.getBatchCode());
        data.setBatchStatus(batch.getStatus());
        data.setSowDate(batch.getSowDate() != null ? batch.getSowDate().toString() : null);
        data.setHarvestDate(batch.getHarvestDate() != null ? batch.getHarvestDate().toString() : null);
        data.setActivatedAt(batch.getActivatedAt());
        data.setSaltEnc(batch.getSaltEnc());
        data.setBatchActive(batch.isActive());

        Greenhouse greenhouse = batch.getGreenhouse();
        if (greenhouse != null) {
            data.setGreenhouseId(greenhouse.getId());
            data.setGreenhouseCode(greenhouse.getCode());
            data.setGreenhouseName(greenhouse.getName());
            data.setCertificates(findCertificatesByGreenhouseId(greenhouse.getId()));
        }

        Variety variety = batch.getVariety();
        if (variety != null) {
            data.setVarietyCode(variety.getCode());
            data.setVarietyName(variety.getName());
        }

        data.setTimeline(findEventsByBatchId(batch.getId()));
        data.setMetrics(findLatestQcByBatchId(batch.getId()));

        List<RecallInfo> recalls = findRecallsByBatchId(batch.getId());
        data.setRecalls(recalls);
        data.setRecalled(recalls != null && !recalls.isEmpty());

        SnapshotData snapshot = findLatestSnapshotByBatchId(batch.getId());
        if (snapshot != null) {
            data.setMerkleRoot(snapshot.getMerkleRoot());
            data.setLeafCount(snapshot.getLeafCount());
            data.setLeavesJson(findLeavesJsonBySnapshotId(snapshot.getId()));
            data.setAnchors(findAnchorsBySnapshotId(snapshot.getId()));
        }
    }

    private List<CertificateInfo> findCertificatesByGreenhouseId(Long greenhouseId) {
        return em.createQuery(
                "select new jakarta.ecofarm_backend.common.dto.response.CertificateInfo(c.certType, c.certNo, c.validFrom, c.validTo) "
                        + "from Certificate c where c.greenhouse.id = :greenhouseId order by c.id desc",
                CertificateInfo.class)
                .setParameter("greenhouseId", greenhouseId)
                .getResultList();
    }

    private List<EventData> findEventsByBatchId(Long batchId) {
        return em.createQuery(
                "select new jakarta.ecofarm_backend.common.dto.response.EventData(e.seq, e.eventType, e.status, e.recordedAt, e.eventHash, u.id) "
                        + "from CultivationEvent e left join e.recordedBy u "
                        + "where e.batch.id = :batchId order by e.seq asc, e.id asc",
                EventData.class)
                .setParameter("batchId", batchId)
                .getResultList();
    }

    private QcMetricInfo findLatestQcByBatchId(Long batchId) {
        return em.createQuery(
                "select new jakarta.ecofarm_backend.common.dto.response.QcMetricInfo(q.brix, q.avgWeightKg, q.passed, q.inspectedAt) "
                        + "from QcRecord q where q.batch.id = :batchId order by q.id desc",
                QcMetricInfo.class)
                .setParameter("batchId", batchId)
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    private List<RecallInfo> findRecallsByBatchId(Long batchId) {
        return em.createQuery(
                "select new jakarta.ecofarm_backend.common.dto.response.RecallInfo(r.reason, r.createdAt, r.revokeTxHash) "
                        + "from Recall r where r.batch.id = :batchId order by r.id desc",
                RecallInfo.class)
                .setParameter("batchId", batchId)
                .getResultList();
    }

    private SnapshotData findLatestSnapshotByBatchId(Long batchId) {
        return em.createQuery(
                "select new jakarta.ecofarm_backend.modules.ver.repository.SnapshotData(s.id, s.merkleRoot, s.leafCount) "
                        + "from MerkleSnapshot s where s.batch.id = :batchId order by s.id desc",
                SnapshotData.class)
                .setParameter("batchId", batchId)
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    private String findLeavesJsonBySnapshotId(Long snapshotId) {
        return (String) em.createNativeQuery("select cast(leaves as text) from merkle_snapshots where id = ?1")
                .setParameter(1, snapshotId)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    private List<PublicChainStatus> findAnchorsBySnapshotId(Long snapshotId) {
        return em.createQuery(
                "select new jakarta.ecofarm_backend.common.dto.response.PublicChainStatus(a.id, a.chainId, a.contractAddress, a.txHash, a.blockNumber, a.status, a.anchoredAt, a.explorerUrl) "
                        + "from Anchor a where a.snapshot.id = :snapshotId order by a.id desc",
                PublicChainStatus.class)
                .setParameter("snapshotId", snapshotId)
                .getResultList();
    }
}
