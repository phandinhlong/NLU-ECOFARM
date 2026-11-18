package jakarta.ecofarm_backend.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchInfo {
    String batchCode;
    String status;
    String varietyName;
    String greenhouseCode;
    String greenhouseName;
    String sowDate;
    String harvestDate;
    Long activatedAt;
    Boolean active;
    Boolean recalled;

    public BatchInfo(String batchCode, String status, String vName, String gCode, String gName, String sDate) {
        this.batchCode = batchCode;
        this.status = status;
        this.varietyName = vName;
        this.greenhouseCode = gCode;
        this.greenhouseName = gName;
        this.sowDate = sDate;
    }
}
