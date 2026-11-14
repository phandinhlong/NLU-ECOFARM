package jakarta.ecofarm_backend.modules.ver.repository;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SnapshotData {

    private Long id;
    private String merkleRoot;
    private Integer leafCount;
}
