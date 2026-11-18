package jakarta.ecofarm_backend.common.dto.response;

import jakarta.validation.constraints.Positive;
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
public class AnchorStatus {
    @Positive
    Long batchId;
    String batchStatus;
    SnapshotInfo snapshot;
    PrivateChainStatus privateChain;
    PublicChainStatus publicChain;
    Integer attempts;
    String lastError;
    Long nextAttemptAt;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SnapshotInfo {
        @Positive
        Long id;
        String merkleRoot;
        @Positive
        Integer leafCount;
    }
}
