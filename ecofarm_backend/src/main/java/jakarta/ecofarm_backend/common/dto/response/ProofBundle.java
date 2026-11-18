package jakarta.ecofarm_backend.common.dto.response;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.Anchor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProofBundle {

    private String batchCode;
    private String batchIdHash;
    private String hashAlgorithm;
    private String canonicalization;
    private String salt;
    private String merkleRoot;
    private List<ProofLeaf> leaves;
    private List<ProofEvent> events;
    private Anchor anchor;
    private long generatedAt;
}
