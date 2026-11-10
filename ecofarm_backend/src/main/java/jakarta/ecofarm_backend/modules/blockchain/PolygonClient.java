package jakarta.ecofarm_backend.modules.blockchain;

import java.io.IOException;

import jakarta.ecofarm_backend.config.BlockchainConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class PolygonClient implements BlockchainClient {
    @Inject
    BlockchainConfig config;
    private boolean connected;

    public PolygonClient() {
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
