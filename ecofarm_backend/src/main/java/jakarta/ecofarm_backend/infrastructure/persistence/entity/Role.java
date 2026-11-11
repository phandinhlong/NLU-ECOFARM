package jakarta.ecofarm_backend.infrastructure.persistence.entity;

import jakarta.ecofarm_backend.common.enums.RoleEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "roles")
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Role extends BaseEntity {
    @Column(name = "role_name", nullable = false, unique = true)
    @Enumerated(value = EnumType.STRING)
    private RoleEnum roleName;
}
