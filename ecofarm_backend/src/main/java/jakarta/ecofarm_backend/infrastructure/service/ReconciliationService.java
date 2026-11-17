package jakarta.ecofarm_backend.infrastructure.service;

import jakarta.ecofarm_backend.common.dto.response.ReconciliationItem;

import java.util.List;

public interface ReconciliationService {
    List<ReconciliationItem> reconcileAll();
}
