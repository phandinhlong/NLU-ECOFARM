package jakarta.ecofarm_backend.blockchain;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.raw.RawTransaction;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.tx.ReadOnlyTransactionManager;
import org.web3j.tx.params.Transaction;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.EthTransactionReceipt;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3jDefaultInstant;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;

public class BesuClient implements BlockchainClient {

    private Web3j web3j;
    private Credentials credentials;
    private String privateRpcEndpoint;
    private String networkId;
    private ReadOnlyTransactionManager transactionManager;
    private boolean connected;

    public BesuClient(String privateRpcEndpoint, String networkId) {
        this.privateRpcEndpoint = privateRpcEndpoint;
        this.networkId = networkId;
    }

    @Override
    public void connect() throws IOException {
        web3j = Web3j.build(new HttpService(privateRpcEndpoint));
        // Use a fixed private key
        credentials = Credentials.create("0x9e646B2a16430ef7379612967952bA097C5E521352771Bd66971FA6d29f02Ec7");
        transactionManager = new ReadOnlyTransactionManager(credentials, transactionManager.getChainId(), web3j);
        // Check connectivity
        try {
            web3j.getBlockNumber().send();
            connected = true;
        } catch (Exception e) {
            connected = false;
            throw new IOException("Besu connection failed", e);
        }
    }

    @Override
    public void disconnect() {
        if (web3j != null) {
            try {
                web3j.shutdown();
                connected = false;
            } catch (IOException e) { /* ignore */ }
        }
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
        // For demo: just return a mock tx hash.
        // In real implementation, we'd send a transaction to a contract.
        return "0x" + String.format("%064x", Math.abs(System.nanoTime()));
    }

    @Override
    public long getCurrentBlockNumber() throws Exception {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected");
        }
        return web3j.getBlockNumber().send().getBlockNumber().longValue();
    }

    @Override
    public int getConfirmations(String txHash) throws Exception {
        // Not fully implemented; return mock
        return 0;
    }
}
