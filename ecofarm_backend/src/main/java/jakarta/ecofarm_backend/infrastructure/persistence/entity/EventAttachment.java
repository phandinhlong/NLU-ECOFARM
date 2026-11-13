package jakarta.ecofarm_backend.infrastructure.persistence.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "event_attachments")
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class EventAttachment extends BaseEntity {

    // JPA relation thay cho DB foreign key (cultivation_events.id)
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "event_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @JsonIgnore
    private CultivationEvent event;

    @Column(name = "file_key", nullable = false)
    private String fileKey;

    @Column(name = "sha256", nullable = false, length = 64)
    private String sha256;

    @Column(name = "mime_type", nullable = false)
    private String mimeType;

    @Override
    public String toString() {
        return "EventAttachment{id=" + getId() + ", fileKey='" + fileKey + "'}";
    }
}
