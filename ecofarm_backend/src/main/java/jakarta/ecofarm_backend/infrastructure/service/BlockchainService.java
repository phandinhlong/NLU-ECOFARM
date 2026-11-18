package jakarta.ecofarm_backend.infrastructure.service;

import jakarta.ecofarm_backend.common.dto.request.RecallRequest;
import jakarta.ecofarm_backend.common.dto.response.AnchorStatus;
import jakarta.ecofarm_backend.common.dto.response.PageResponse;
import jakarta.ecofarm_backend.common.dto.response.ProofBundle;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Anchor;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.OutboxEvent;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Recall;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.User;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;
import java.util.Map;

public interface BlockchainService {

    PageResponse<Anchor> getAllAnchors(@PositiveOrZero int page, @Positive int size);

    Map<String, Object> getReconciliation(int page, int size, boolean onlyMismatch);

    AnchorStatus getAnchorStatus(@Positive Long batchId);

    Anchor retryAnchor(@Positive Long anchorId);

    Recall recallBatch(@Positive Long batchId, RecallRequest req, User currentUser);

    ProofBundle getProofBundle(@Positive Long batchId);

    OutboxEvent createTestOutbox(String data);

    Map<String, Object> getPolygonStatus() throws Exception;

    Map<String, Object> getPolygonConfirmations(String txHash) throws Exception;

    Map<String, Object> getTransactionDetail(String txHash) throws Exception;

    Map<String, Object> triggerProcessOutbox();

    Anchor anchorToPolygon(@Positive Long batchId);
}
