package jakarta.ecofarm_backend.blockchain;

import java.io.IOException;
import java.math.BigInteger;

public class BesuClient implements BlockchainClient {

    private boolean connected;

    public BesuClient(String privateRpcEndpoint, String networkId) {
        this.connected = false;
    }

    @Override
    public void connect() throws IOException {
        connected = true;
    }

    @Override
    public void disconnect() {
        connected = false;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public String sendTransaction(String data) throws Exception {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected to Besu");
        }
        // Return a mock tx hash
        return "0x" + String.valueOf(System.nanoTime());
    }

    @Override
    public long getCurrentBlockNumber() {
        return 0; // mock
    }

    @Override
    public int getConfirmations(String txHash) throws Exception {
        return 0; // stub
    }
}
