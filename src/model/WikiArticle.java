package model;

import java.util.Collections;
import java.util.Map;

public class WikiArticle {

    private final String title;
    private final String description;
    private final String summary;
    private final String imageUrl;
    private final Map<String, String> sections;
    private final String fullContent;

    public WikiArticle(
            String title,
            String summary,
            String imageUrl,
            Map<String, String> sections
    ) {
        this(title, "", summary, imageUrl, sections, "");
    }

    public WikiArticle(
            String title,
            String description,
            String summary,
            String imageUrl,
            Map<String, String> sections,
            String fullContent
    ) {
        this.title = title != null ? title : "";
        this.description = description != null ? description : "";
        this.summary = summary != null ? summary : "";
        this.imageUrl = imageUrl != null ? imageUrl : "";
        this.sections = sections != null ? sections : Collections.emptyMap();
        this.fullContent = fullContent != null ? fullContent : "";
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getSummary() {
        return summary;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Map<String, String> getSections() {
        return sections;
    }

    public String getFullContent() {
        return fullContent;
    }
}
