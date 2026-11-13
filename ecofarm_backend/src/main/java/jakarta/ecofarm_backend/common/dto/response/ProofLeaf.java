package jakarta.ecofarm_backend.common.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class ProofLeaf {
    private Integer seq;
    private String eventHash;
    private String leaf;
    private List<String> proof;

    public ProofLeaf() {}

}
