package jakarta.ecofarm_backend.common.dto;

import java.util.List;

public class ProofLeaf {
    private Integer seq;
    private String eventHash;
    private String leaf;
    private List<String> proof;

    public ProofLeaf() {}

    public Integer getSeq() {
        return seq;
    }

    public void setSeq(Integer seq) {
        this.seq = seq;
    }

    public String getEventHash() {
        return eventHash;
    }

    public void setEventHash(String eventHash) {
        this.eventHash = eventHash;
    }

    public String getLeaf() {
        return leaf;
    }

    public void setLeaf(String leaf) {
        this.leaf = leaf;
    }

    public List<String> getProof() {
        return proof;
    }

    public void setProof(List<String> proof) {
        this.proof = proof;
    }
}
