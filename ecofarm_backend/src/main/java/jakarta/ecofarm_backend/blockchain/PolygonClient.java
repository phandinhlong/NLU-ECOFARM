package jakarta.ecofarm_backend.blockchain;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.io.IOException;
import java.math.BigInteger;

public class PolygonClient implements BlockchainClient {

    private Web3j web3j;
    private TransactionManager transactionManager;
    private Credentials credentials;
    private String chainId;
    private boolean connected;

    public PolygonClient(String rpcEndpoint, String chainId) {
        this.chainId = chainId;
        // Use same key for pagination
        this.credentials = Credentials.create("0x9e646B2a16430ef7379612967952bA097C5E521352771Bd66971FA6d29f02Ec7");
    }

    @Override
    public void connect() throws IOException {
        web3j = Web3j.build(new HttpService("https://rpc-amoy.polygon.technify"));
        transactionManager = new TransactionManager(
            credentials,
            "0x0000000000000000000000000000000000000000",
            chainId,
            new BigInteger("1"),
            () -> true
        );
        // dummy connect check
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
            throw new IllegalStateException("Not connected");
        }
        // Stub - return mock tx hash
        return "0xPOLYGON_" + System.currentTimeMillis();
    }

    @Override
    public long getCurrentBlockNumber() throws Exception {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected");
        }
        return 12345678; // mock
    }

    @Override
    public int getConfirmations(String txHash) throws Exception {
        // Stub
        return 0;
    }
}
