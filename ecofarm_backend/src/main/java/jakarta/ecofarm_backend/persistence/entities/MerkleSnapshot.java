package jakarta.ecofarm_backend.persistence.entities;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "merkle_snapshots")
public class MerkleSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String merkleRoot;

    @Column(nullable = false)
    private int leafCount;

    @Column(nullable = false)
    private Instant createdAt;

    public MerkleSnapshot() {}

    public MerkleSnapshot(String merkleRoot, int leafCount) {
        this.merkleRoot = merkleRoot;
        this.leafCount = leafCount;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMerkleRoot() { return merkleRoot; }
    public void setMerkleRoot(String merkleRoot) { this.merkleRoot = merkleRoot; }
    public int getLeafCount() { return leafCount; }
    public void setLeafCount(int leafCount) { this.leafCount = leafCount; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
