package jakarta.ecofarm_backend.common.dto.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AnchorStatus {
    Long batchId;
    String batchStatus;
    SnapshotInfo snapshot;
    PrivateChainStatus privateChain;
    PublicChainStatus publicChain;
    Integer attempts;
    String lastError;
    Long nextAttemptAt;

    public static class SnapshotInfo {
        Long id;
        String merkleRoot;
        Integer leafCount;

        public SnapshotInfo() {}
        public SnapshotInfo(Long id, String merkleRoot, Integer leafCount) {
            this.id = id;
            this.merkleRoot = merkleRoot;
            this.leafCount = leafCount;
        }
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getMerkleRoot() { return merkleRoot; }
        public void setMerkleRoot(String merkleRoot) { this.merkleRoot = merkleRoot; }
        public Integer getLeafCount() { return leafCount; }
        public void setLeafCount(Integer leafCount) { this.leafCount = leafCount; }
    }

}
