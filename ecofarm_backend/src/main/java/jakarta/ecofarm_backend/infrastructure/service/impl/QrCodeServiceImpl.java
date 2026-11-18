package jakarta.ecofarm_backend.infrastructure.service.impl;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.QrCode;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.QrCodeRepository;
import jakarta.ecofarm_backend.infrastructure.service.QrCodeService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class QrCodeServiceImpl implements QrCodeService {

    @Inject
    private QrCodeRepository qrCodeRepository;

    @Override
    public void persist(QrCode qrCode) {
        this.qrCodeRepository.persist(qrCode);
    }

    @Override
    public QrCode merge(QrCode qrCode) {
        return this.qrCodeRepository.merge(qrCode);
    }

    @Override
    public QrCode findByCode(String code) {
        return this.qrCodeRepository.findByCode(code);
    }

    @Override
    public boolean existsByCode(String code) {
        return this.qrCodeRepository.existsByCode(code);
    }

    @Override
    public List<QrCode> findByPackageIdOrderBySerialAsc(Long packageId, Integer fromSerial, Integer toSerial) {
        return this.qrCodeRepository.findByPackageIdOrderBySerialAsc(packageId, fromSerial, toSerial);
    }

    @Override
    public int countByPackageId(Long packageId) {
        return this.qrCodeRepository.countByPackageId(packageId);
    }
}
