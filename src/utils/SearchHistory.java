package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SearchHistory {

    private static final String HISTORY_FILE = "data/history.txt";

    public static void saveSearch(String query) {
        if (query == null || query.isBlank()) {
            return;
        }
        try {
            File file = new File(HISTORY_FILE);
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8, true)) {
                writer.write(query.trim() + System.lineSeparator());
            }
        } catch (Exception ignored) {
        }
    }

    public static List<String> getHistory() {
        List<String> list = new ArrayList<>();
        File file = new File(HISTORY_FILE);
        if (!file.exists()) {
            return list;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !list.contains(line)) {
                    list.add(line);
                }
            }
        } catch (Exception ignored) {
        }
        return list;
    }
}
