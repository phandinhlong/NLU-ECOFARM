package jakarta.ecofarm_backend.blockchain;

import java.io.IOException;

public interface BlockchainClient {
    void connect() throws IOException;
    void disconnect();
    boolean isConnected();
    String sendTransaction(String data) throws Exception;
    long getCurrentBlockNumber() throws IOException;
    int getConfirmations(String txHash) throws Exception;
}
