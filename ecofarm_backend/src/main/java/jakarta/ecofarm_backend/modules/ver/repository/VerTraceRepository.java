package jakarta.ecofarm_backend.modules.ver.repository;

import jakarta.ecofarm_backend.common.dto.response.CertificateInfo;
import jakarta.ecofarm_backend.common.dto.response.EventData;
import jakarta.ecofarm_backend.common.dto.response.PublicChainStatus;
import jakarta.ecofarm_backend.common.dto.response.QcMetricInfo;
import jakarta.ecofarm_backend.common.dto.response.RecallInfo;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Anchor;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Batch;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.CultivationEvent;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Greenhouse;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.PackageEntity;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.QrCode;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Recall;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Variety;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.AnchorRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.CultivationEventRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.QrCodeRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.RecallRepository;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Stateless
public class VerTraceRepository {

    @PersistenceContext
    private EntityManager em;

    @Inject
    private QrCodeRepository qrCodeRepository;

    @Inject
    private CultivationEventRepository cultivationEventRepository;

    @Inject
    private RecallRepository recallRepository;

    @Inject
    private AnchorRepository anchorRepository;

    public TraceData load(String code) {
        QrCode qr = qrCodeRepository.findByCode(code);
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

        List<CultivationEvent> events = cultivationEventRepository.findByBatchIdOrderBySeqAsc(batch.getId());
        data.setEvents(events);
        data.setTimeline(mapTimeline(events));
        data.setMetrics(findLatestQcByBatchId(batch.getId()));

        List<Recall> recalls = recallRepository.findByBatchId(batch.getId());
        data.setRecalls(mapRecalls(recalls));
        data.setRecalled(recalls != null && !recalls.isEmpty());

        SnapshotData snapshot = findLatestSnapshotByBatchId(batch.getId());
        if (snapshot != null) {
            data.setMerkleRoot(snapshot.getMerkleRoot());
            data.setLeafCount(snapshot.getLeafCount());
            data.setAnchors(mapAnchors(anchorRepository.findBySnapshotId(snapshot.getId())));
        }
    }

    private List<EventData> mapTimeline(List<CultivationEvent> events) {
        List<EventData> timeline = new ArrayList<>();
        if (events == null) {
            return timeline;
        }
        for (CultivationEvent e : events) {
            Long recordedById = e.getRecordedBy() != null ? e.getRecordedBy().getId() : null;
            timeline.add(new EventData(e.getSeq(), e.getEventType(), e.getStatus(),
                    e.getRecordedAt(), e.getEventHash(), recordedById));
        }
        return timeline;
    }

    private List<RecallInfo> mapRecalls(List<Recall> recalls) {
        List<RecallInfo> result = new ArrayList<>();
        if (recalls == null) {
            return result;
        }
        for (Recall r : recalls) {
            result.add(RecallInfo.builder()
                    .reason(r.getReason())
                    .createdAt(r.getCreatedAt())
                    .revokeTxHash(r.getRevokeTxHash())
                    .build());
        }
        return result;
    }

    private List<PublicChainStatus> mapAnchors(List<Anchor> anchors) {
        List<PublicChainStatus> result = new ArrayList<>();
        if (anchors == null) {
            return result;
        }
        for (Anchor a : anchors) {
            PublicChainStatus status = new PublicChainStatus();
            status.setId(a.getId());
            status.setSnapshotId(a.getSnapshot() != null ? a.getSnapshot().getId() : null);
            status.setChainId(a.getChainId());
            status.setContractAddress(a.getContractAddress());
            status.setTxHash(a.getTxHash());
            status.setBlockNumber(a.getBlockNumber());
            status.setConfirmations(a.getConfirmations());
            status.setStatus(a.getStatus());
            status.setAnchoredAt(a.getAnchoredAt());
            status.setExplorerUrl(a.getExplorerUrl());
            result.add(status);
        }
        result.sort(Comparator.comparing(PublicChainStatus::getId,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return result;
    }

    private List<CertificateInfo> findCertificatesByGreenhouseId(Long greenhouseId) {
        return em.createQuery(
                "select new jakarta.ecofarm_backend.common.dto.response.CertificateInfo(c.certType, c.certNo, c.validFrom, c.validTo) "
                        + "from Certificate c where c.greenhouse.id = :greenhouseId order by c.id desc",
                CertificateInfo.class)
                .setParameter("greenhouseId", greenhouseId)
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
}
