package jakarta.ecofarm_backend.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "packages")
@Getter
@Setter
@NoArgsConstructor
public class PackageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_id")
    private Long batchId;

    @Column(name = "package_code", unique = true)
    private String packageCode;

    @Column(name = "unit_count")
    private Integer unitCount;

    @Column(name = "packed_by")
    private Long packedBy;

    @Column(name = "packed_at")
    private Long packedAt;

    @Column(name = "is_active")
    private Boolean active;

    @Version
    @Column(name = "version")
    private Integer version;

    @Column(name = "created_at")
    private Long createdAt;

    @Column(name = "updated_at")
    private Long updatedAt;
}
