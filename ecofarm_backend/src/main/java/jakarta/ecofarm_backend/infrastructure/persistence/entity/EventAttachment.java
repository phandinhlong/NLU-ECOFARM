package jakarta.ecofarm_backend.infrastructure.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "event_attachments")
public class EventAttachment {

    @Id
    private Long id;

    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "file_key")
    private String fileKey;

    @Column(name = "sha256")
    private String sha256;

    @Column(name = "mime_type")
    private String mimeType;

    public EventAttachment() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
    public String getFileKey() { return fileKey; }
    public void setFileKey(String fileKey) { this.fileKey = fileKey; }
    public String getSha256() { return sha256; }
    public void setSha256(String sha256) { this.sha256 = sha256; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
}
