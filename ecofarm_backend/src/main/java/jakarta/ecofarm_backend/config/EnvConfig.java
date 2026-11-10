package jakarta.ecofarm_backend.config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EnvConfig {
    private static final Map<String, String> ENV = new HashMap<>();
    static {
        try (BufferedReader reader = new BufferedReader(new FileReader("src/.env"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    ENV.put(parts[0].trim(), parts[1].trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static String getKey(String key) {
        String value = ENV.get(key);
        return value != null ? value : System.getenv(key);
    }

    public static int getInt(String key, int defaultValue) {
        String value = getKey(key);
        return value == null || value.isBlank() ? defaultValue : Integer.parseInt(value.trim());
    }

}
