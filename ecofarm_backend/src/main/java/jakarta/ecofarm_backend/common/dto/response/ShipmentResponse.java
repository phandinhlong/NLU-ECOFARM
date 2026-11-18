package jakarta.ecofarm_backend.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentResponse {

    private Long id;
    private Long batchId;
    private String batchCode;
    private String destination;
    private String vehiclePlate;
    private Long shippedAt;
    private Long receivedAt;
    private Long receivedBy;
    private Integer quantity;
    private String status;
    private String conditionNote;
    private List<String> photoUrls;
    private Long version;
    private Long createdAt;
    private Long updatedAt;
}
