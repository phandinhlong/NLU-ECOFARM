package jakarta.ecofarm_backend.common.dto;

public class CertificateInfo {
    String certType;
    String certNo;
    String validTo;

    public CertificateInfo() {}

    public String getCertType() { return certType; }
    public void setCertType(String certType) { this.certType = certType; }
    public String getCertNo() { return certNo; }
    public void setCertNo(String certNo) { this.certNo = certNo; }
    public String getValidTo() { return validTo; }
    public void setValidTo(String validTo) { this.validTo = validTo; }
}
