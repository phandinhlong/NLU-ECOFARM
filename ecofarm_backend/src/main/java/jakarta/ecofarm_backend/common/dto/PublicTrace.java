package jakarta.ecofarm_backend.common.dto;

public class PublicTrace {
    String code;
    BatchInfo batch;
    BlockchainStatus blockchain;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public BatchInfo getBatch() { return batch; }
    public void setBatch(BatchInfo batch) { this.batch = batch; }
    public BlockchainStatus getBlockchain() { return blockchain; }
    public void setBlockchain(BlockchainStatus blockchain) { this.blockchain = blockchain; }
}
