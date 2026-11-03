# Wikipedia Search Engine & Downloader

A Java desktop application with a rich graphical user interface (Swing) to search any Wikipedia article, view its figure (photo) and summary, explore article sections, read the complete content, and download/save articles locally.

---

## Features

- **Search by Name or Topic**: Search for any person, concept, place, or historical event. Includes automatic typo fallback and redirects.
- **Figure & Photo Display**: Automatically downloads and renders the subject's portrait or figure with aspect-ratio preservation and custom User-Agent headers.
- **Overview & Summary**: Displays the title, subtitle/description, and concise introductory summary.
- **Table of Contents**: Instant browsing of all article sections and sub-headings.
- **Full Article View**: Read the full plain-text article from beginning to end without ads or markup noise.
- **Save Article**: Download and export articles (title, description, summary, sections) directly into `data/articles/`.
- **Search History**: Automatically logs searches to `data/history.txt`.
- **Responsive UI**: Asynchronous background network processing (`SwingWorker`) ensures the interface never freezes during queries.

---

## Project Structure

```
wikipedia_downloader/
│
├── data/
│   ├── articles/          # Saved Wikipedia articles (.txt)
│   └── history.txt        # Search history log
│
├── lib/
│   ├── json-20260522.jar  # org.json parsing library
│   └── jsoup-1.22.2.jar   # HTML parser
│
└── src/
    ├── model/
    │   └── WikiArticle.java    # Article data model
    ├── service/
    │   └── WikiService.java    # Wikipedia API integration & figure downloader
    ├── ui/
    │   └── WikiGUI.java        # Swing GUI interface
    ├── utils/
    │   ├── FileManager.java    # Local article persistence
    │   └── SearchHistory.java  # Search history manager
    └── Main.java               # Application entry point
```

---

## How to Compile & Run

### Prerequisites
- Java Development Kit (JDK 17 or newer, tested with JDK 24)

### 1. Compile
```powershell
javac -cp "lib/*;src" src/Main.java src/model/*.java src/service/*.java src/ui/*.java src/utils/*.java
```

### 2. Run
```powershell
java -cp "lib/*;src" Main
```

