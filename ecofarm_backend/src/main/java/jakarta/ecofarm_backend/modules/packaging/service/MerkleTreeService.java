package jakarta.ecofarm_backend.modules.packaging.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.CultivationEvent;
import jakarta.enterprise.context.ApplicationScoped;
import org.web3j.crypto.Hash;

import java.util.ArrayList;
import java.util.List;

/**
 * Dựng cây Merkle từ danh sách sự kiện canh tác đã xác thực (FR-PCK-002).
 */
@ApplicationScoped
public class MerkleTreeService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public record MerkleResult(String merkleRoot, List<String> leafHashes, String leavesJson) {}

    public MerkleResult buildTree(Long saltEnc, List<CultivationEvent> events) {
        List<String> leafHashes = new ArrayList<>();
        String salt = saltEnc != null ? String.valueOf(saltEnc) : "0";

        for (CultivationEvent event : events) {
            String eventHash = event.getEventHash() != null ? event.getEventHash() : "";
            String leafInput = salt + ":" + event.getSeq() + ":" + eventHash;
            leafHashes.add(Hash.sha3String(leafInput));
        }

        if (leafHashes.isEmpty()) {
            throw new IllegalArgumentException("Cannot build Merkle tree with empty events");
        }

        List<String> currentLevel = new ArrayList<>(leafHashes);
        while (currentLevel.size() > 1) {
            List<String> nextLevel = new ArrayList<>();
            for (int i = 0; i < currentLevel.size(); i += 2) {
                String left = currentLevel.get(i);
                String right = (i + 1 < currentLevel.size()) ? currentLevel.get(i + 1) : left;
                nextLevel.add(Hash.sha3String(left + right));
            }
            currentLevel = nextLevel;
        }

        String leavesJson;
        try {
            leavesJson = MAPPER.writeValueAsString(leafHashes);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize Merkle leaves", e);
        }

        return new MerkleResult(currentLevel.get(0), leafHashes, leavesJson);
    }
}
