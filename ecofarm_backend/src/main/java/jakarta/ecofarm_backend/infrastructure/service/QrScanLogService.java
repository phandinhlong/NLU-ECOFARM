package jakarta.ecofarm_backend.infrastructure.service;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.QrScanLog;

public interface QrScanLogService {

    void persist(QrScanLog log);

    long countDistinctIpSince(Long qrCodeId, long sinceEpochMillis);
}
