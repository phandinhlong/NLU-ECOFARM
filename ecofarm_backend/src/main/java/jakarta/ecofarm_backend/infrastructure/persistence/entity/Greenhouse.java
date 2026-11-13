package jakarta.ecofarm_backend.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "green_houses")
@Getter
@Setter
@NoArgsConstructor
public class Greenhouse extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true, length = 32)
    private String code;

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "gps_lat")
    private BigDecimal gpsLat;

    @Column(name = "gps_lng")
    private BigDecimal gpsLng;

    @Column(name = "area_m2")
    private BigDecimal areaM2;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Transient
    private Long batchCount;
}
