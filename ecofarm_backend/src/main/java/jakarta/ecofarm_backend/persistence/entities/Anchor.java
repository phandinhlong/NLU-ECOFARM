package jakarta.ecofarm_backend.persistence.entities;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "anchors")
public class Anchor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "snapshot_id")
    private Long snapshotId;

    // JPA relation thay cho DB foreign key (merkle_snapshots.id)
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "snapshot_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @JsonIgnore
    private MerkleSnapshot snapshot;

    @Column(name = "chain_id")
    private Integer chainId;

    @Column(name = "contract_address")
    private String contractAddress;

    @Column(name = "tx_hash")
    private String txHash;

    @Column(name = "block_number")
    private Long blockNumber;

    @Column(name = "confirmations")
    private int confirmations;

    @Column(name = "status")
    private String status;

    @Column(name = "anchored_at")
    private Long anchoredAt; // epoch seconds (BIGINT)

    @Column(name = "explorer_url")
    private String explorerUrl;

    @Column(name = "is_active")
    private Boolean active;

    @Version
    private Integer version;

    @Column(name = "created_at")
    private Long createdAt;

    @Column(name = "updated_at")
    private Long updatedAt;

    public Anchor() {}

    public Anchor(Long snapshotId, Integer chainId, String contractAddress) {
        this.snapshotId = snapshotId;
        this.chainId = chainId;
        this.contractAddress = contractAddress;
        this.status = "PENDING";
        this.confirmations = 0;
        this.active = true;
        this.version = 1;
        this.createdAt = System.currentTimeMillis() / 1000;
        this.updatedAt = this.createdAt;
        this.anchoredAt = this.createdAt;
    }

    // getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSnapshotId() { return snapshotId; }
    public void setSnapshotId(Long snapshotId) { this.snapshotId = snapshotId; }
    public MerkleSnapshot getSnapshot() { return snapshot; }
    public void setSnapshot(MerkleSnapshot snapshot) { this.snapshot = snapshot; }
    public Integer getChainId() { return chainId; }
    public void setChainId(Integer chainId) { this.chainId = chainId; }
    public String getContractAddress() { return contractAddress; }
    public void setContractAddress(String contractAddress) { this.contractAddress = contractAddress; }
    public String getTxHash() { return txHash; }
    public void setTxHash(String txHash) { this.txHash = txHash; }
    public Long getBlockNumber() { return blockNumber; }
    public void setBlockNumber(Long blockNumber) { this.blockNumber = blockNumber; }
    public int getConfirmations() { return confirmations; }
    public void setConfirmations(int confirmations) { this.confirmations = confirmations; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getAnchoredAt() { return anchoredAt; }
    public void setAnchoredAt(Long anchoredAt) { this.anchoredAt = anchoredAt; }
    public String getExplorerUrl() { return explorerUrl; }
    public void setExplorerUrl(String explorerUrl) { this.explorerUrl = explorerUrl; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
    public Long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Long updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Anchor{id=" + id + ", txHash='" + txHash + "', status='" + status + "'}";
    }
}
