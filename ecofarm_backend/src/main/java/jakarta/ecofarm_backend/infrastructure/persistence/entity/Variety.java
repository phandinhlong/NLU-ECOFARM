package jakarta.ecofarm_backend.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "varieties")
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Variety extends BaseEntity {

    @Column(name = "code", nullable = false, length = 32)
    private String code;

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "days_pollination_to_harvest")
    private Integer daysPollinationToHarvest;

    @Column(name = "min_brix")
    private Double minBrix;

    @Column(name = "min_isolation_days")
    private Integer minIsolationDays;

    @Transient
    private Long batchCount;

    @Override
    public String toString() {
        return "Variety{id=" + getId() + ", code='" + code + "', name='" + name + "'}";
    }
}
