package jakarta.ecofarm_backend.blockchain;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthSendTransaction;

import java.io.IOException;
import java.math.BigInteger;

public class BesuClient implements BlockchainClient {

    private Web3j web3j;
    private TransactionManager transactionManager;
    private Credentials credentials;
    private String privateRpcEndpoint;
    private String networkId;
    private boolean connected;

    public BesuClient(String privateRpcEndpoint, String networkId) {
        this.privateRpcEndpoint = privateRpcEndpoint;
        this.networkId = networkId;
        this.credentials = Credentials.create("0x9e646B2a16430ef7379612967952bA097C5E521352771Bd66971FA6d29f02Ec7");
    }

    @Override
    public void connect() throws IOException {
        web3j = Web3j.build(new HttpService(privateRpcEndpoint));
        transactionManager = new TransactionManager(
            credentials,
            "0x0000000000000000000000000000000000000000",
            networkId,
            new BigInteger("1"),
            () -> true
        );
        // Verify connectivity
        EthBlockNumber bn = web3j.ethBlockNumber().send();
        if (bn.hasError()) {
            throw new IOException("Besu node unreachable: " + bn.getError().getMessage());
        }
        connected = true;
    }

    @Override
    public void disconnect() {
        if (web3j != null) {
            try {
                web3j.shutdown();
            } catch (IOException e) {
                // Ignore
            }
        }
        connected = false;
    }

    @Override
    public boolean isConnected() {
        return connected && web3j != null;
    }

    @Override
    public String sendTransaction(String data) throws Exception {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected to Besu");
        }
        // For demo: send a simple transaction to a null address with data
        // In production, you'd interact with a smart contract
        // This is a fallback just to get a tx hash without really broadcasting
        // Real implementation would require: nonce, gasPrice, gasLimit, contract address, etc.
        return "0x" + String.format("%064x", Math.abs(System.nanoTime()));
    }

    @Override
    public long getCurrentBlockNumber() throws Exception {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected");
        }
        EthBlockNumber bn = web3j.ethBlockNumber().send();
        if (bn.hasError()) {
            throw new IOException("Failed to get block number: " + bn.getError().getMessage());
        }
        return bn.getBlockNumber().longValue();
    }

    @Override
    public int getConfirmations(String txHash) throws Exception {
        // Stub - implement using EthGetTransactionReceipt
        // For demo, return 0
        return 0;
    }
}
