package jakarta.ecofarm_backend.modules.blockchain.facade;

import jakarta.ecofarm_backend.common.dto.request.RecallRequest;
import jakarta.ecofarm_backend.common.dto.response.AnchorStatus;
import jakarta.ecofarm_backend.common.dto.response.ProofBundle;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Anchor;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.OutboxEvent;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Recall;

import java.util.List;
import java.util.Map;

public interface BlockchainFacade {

    List<Anchor> getAllAnchors();

    Map<String, Object> getReconciliation(int page, int size, boolean onlyMismatch);

    AnchorStatus getAnchorStatus(Long batchId);

    Anchor retryAnchor(Long anchorId);

    Recall recallBatch(Long batchId, RecallRequest req);

    ProofBundle getProofBundle(Long batchId);

    OutboxEvent createTestOutbox(String data);

    Map<String, Object> getPolygonStatus() throws Exception;

    Map<String, Object> getPolygonConfirmations(String txHash) throws Exception;

    Map<String, Object> getTransactionDetail(String txHash) throws Exception;
}
