package utils;

import model.WikiArticle;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class FileManager {

    public static File saveArticle(WikiArticle article) throws Exception {
        File dir = new File("data/articles");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String safeTitle = article.getTitle()
                .replaceAll("[\\\\/:*?\"<>|]", "_")
                .replace(" ", "_");

        File file = new File(dir, safeTitle + ".txt");

        try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            writer.write("TITLE: " + article.getTitle() + "\n");
            if (!article.getDescription().isEmpty()) {
                writer.write("DESCRIPTION: " + article.getDescription() + "\n");
            }
            writer.write("====================================================\n\n");

            writer.write("SUMMARY:\n");
            writer.write("----------------------------------------------------\n");
            writer.write(article.getSummary() + "\n\n");

            if (!article.getSections().isEmpty()) {
                writer.write("SECTIONS:\n");
                writer.write("----------------------------------------------------\n");
                for (Map.Entry<String, String> entry : article.getSections().entrySet()) {
                    writer.write("## " + entry.getKey() + "\n");
                    writer.write(entry.getValue() + "\n\n");
                }
            } else if (!article.getFullContent().isEmpty()) {
                writer.write("CONTENT:\n");
                writer.write("----------------------------------------------------\n");
                writer.write(article.getFullContent() + "\n");
            }
        }

        return file;
    }
}
