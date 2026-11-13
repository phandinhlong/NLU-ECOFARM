package jakarta.ecofarm_backend.modules.ver.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.web3j.crypto.Hash;
import org.web3j.utils.Numeric;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class MerkleUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private MerkleUtil() {
    }

    public static List<String> parseLeaves(String leavesJson) {
        if (leavesJson == null || leavesJson.isBlank()) {
            return List.of();
        }
        try {
            return MAPPER.readValue(leavesJson, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Dung lai Merkle root kieu Ethereum: hash tung leaf bang keccak256,
     * sau do ghep cap va hash lien tiep den khi con 1 phan tu.
     */
    public static String computeRoot(List<String> leaves) {
        if (leaves == null || leaves.isEmpty()) {
            return null;
        }
        List<byte[]> level = new ArrayList<>();
        for (String leaf : leaves) {
            level.add(Hash.sha3(leaf.getBytes(StandardCharsets.UTF_8)));
        }
        while (level.size() > 1) {
            List<byte[]> next = new ArrayList<>();
            for (int i = 0; i < level.size(); i += 2) {
                byte[] left = level.get(i);
                if (i + 1 < level.size()) {
                    byte[] right = level.get(i + 1);
                    byte[] pair = new byte[left.length + right.length];
                    System.arraycopy(left, 0, pair, 0, left.length);
                    System.arraycopy(right, 0, pair, left.length, right.length);
                    next.add(Hash.sha3(pair));
                } else {
                    next.add(left);
                }
            }
            level = next;
        }
        return Numeric.toHexString(level.get(0));
    }
}
