package jakarta.ecofarm_backend.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "green_houses")
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Greenhouse extends BaseEntity {

    @Column(name = "code", nullable = false, length = 32)
    private String code;

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "gps_lat")
    private Double gpsLat;

    @Column(name = "gps_lng")
    private Double gpsLng;

    @Column(name = "area_m2")
    private Double areaM2;

    @Column(name = "status")
    private String status;

    @Transient
    private Long batchCount;
    @Builder.Default
    private List<Batch> batches = new ArrayList<>();
    @Builder.Default
    private List<Certificate> certificates = new ArrayList<>();

    @Override
    public String toString() {
        return "Greenhouse{id=" + getId() + ", code='" + code + "', name='" + name + "'}";
    }
}
