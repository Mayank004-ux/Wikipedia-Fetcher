package service;

import model.WikiArticle;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class WikiService {

    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    @SuppressWarnings("deprecation")
    private static URL toURL(String urlString) throws Exception {
        String clean = urlString.replace(" ", "%20");
        try {
            return URI.create(clean).toURL();
        } catch (Exception e) {
            return new URL(clean);
        }
    }

    // =====================================================
    // DOWNLOAD JSON STRING FROM URL
    // =====================================================

    private String downloadJson(String urlString) throws Exception {
        URL url = toURL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", USER_AGENT);
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);

        int status = connection.getResponseCode();
        if (status >= 400) {
            connection.disconnect();
            return null;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        } finally {
            connection.disconnect();
        }
    }

    // =====================================================
    // DOWNLOAD IMAGE (FIGURE)
    // =====================================================

    public BufferedImage downloadImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }
        try {
            if (imageUrl.startsWith("//")) {
                imageUrl = "https:" + imageUrl;
            }
            URL url = toURL(imageUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            try (InputStream in = conn.getInputStream()) {
                return ImageIO.read(in);
            } finally {
                conn.disconnect();
            }
        } catch (Exception ex) {
            return null;
        }
    }

    // =====================================================
    // SEARCH ARTICLE
    // =====================================================

    public WikiArticle searchArticle(String query) throws Exception {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Search term cannot be empty.");
        }

        String trimmed = query.trim();
        String slug = URLEncoder.encode(trimmed.replace(" ", "_"), StandardCharsets.UTF_8);

        // 1. Try Wikipedia REST API summary
        String summaryUrl = "https://en.wikipedia.org/api/rest_v1/page/summary/" + slug;
        String jsonResponse = downloadJson(summaryUrl);

        // 2. If direct lookup returns null (404), fallback to Wikipedia OpenSearch
        if (jsonResponse == null) {
            String searchUrl = "https://en.wikipedia.org/w/api.php?action=opensearch&search="
                    + URLEncoder.encode(trimmed, StandardCharsets.UTF_8)
                    + "&limit=3&namespace=0&format=json";
            String searchRes = downloadJson(searchUrl);

            if (searchRes != null) {
                JSONArray searchArr = new JSONArray(searchRes);
                if (searchArr.length() > 1 && searchArr.getJSONArray(1).length() > 0) {
                    String resolvedTitle = searchArr.getJSONArray(1).getString(0);
                    String resolvedSlug = URLEncoder.encode(resolvedTitle.replace(" ", "_"), StandardCharsets.UTF_8);
                    jsonResponse = downloadJson("https://en.wikipedia.org/api/rest_v1/page/summary/" + resolvedSlug);
                }
            }
        }

        if (jsonResponse == null) {
            throw new Exception("No Wikipedia article found for \"" + trimmed + "\".");
        }

        JSONObject summaryJson = new JSONObject(jsonResponse);
        String title = summaryJson.optString("title", trimmed);
        String description = summaryJson.optString("description", "");
        String summary = summaryJson.optString("extract", "");

        // Extract thumbnail image (figure)
        String imageUrl = "";
        if (summaryJson.has("thumbnail")) {
            imageUrl = summaryJson.getJSONObject("thumbnail").optString("source", "");
        } else if (summaryJson.has("originalimage")) {
            imageUrl = summaryJson.getJSONObject("originalimage").optString("source", "");
        }

        // 3. Fetch full content and sections
        Map<String, String> sections = new LinkedHashMap<>();
        String fullContent = "";

        String fullContentUrl = "https://en.wikipedia.org/w/api.php?action=query&prop=extracts&explaintext=1&titles="
                + URLEncoder.encode(title.replace(" ", "_"), StandardCharsets.UTF_8)
                + "&format=json";

        String fullRes = downloadJson(fullContentUrl);
        if (fullRes != null) {
            JSONObject fullJson = new JSONObject(fullRes);
            JSONObject queryObj = fullJson.optJSONObject("query");
            if (queryObj != null && queryObj.has("pages")) {
                JSONObject pages = queryObj.getJSONObject("pages");
                for (String key : pages.keySet()) {
                    if (!"-1".equals(key)) {
                        JSONObject page = pages.getJSONObject(key);
                        fullContent = page.optString("extract", "");
                        break;
                    }
                }
            }
        }

        if (!fullContent.isEmpty()) {
            // Split fullContent into sections using markdown / wiki heading lines
            String currentHeading = "Introduction";
            StringBuilder currentText = new StringBuilder();

            for (String line : fullContent.split("\n")) {
                if (line.matches("^={2,}\\s+.+?\\s+={2,}$")) {
                    if (currentText.length() > 0 && !currentHeading.equals("Introduction")) {
                        sections.put(currentHeading, currentText.toString().trim());
                    }
                    currentHeading = line.replaceAll("^=+\\s*", "").replaceAll("\\s*=+$", "").trim();
                    currentText.setLength(0);
                } else {
                    currentText.append(line).append("\n");
                }
            }
            if (currentText.length() > 0 && !currentHeading.equals("Introduction")) {
                sections.put(currentHeading, currentText.toString().trim());
            }

            if (summary.isEmpty() && fullContent.length() > 0) {
                // If summary wasn't in REST response, extract intro text
                int firstHeader = fullContent.indexOf("\n==");
                summary = (firstHeader > 0) ? fullContent.substring(0, firstHeader).trim() : fullContent.trim();
            }
        }

        return new WikiArticle(title, description, summary, imageUrl, sections, fullContent);
    }
}
