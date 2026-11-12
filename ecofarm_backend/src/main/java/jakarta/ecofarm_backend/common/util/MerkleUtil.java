package jakarta.ecofarm_backend.common.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.web3j.crypto.Hash;
import org.web3j.utils.Numeric;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class MerkleUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private MerkleUtil() {}

    public static String keccak256Hex(byte[] data) {
        return Numeric.toHexString(Hash.sha3(data));
    }

    public static List<String> parseLeaves(String leavesJson) {
        if (leavesJson == null || leavesJson.isBlank()) return List.of();
        try {
            return MAPPER.readValue(leavesJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    public static String buildRoot(List<String> leaves) {
        if (leaves == null || leaves.isEmpty()) return null;
        List<byte[]> level = new ArrayList<>();
        for (String leaf : leaves) {
            level.add(leafBytes(leaf));
        }
        while (level.size() > 1) {
            List<byte[]> next = new ArrayList<>();
            for (int i = 0; i < level.size(); i += 2) {
                byte[] left = level.get(i);
                byte[] right = (i + 1 < level.size()) ? level.get(i + 1) : left;
                next.add(Hash.sha3(concat(left, right)));
            }
            level = next;
        }
        return Numeric.toHexString(level.get(0));
    }

    public static List<String> buildProof(List<String> leaves, int index) {
        if (leaves == null || leaves.isEmpty() || index < 0 || index >= leaves.size()) {
            return List.of();
        }
        List<byte[]> level = new ArrayList<>();
        for (String leaf : leaves) {
            level.add(leafBytes(leaf));
        }
        List<String> proof = new ArrayList<>();
        int idx = index;
        while (level.size() > 1) {
            List<byte[]> next = new ArrayList<>();
            for (int i = 0; i < level.size(); i += 2) {
                byte[] left = level.get(i);
                byte[] right = (i + 1 < level.size()) ? level.get(i + 1) : left;
                next.add(Hash.sha3(concat(left, right)));
                if (i == idx) {
                    proof.add(Numeric.toHexString(right));
                } else if (i + 1 == idx) {
                    proof.add(Numeric.toHexString(left));
                }
            }
            idx /= 2;
            level = next;
        }
        return proof;
    }

    public static byte[] leafBytes(String leaf) {
        if (leaf != null && leaf.startsWith("0x") && leaf.length() > 2 && leaf.length() % 2 == 0) {
            try {
                return Numeric.hexStringToByteArray(leaf);
            } catch (Exception ignored) {
            }
        }
        return leaf == null ? new byte[0] : leaf.getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] concat(byte[] a, byte[] b) {
        byte[] out = new byte[a.length + b.length];
        System.arraycopy(a, 0, out, 0, a.length);
        System.arraycopy(b, 0, out, a.length, b.length);
        return out;
    }
}
