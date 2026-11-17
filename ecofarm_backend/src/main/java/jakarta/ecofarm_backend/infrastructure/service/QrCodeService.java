package jakarta.ecofarm_backend.infrastructure.service;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.QrCode;

import java.util.List;

public interface QrCodeService {

    void persist(QrCode qrCode);

    QrCode merge(QrCode qrCode);

    QrCode findByCode(String code);

    boolean existsByCode(String code);

    List<QrCode> findByPackageIdOrderBySerialAsc(Long packageId, Integer fromSerial, Integer toSerial);

    int countByPackageId(Long packageId);
}
