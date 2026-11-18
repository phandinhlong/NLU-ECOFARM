package jakarta.ecofarm_backend.infrastructure.service.impl;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.Shipment;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.ShipmentRepository;
import jakarta.ecofarm_backend.infrastructure.service.ShipmentService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class ShipmentServiceImpl implements ShipmentService {

    @Inject
    private ShipmentRepository shipmentRepository;

    @Override
    public void persist(Shipment shipment) {
        this.shipmentRepository.persist(shipment);
    }

    @Override
    public Shipment findById(Long id) {
        return this.shipmentRepository.findById(id);
    }

    @Override
    public List<Shipment> findByBatchId(Long batchId) {
        return this.shipmentRepository.findByBatchId(batchId);
    }

    @Override
    public List<Shipment> findByStatus(String status) {
        return this.shipmentRepository.findByStatus(status);
    }

    @Override
    public List<Shipment> findAll() {
        return this.shipmentRepository.findAll();
    }

    @Override
    public Boolean existsShipmentInProgressForBatch(Long batchId) {
        return this.shipmentRepository.existsShipmentInProgressForBatch(batchId);
    }

    @Override
    public Shipment merge(Shipment shipment) {
        return this.shipmentRepository.merge(shipment);
    }
}
