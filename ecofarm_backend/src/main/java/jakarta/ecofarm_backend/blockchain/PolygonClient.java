package jakarta.ecofarm_backend.blockchain;

import java.io.IOException;
import java.math.BigInteger;

public class PolygonClient implements BlockchainClient {

    private boolean connected;

    public PolygonClient(String rpcEndpoint, String chainId) {
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
            throw new IllegalStateException("Not connected");
        }
        return "0xPOLYGON_" + System.currentTimeMillis();
    }

    @Override
    public long getCurrentBlockNumber() {
        return 0; // stub
    }

    @Override
    public int getConfirmations(String txHash) throws Exception {
        return 0;
    }
}
