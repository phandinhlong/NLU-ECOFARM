package jakarta.ecofarm_backend.common.dto;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.Anchor;

import java.util.List;

public class ProofBundle {

    private String batchCode;
    private String batchIdHash;
    private String hashAlgorithm;
    private String canonicalization;
    private String salt;
    private String merkleRoot;
    private List<ProofLeaf> leaves;
    private List<ProofEvent> events;
    private Anchor anchor;
    private long generatedAt;
    private String selfCheckScript;

    public ProofBundle() {}

    public String getBatchCode() {
        return batchCode;
    }

    public void setBatchCode(String batchCode) {
        this.batchCode = batchCode;
    }

    public String getBatchIdHash() {
        return batchIdHash;
    }

    public void setBatchIdHash(String batchIdHash) {
        this.batchIdHash = batchIdHash;
    }

    public String getHashAlgorithm() {
        return hashAlgorithm;
    }

    public void setHashAlgorithm(String hashAlgorithm) {
        this.hashAlgorithm = hashAlgorithm;
    }

    public String getCanonicalization() {
        return canonicalization;
    }

    public void setCanonicalization(String canonicalization) {
        this.canonicalization = canonicalization;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getMerkleRoot() {
        return merkleRoot;
    }

    public void setMerkleRoot(String merkleRoot) {
        this.merkleRoot = merkleRoot;
    }

    public List<ProofLeaf> getLeaves() {
        return leaves;
    }

    public void setLeaves(List<ProofLeaf> leaves) {
        this.leaves = leaves;
    }

    public List<ProofEvent> getEvents() {
        return events;
    }

    public void setEvents(List<ProofEvent> events) {
        this.events = events;
    }

    public Anchor getAnchor() {
        return anchor;
    }

    public void setAnchor(Anchor anchor) {
        this.anchor = anchor;
    }

    public long getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(long generatedAt) {
        this.generatedAt = generatedAt;
    }

    public String getSelfCheckScript() {
        return selfCheckScript;
    }

    public void setSelfCheckScript(String selfCheckScript) {
        this.selfCheckScript = selfCheckScript;
    }
}
