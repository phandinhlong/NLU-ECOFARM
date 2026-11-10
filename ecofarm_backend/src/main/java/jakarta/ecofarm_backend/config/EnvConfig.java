package jakarta.ecofarm_backend.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class EnvConfig {
    private static final Map<String, String> ENV = new HashMap<>();

    static {
        // 1. Ưu tiên đọc từ classpath (src/main/resources/.env trong file WAR)
        boolean loaded = false;
        try (InputStream is = EnvConfig.class.getClassLoader().getResourceAsStream(".env")) {
            if (is != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    parseReader(reader);
                    loaded = true;
                }
            }
        } catch (Exception e) {
            System.err.println("EnvConfig: Không đọc được .env từ classpath: " + e.getMessage());
        }

        // 2. Nếu không có trong classpath, tìm các đường dẫn trên ổ đĩa
        if (!loaded) {
            String[] candidatePaths = {
                    "src/.env",
                    ".env",
                    "/home/vinh/Vinh/Coder/NLU-ECOFARM/ecofarm_backend/src/.env"
            };

            for (String path : candidatePaths) {
                File file = new File(path);
                if (file.exists() && file.isFile()) {
                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        parseReader(reader);
                        loaded = true;
                        break;
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        // 3. Nạp biến môi trường hệ thống (nếu có biến nào ghi đè)
        ENV.putAll(System.getenv());
    }

    private static void parseReader(BufferedReader reader) throws Exception {
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            String[] parts = line.split("=", 2);
            if (parts.length == 2) {
                String key = parts[0].trim();
                String value = parts[1].trim();
                // Bỏ dấu nháy kép hoặc nháy đơn nếu có
                if ((value.startsWith("\"") && value.endsWith("\"")) ||
                        (value.startsWith("'") && value.endsWith("'"))) {
                    value = value.substring(1, value.length() - 1);
                }
                ENV.put(key, value);
            }
        }
    }

    public static String getKey(String key) {
        return ENV.get(key);
    }
}