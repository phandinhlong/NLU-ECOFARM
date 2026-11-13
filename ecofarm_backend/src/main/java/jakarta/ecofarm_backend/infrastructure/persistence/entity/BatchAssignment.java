package jakarta.ecofarm_backend.infrastructure.persistence.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "batch_assignments")
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class BatchAssignment extends BaseEntity {

    // JPA relation thay cho DB foreign key (batches.id)
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "batch_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @JsonIgnore
    private Batch batch;

    // JPA relation thay cho DB foreign key (users.id)
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @JsonIgnore
    private User user;
}
