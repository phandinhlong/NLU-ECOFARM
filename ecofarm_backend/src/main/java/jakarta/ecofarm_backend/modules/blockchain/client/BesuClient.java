package jakarta.ecofarm_backend.modules.blockchain.client;

import java.io.IOException;
import java.math.BigInteger;

import jakarta.ecofarm_backend.config.BlockchainConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthChainId;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

@ApplicationScoped
public class BesuClient implements BlockchainClient {

    @Inject
    BlockchainConfig config;

    private volatile Web3j web3j;
    private volatile Credentials credentials;

    public BesuClient() {
    }

    @Override
    public void connect() throws IOException {
        ensureConnected();
    }

    @Override
    public void disconnect() {
        Web3j current = web3j;
        if (current != null) {
            current.shutdown();
            web3j = null;
            credentials = null;
        }
    }

    @Override
    public boolean isConnected() {
        if (web3j != null) {
            try {
                web3j.ethBlockNumber().send();
                return true;
            } catch (Exception e) {
                disconnect();
                return false;
            }
        }
        return false;
    }

    @Override
    public String sendTransaction(String data) throws Exception {
        ensureConnected();
        byte[] payload = hexOrUtf8(data);
        EthGetTransactionCount nonceResp = web3j.ethGetTransactionCount(
                credentials.getAddress(), DefaultBlockParameterName.PENDING).send();
        long nonce = nonceResp.getTransactionCount().longValueExact();
        BigInteger gasPrice = BigInteger.valueOf(config.getPrivateGasPrice());
        BigInteger gasLimit = BigInteger.valueOf(100_000L);
        RawTransaction raw = RawTransaction.createTransaction(
                BigInteger.valueOf(nonce),
                gasPrice,
                gasLimit,
                credentials.getAddress(),
                BigInteger.ZERO,
                Numeric.toHexString(payload));

        long chainId = Long.parseLong(config.getPrivateNetworkId());
        byte[] signed = TransactionEncoder.signMessage(raw, chainId, credentials);
        EthSendTransaction resp = web3j.ethSendRawTransaction(Numeric.toHexString(signed)).send();
        if (resp.hasError()) {
            throw new RuntimeException("eth_sendRawTransaction error: " + resp.getError().getMessage());
        }
        String txHash = resp.getTransactionHash();
        if (txHash == null || txHash.isEmpty()) {
            throw new RuntimeException("eth_sendRawTransaction returned no hash");
        }
        return txHash;
    }

    @Override
    public long getCurrentBlockNumber() throws IOException {
        ensureConnected();
        try {
            EthBlockNumber resp = web3j.ethBlockNumber().send();
            if (resp.hasError()) {
                throw new RuntimeException("eth_blockNumber error: " + resp.getError().getMessage());
            }
            return resp.getBlockNumber().longValueExact();
        } catch (IOException e) {
            throw new RuntimeException("Besu eth_blockNumber I/O failure: " + e.getMessage(), e);
        }
    }

    @Override
    public int getConfirmations(String txHash) throws Exception {
        ensureConnected();
        EthGetTransactionReceipt receiptResp = web3j.ethGetTransactionReceipt(txHash).send();
        if (receiptResp.hasError()) {
            throw new RuntimeException("eth_getTransactionReceipt error: " + receiptResp.getError().getMessage());
        }
        if (receiptResp.getTransactionReceipt().isEmpty()) {
            return 0;
        }
        long minedAt = receiptResp.getTransactionReceipt().get().getBlockNumber().longValueExact();
        long latest = web3j.ethBlockNumber().send().getBlockNumber().longValueExact();
        return (int) Math.max(0L, latest - minedAt);
    }

    private synchronized void ensureConnected() throws IOException {
        if (web3j != null) {
            return;
        }
        Web3j client;
        EthChainId chainIdResp;
        try {
            client = Web3j.build(new HttpService(config.getPrivateRpcEndpoint()));
            chainIdResp = client.ethChainId().send();
        } catch (IOException e) {
            throw new RuntimeException("Besu RPC unreachable: " + e.getMessage(), e);
        }
        if (chainIdResp.hasError()) {
            client.shutdown();
            throw new IOException("Besu eth_chainId error: " + chainIdResp.getError().getMessage());
        }
        long actual = chainIdResp.getChainId().longValueExact();
        long expected = Long.parseLong(config.getPrivateNetworkId());
        if (actual != expected) {
            client.shutdown();
            throw new IOException("Besu chainId mismatch: endpoint has " + actual + ", config expects " + expected);
        }
        credentials = Credentials.create(config.getPrivateAccountKey());
        web3j = client;
    }

    private static byte[] hexOrUtf8(String data) {
        if (data != null && data.startsWith("0x") && data.length() % 2 == 0) {
            return Numeric.hexStringToByteArray(data);
        }
        return data == null ? new byte[0] : data.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }
}
