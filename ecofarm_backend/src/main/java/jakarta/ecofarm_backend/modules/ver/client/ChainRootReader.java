package jakarta.ecofarm_backend.modules.ver.client;

import jakarta.ecofarm_backend.config.BlockchainConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.http.HttpService;

@ApplicationScoped
public class ChainRootReader {

    private static final Logger log = LogManager.getLogger(ChainRootReader.class);

    // 4 byte dau cua keccak256("root()")
    private static final String ROOT_SELECTOR = "0xebf9014e";

    @Inject
    BlockchainConfig blockchainConfig;

    /** Endpoint RPC theo chainId (Polygon Amoy hoac private Besu); null neu khong ho tro. */
    public String resolveEndpoint(Integer chainId) {
        if (chainId == null) {
            return null;
        }
        if (chainId.equals(blockchainConfig.getPublicChainId())) {
            return blockchainConfig.getPublicRpcEndpoint();
        }
        if (String.valueOf(chainId).equals(blockchainConfig.getPrivateNetworkId())) {
            return blockchainConfig.getPrivateRpcEndpoint();
        }
        return null;
    }

    public String chainName(Integer chainId) {
        if (chainId == null) {
            return null;
        }
        if (chainId.equals(blockchainConfig.getPublicChainId())) {
            return "Polygon Amoy";
        }
        if (String.valueOf(chainId).equals(blockchainConfig.getPrivateNetworkId())) {
            return "Private Chain (Besu)";
        }
        return "Chain " + chainId;
    }

    public boolean isPublicChain(Integer chainId) {
        return chainId != null && chainId.equals(blockchainConfig.getPublicChainId());
    }

    /** eth_call root() doc bytes32 cua contract; null khi RPC loi/revert. */
    public String callRoot(String rpcEndpoint, String contractAddress) {
        if (rpcEndpoint == null || contractAddress == null || contractAddress.isBlank()) {
            return null;
        }
        try {
            Web3j web3j = Web3j.build(new HttpService(rpcEndpoint));
            try {
                EthCall resp = web3j.ethCall(
                        Transaction.createEthCallTransaction(null, contractAddress, ROOT_SELECTOR),
                        DefaultBlockParameterName.LATEST).send();
                if (resp.hasError()) {
                    log.warn("eth_call root() error on {}: {}", rpcEndpoint, resp.getError().getMessage());
                    return null;
                }
                String result = resp.getValue();
                return (result == null || result.isBlank() || "0x".equals(result)) ? null : result;
            } finally {
                web3j.shutdown();
            }
        } catch (Exception e) {
            log.warn("eth_call root() failed on {}: {}", rpcEndpoint, e.getMessage());
            return null;
        }
    }
}
