package jakarta.ecofarm_backend.modules.blockchain.facade.impl;

import jakarta.ecofarm_backend.common.anotation.DatabaseTransactional;
import jakarta.ecofarm_backend.common.dto.request.RecallRequest;
import jakarta.ecofarm_backend.common.dto.response.AnchorStatus;
import jakarta.ecofarm_backend.common.dto.response.PageResponse;
import jakarta.ecofarm_backend.common.dto.response.ProofBundle;
import jakarta.ecofarm_backend.common.util.CurrentUserUtil;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Anchor;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.OutboxEvent;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Recall;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.User;
import jakarta.ecofarm_backend.infrastructure.security.UserSecurityDetailsPrincipal;
import jakarta.ecofarm_backend.infrastructure.service.UserService;
import jakarta.ecofarm_backend.modules.blockchain.facade.BlockchainFacade;
import jakarta.ecofarm_backend.infrastructure.service.BlockchainService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class BlockchainFacadeImpl implements BlockchainFacade {

    @Inject
    private BlockchainService blockchainService;

    @Inject
    private CurrentUserUtil currentUserUtil;

    @Inject
    private UserService userService;

    @Override
    public PageResponse<Anchor> getAllAnchors(int page, int size) {
        return blockchainService.getAllAnchors(page, size);
    }

    @Override
    public Map<String, Object> getReconciliation(int page, int size, boolean onlyMismatch) {
        return blockchainService.getReconciliation(page, size, onlyMismatch);
    }

    @Override
    public AnchorStatus getAnchorStatus(Long batchId) {
        return blockchainService.getAnchorStatus(batchId);
    }

    @Override
    @DatabaseTransactional
    public Anchor retryAnchor(Long anchorId) {
        return blockchainService.retryAnchor(anchorId);
    }

    @Override
    @DatabaseTransactional
    public Recall recallBatch(Long batchId, RecallRequest req) {
        User user = null;
        try {
            UserSecurityDetailsPrincipal principal = currentUserUtil.get();
            if (principal != null && principal.getId() != null) {
                user = userService.findById(principal.getId()).orElse(null);
            }
        } catch (Exception ignored) {
        }
        if (user == null) {
            user = userService.findById(1L).orElse(null);
        }
        return blockchainService.recallBatch(batchId, req, user);
    }

    @Override
    public ProofBundle getProofBundle(Long batchId) {
        return blockchainService.getProofBundle(batchId);
    }

    @Override
    @DatabaseTransactional
    public OutboxEvent createTestOutbox(String data) {
        return blockchainService.createTestOutbox(data);
    }

    @Override
    public Map<String, Object> getPolygonStatus() throws Exception {
        return blockchainService.getPolygonStatus();
    }

    @Override
    public Map<String, Object> getPolygonConfirmations(String txHash) throws Exception {
        return blockchainService.getPolygonConfirmations(txHash);
    }

    @Override
    public Map<String, Object> getTransactionDetail(String txHash) throws Exception {
        return blockchainService.getTransactionDetail(txHash);
    }

    @Override
    public Map<String, Object> triggerProcessOutbox() {
        return blockchainService.triggerProcessOutbox();
    }
}
