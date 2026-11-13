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
public class Variety extends BaseEntity {

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

    @Transient
    private Long batchCount;
}
