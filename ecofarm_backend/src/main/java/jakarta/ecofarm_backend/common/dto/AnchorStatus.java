package jakarta.ecofarm_backend.common.dto;

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

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public String getBatchStatus() { return batchStatus; }
    public void setBatchStatus(String batchStatus) { this.batchStatus = batchStatus; }
    public SnapshotInfo getSnapshot() { return snapshot; }
    public void setSnapshot(SnapshotInfo snapshot) { this.snapshot = snapshot; }
    public PrivateChainStatus getPrivateChain() { return privateChain; }
    public void setPrivateChain(PrivateChainStatus privateChain) { this.privateChain = privateChain; }
    public PublicChainStatus getPublicChain() { return publicChain; }
    public void setPublicChain(PublicChainStatus publicChain) { this.publicChain = publicChain; }
    public Integer getAttempts() { return attempts; }
    public void setAttempts(Integer attempts) { this.attempts = attempts; }
    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }
    public Long getNextAttemptAt() { return nextAttemptAt; }
    public void setNextAttemptAt(Long nextAttemptAt) { this.nextAttemptAt = nextAttemptAt; }
}
