package jakarta.ecofarm_backend.infrastructure.service.impl;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.QrScanLog;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.QrScanLogRepository;
import jakarta.ecofarm_backend.infrastructure.service.QrScanLogService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class QrScanLogServiceImpl implements QrScanLogService {

    @Inject
    private QrScanLogRepository qrScanLogRepository;

    @Override
    public void persist(QrScanLog log) {
        this.qrScanLogRepository.persist(log);
    }

    @Override
    public long countDistinctIpSince(Long qrCodeId, long sinceEpochMillis) {
        return this.qrScanLogRepository.countDistinctIpSince(qrCodeId, sinceEpochMillis);
    }
}
