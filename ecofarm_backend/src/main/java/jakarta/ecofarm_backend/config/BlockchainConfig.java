package jakarta.ecofarm_backend.config;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BlockchainConfig {

    // Private chain endpoint (Besu node)
    private String privateRpcEndpoint = "http://localhost:8545";
    // Private chain network ID
    private String privateNetworkId = "1337";
    // Anchor account private key (funded in genesis alloc, non-validator)
    private String privateAccountKey = "0x0000000000000000000000000000000000000000000000000000000000000001";
    // Anchor account gas price in wei
    private Long privateGasPrice = 1000000000L;
    // Public chain endpoint (Polygon Amoy)
    private String publicRpcEndpoint = "https://rpc-amoy.polygon.technology";
    // Public chain network ID
    private Integer publicChainId = 80002;
    // Gas price for public chain (wei)
    private Long gasPrice = 1000000000L;
    // Confirmation needed before moving to public
    private Integer requiredConfirmations = 12;

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
}
