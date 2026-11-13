package jakarta.ecofarm_backend.modules.ver.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@ApplicationScoped
public class PolygonRpcClient {

    private static final Logger log = LogManager.getLogger(PolygonRpcClient.class);

    // 4 byte dau cua keccak256("root()")
    private static final String ROOT_SELECTOR = "0xebf9014e";

    @Inject
    @ConfigProperty(name = "nlu.eco.farm.ver.rpc.url", defaultValue = "https://rpc-amoy.polygon.technology")
    private String rpcUrl;

    @Inject
    @ConfigProperty(name = "nlu.eco.farm.ver.rpc.timeout-ms", defaultValue = "5000")
    private Integer timeoutMs;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    /** eth_call root() den contract anchor; tra ve bytes32 hoac null khi RPC loi/revert. */
    public String callRoot(String contractAddress) {
        if (contractAddress == null || contractAddress.isBlank()) {
            return null;
        }
        try {
            ObjectNode body = mapper.createObjectNode();
            body.put("jsonrpc", "2.0");
            body.put("id", 1);
            body.put("method", "eth_call");
            ObjectNode params = body.putArray("params").addObject();
            params.put("to", contractAddress);
            params.put("data", ROOT_SELECTOR);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(rpcUrl))
                    .timeout(Duration.ofMillis(timeoutMs))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = mapper.readTree(response.body());
            String result = root.path("result").asText(null);
            if (result == null || result.isBlank() || "0x".equals(result)) {
                return null;
            }
            return result;
        } catch (Exception e) {
            log.warn("Polygon RPC root() failed: {}", e.getMessage());
            return null;
        }
    }
}
