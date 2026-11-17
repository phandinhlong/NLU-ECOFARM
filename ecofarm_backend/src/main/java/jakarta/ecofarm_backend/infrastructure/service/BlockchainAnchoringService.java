package jakarta.ecofarm_backend.infrastructure.service;

public interface BlockchainAnchoringService {

    void processOutbox();

   void checkConfirmations();
}
