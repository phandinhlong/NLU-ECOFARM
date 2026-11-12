package jakarta.ecofarm_backend.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "varieties")
public class Variety {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 32)
    private String code;

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "days_pollination_to_harvest")
    private Integer daysPollinationToHarvest;

    @Column(name = "min_brix")
    private BigDecimal minBrix;

    @Column(name = "min_isolation_days")
    private Integer minIsolationDays;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "is_active", nullable = false)
    private Boolean active;

    @Column(name = "created_at", nullable = false)
    private Long createdAt;

    @Column(name = "updated_at", nullable = false)
    private Long updatedAt;

    @Transient
    private Long batchCount;

    public Variety() {
    }

    @PrePersist
    protected void prePersist() {
        if (this.createdAt == null) this.createdAt = System.currentTimeMillis();
        if (this.updatedAt == null) this.updatedAt = System.currentTimeMillis();
    }

    @PreUpdate
    protected void preUpdate() {
        this.updatedAt = System.currentTimeMillis();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getDaysPollinationToHarvest() { return daysPollinationToHarvest; }
    public void setDaysPollinationToHarvest(Integer daysPollinationToHarvest) { this.daysPollinationToHarvest = daysPollinationToHarvest; }
    public BigDecimal getMinBrix() { return minBrix; }
    public void setMinBrix(BigDecimal minBrix) { this.minBrix = minBrix; }
    public Integer getMinIsolationDays() { return minIsolationDays; }
    public void setMinIsolationDays(Integer minIsolationDays) { this.minIsolationDays = minIsolationDays; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
    public Long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Long updatedAt) { this.updatedAt = updatedAt; }
    public Long getBatchCount() { return batchCount; }
    public void setBatchCount(Long batchCount) { this.batchCount = batchCount; }
}
