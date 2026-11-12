package jakarta.ecofarm_backend.infrastructure.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "qr_codes")
public class QrCode {

    @Id
    private Long id;

    @Column(name = "package_id")
    private Long packageId;

    @Column(name = "code")
    private String code;

    @Column(name = "serial")
    private Integer serial;

    @Column(name = "status")
    private String status;

    @Column(name = "printed_at")
    private Long printedAt;

    public QrCode() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPackageId() { return packageId; }
    public void setPackageId(Long packageId) { this.packageId = packageId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Integer getSerial() { return serial; }
    public void setSerial(Integer serial) { this.serial = serial; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getPrintedAt() { return printedAt; }
    public void setPrintedAt(Long printedAt) { this.printedAt = printedAt; }
}
