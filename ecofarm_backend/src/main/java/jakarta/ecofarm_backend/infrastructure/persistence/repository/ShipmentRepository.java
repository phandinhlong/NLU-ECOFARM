package jakarta.ecofarm_backend.infrastructure.persistence.repository;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.Shipment;

import java.util.List;

public interface ShipmentRepository {

    void persist(Shipment shipment);

    Shipment findById(Long id);

    List<Shipment> findByBatchId(Long batchId);

    List<Shipment> findByStatus(String status);

    List<Shipment> findAll();

    Boolean existsShipmentInProgressForBatch(Long batchId);

    Shipment merge(Shipment shipment);
}
