package jakarta.ecofarm_backend.common.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BatchInfo {
    String batchCode;
    String status;
    String varietyName;
    String greenhouseCode;
    String greenhouseName;
    String sowDate;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String harvestDate;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Long activatedAt;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Boolean active;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Boolean recalled;

    public BatchInfo() {}

    public BatchInfo(String batchCode, String status, String vName, String gCode, String gName, String sDate) {
        this.batchCode = batchCode;
        this.status = status;
        this.varietyName = vName;
        this.greenhouseCode = gCode;
        this.greenhouseName = gName;
        this.sowDate = sDate;
    }

}
