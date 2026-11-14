package jakarta.ecofarm_backend.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

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
    @Builder.Default
    private List<Batch> batches = new ArrayList<>();

    @Override
    public String toString() {
        return "Variety{id=" + getId() + ", code='" + code + "', name='" + name + "'}";
    }
}
