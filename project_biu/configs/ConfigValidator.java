package configs;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Validator utility to inspect configuration data format and detect dependency cycles
 * directly from parsed topic flows without instantiating computational graphs.
 */
public class ConfigValidator {

    /**
     * Data class containing the result of a validation check.
     */
    public static class ValidationResult {
        public final boolean isValid;
        public final String errorMessage;

        public ValidationResult(boolean isValid, String errorMessage) {
            this.isValid = isValid;
            this.errorMessage = errorMessage;
        }
    }

    /**
     * Validates configuration data for proper format (multiple of 3 lines) and
     * performs cycle detection on the resulting topic dependencies.
     *
     * @param data raw byte array representing the config file
     * @return a ValidationResult indicating whether the configuration is valid
     */
    public static ValidationResult validate(byte[] data) {
        if (data == null || data.length == 0) {
            return new ValidationResult(false, "Empty configuration data");
        }

        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(data), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            return new ValidationResult(false, "Failed to read configuration: " + e.getMessage());
        }

        if (lines.isEmpty()) {
            return new ValidationResult(false, "Configuration has no content");
        }

        // Logic of valid conffile is in GenericConfig (line count must be a multiple of 3)
        if (lines.size() % 3 != 0) {
            return new ValidationResult(false, "Invalid configuration file format: line count is not a multiple of 3");
        }

        // Build direct topic-to-topic dependency graph (subTopic -> pubTopic)
        Map<String, List<String>> adjList = new HashMap<>();

        for (int i = 0; i < lines.size(); i += 3) {
            String subsStr = lines.get(i + 1);
            String pubsStr = lines.get(i + 2);

            String[] subs = subsStr.isEmpty() ? new String[0] : subsStr.split(",");
            String[] pubs = pubsStr.isEmpty() ? new String[0] : pubsStr.split(",");

            for (String sub : subs) {
                String subTrimmed = sub.trim();
                if (subTrimmed.isEmpty()) {
                    continue;
                }
                for (String pub : pubs) {
                    String pubTrimmed = pub.trim();
                    if (pubTrimmed.isEmpty()) {
                        continue;
                    }
                    adjList.putIfAbsent(subTrimmed, new ArrayList<>());
                    adjList.get(subTrimmed).add(pubTrimmed);
                }
            }
        }

        // Cycle detection using DFS with recursion stack tracking
        Set<String> visited = new HashSet<>();
        Set<String> recStack = new HashSet<>();

        for (String topic : adjList.keySet()) {
            if (hasCyclesDFS(topic, adjList, visited, recStack)) {
                return new ValidationResult(false, "The computational graph could not be created because the configuration contains cycles.");
            }
        }

        return new ValidationResult(true, null);
    }

    private static boolean hasCyclesDFS(String node, Map<String, List<String>> adjList, Set<String> visited, Set<String> recStack) {
        if (recStack.contains(node)) {
            return true;
        }
        if (visited.contains(node)) {
            return false;
        }

        visited.add(node);
        recStack.add(node);

        List<String> neighbors = adjList.get(node);
        if (neighbors != null) {
            for (String neighbor : neighbors) {
                if (hasCyclesDFS(neighbor, adjList, visited, recStack)) {
                    return true;
                }
            }
        }

        recStack.remove(node);
        return false;
    }
}
