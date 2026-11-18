package jakarta.ecofarm_backend.modules.blockchain.facade;

import jakarta.ecofarm_backend.common.dto.request.RecallRequest;
import jakarta.ecofarm_backend.common.dto.response.AnchorStatus;
import jakarta.ecofarm_backend.common.dto.response.ProofBundle;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Anchor;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.OutboxEvent;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Recall;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.Map;

public interface BlockchainFacade {

    List<Anchor> getAllAnchors();

    Map<String, Object> getReconciliation(int page, int size, boolean onlyMismatch);

    AnchorStatus getAnchorStatus(@Positive Long batchId);

    Anchor retryAnchor(@Positive Long anchorId);

    Recall recallBatch(@Positive Long batchId, @Valid RecallRequest req);

    ProofBundle getProofBundle(@Positive Long batchId);

    OutboxEvent createTestOutbox(String data);

    Map<String, Object> getPolygonStatus() throws Exception;

    Map<String, Object> getPolygonConfirmations(String txHash) throws Exception;

    Map<String, Object> getTransactionDetail(String txHash) throws Exception;

    Map<String, Object> triggerProcessOutbox();
}
