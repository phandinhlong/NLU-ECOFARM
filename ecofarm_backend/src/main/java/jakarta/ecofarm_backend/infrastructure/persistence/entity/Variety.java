package jakarta.ecofarm_backend.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "varieties")
@Getter
@Setter
@NoArgsConstructor
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
    private Boolean active = true;

    @Column(name = "created_at", nullable = false)
    private Long createdAt;

    @Column(name = "updated_at", nullable = false)
    private Long updatedAt;

    @Transient
    private Long batchCount;

    @PrePersist
    protected void prePersist() {
        if (this.createdAt == null) this.createdAt = System.currentTimeMillis();
        if (this.updatedAt == null) this.updatedAt = System.currentTimeMillis();
    }

    @PreUpdate
    protected void preUpdate() {
        this.updatedAt = System.currentTimeMillis();
    }
}
