package jakarta.ecofarm_backend.config;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BlockchainConfig {

    // Private chain endpoint (Besu node)
    private String privateRpcEndpoint = "http://localhost:8545";
    // Private chain network ID
    private String privateNetworkId = "1337";
    // Anchor account private key (funded in genesis alloc, non-validator)
    private String privateAccountKey = EnvConfig.getKey("PRIVATE_ACCOUNT_KEY");
    // Anchor account gas price in wei
    private Long privateGasPrice = 1000000000L;
    // Public chain endpoint (Polygon Amoy)
    private String publicRpcEndpoint = EnvConfig.getKey("PUBLIC_RPC_ENDPOINT", "https://polygon-amoy-bor-rpc.publicnode.com");
    // Public chain network ID
    private Integer publicChainId = EnvConfig.getInt("PUBLIC_CHAIN_ID", 80002);
    // Gas price for public chain (wei)
    private Long gasPrice = 30000000000L;
    // Confirmation needed before moving to public
    private Integer requiredConfirmations = 12;
    // public key của tài khoản giao dịch gửi lên polygon
    private String publicAccountKey = EnvConfig.getKey("PUBLIC_ACCOUNT_KEY");

    public String getPrivateRpcEndpoint() { return privateRpcEndpoint; }
    public void setPrivateRpcEndpoint(String privateRpcEndpoint) { this.privateRpcEndpoint = privateRpcEndpoint; }
    public String getPrivateNetworkId() { return privateNetworkId; }
    public void setPrivateNetworkId(String privateNetworkId) { this.privateNetworkId = privateNetworkId; }
    public String getPublicRpcEndpoint() { return publicRpcEndpoint; }
    public void setPublicRpcEndpoint(String publicRpcEndpoint) { this.publicRpcEndpoint = publicRpcEndpoint; }
    public String getPrivateAccountKey() { return privateAccountKey; }
    public void setPrivateAccountKey(String privateAccountKey) { this.privateAccountKey = privateAccountKey; }
    public Integer getPublicChainId() { return publicChainId; }
    public void setPublicChainId(Integer publicChainId) { this.publicChainId = publicChainId; }
    public Long getPrivateGasPrice() { return privateGasPrice; }
    public void setPrivateGasPrice(Long privateGasPrice) { this.privateGasPrice = privateGasPrice; }
    public Long getGasPrice() { return gasPrice; }
    public void setGasPrice(Long gasPrice) { this.gasPrice = gasPrice; }
    public Integer getRequiredConfirmations() { return requiredConfirmations; }
    public void setRequiredConfirmations(Integer requiredConfirmations) { this.requiredConfirmations = requiredConfirmations; }
    public String getPublicAccountKey(){return publicAccountKey; }
    public void setPublicAccountKey(String privateAccountKey){this.publicAccountKey=publicAccountKey;}
}
