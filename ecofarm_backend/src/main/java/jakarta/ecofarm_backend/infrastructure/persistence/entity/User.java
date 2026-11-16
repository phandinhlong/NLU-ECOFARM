package jakarta.ecofarm_backend.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseEntity {

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String password;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    @Builder.Default
    private List<Role> roles = new ArrayList<>();
    @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Batch> batches = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @Builder.Default
    private List<BatchAssignment> batchAssignments = new ArrayList<>();

    @OneToMany(mappedBy = "actor", fetch = FetchType.LAZY)
    @Builder.Default
    private List<AuditLog> auditLogs = new ArrayList<>();

    @OneToMany(mappedBy = "recordedBy", fetch = FetchType.LAZY)
    @Builder.Default
    private List<CultivationEvent> cultivationEvents = new ArrayList<>();

    @OneToMany(mappedBy = "inspectedByUser", fetch = FetchType.LAZY)
    @Builder.Default
    private List<QcRecord> qcRecords = new ArrayList<>();

    @OneToMany(mappedBy = "createdByUser", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Recall> recalls = new ArrayList<>();

    @OneToMany(mappedBy = "packedBy", fetch = FetchType.LAZY)
    @Builder.Default
    private List<PackageEntity> packageEntities = new ArrayList<>();
    public void addRole(Role role) {
        this.roles.add(role);
    }

    public void activeUser() {
        super.isActive = true;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
}
