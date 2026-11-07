package jakarta.ecofarm_backend.persistence.entities;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "anchors")
public class Anchor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "snapshot_id")
    private Long snapshotId;

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

    @Enumerated(EnumType.STRING)
    private String status;

    @Column(name = "anchored_at")
    private Instant anchoredAt;

    @Column(name = "explorer_url")
    private String explorerUrl;

    public Anchor() {}

    public Anchor(Long snapshotId, Integer chainId, String contractAddress) {
        this.snapshotId = snapshotId;
        this.chainId = chainId;
        this.contractAddress = contractAddress;
        this.status = "PENDING";
        this.confirmations = 0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSnapshotId() { return snapshotId; }
    public void setSnapshotId(Long snapshotId) { this.snapshotId = snapshotId; }
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
    public Instant getAnchoredAt() { return anchoredAt; }
    public void setAnchoredAt(Instant anchoredAt) { this.anchoredAt = anchoredAt; }
    public String getExplorerUrl() { return explorerUrl; }
    public void setExplorerUrl(String explorerUrl) { this.explorerUrl = explorerUrl; }
}
