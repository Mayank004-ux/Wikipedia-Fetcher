package ui;

import model.WikiArticle;
import service.WikiService;
import utils.FileManager;
import utils.SearchHistory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;

@SuppressWarnings("serial")
public class WikiGUI extends JFrame {

    private static final long serialVersionUID = 1L;

    private final transient WikiService wikiService;
    private transient WikiArticle currentArticle;

    // Theme state
    private boolean isDarkMode = false;

    // UI Panels
    private JPanel topPanel;
    private JLabel appTitle;
    private JTextField searchField;
    private JButton searchButton;
    private JButton saveButton;
    private JButton themeToggleButton;

    private JPanel leftPanel;
    private JLabel contentsHeader;
    private DefaultListModel<String> headingModel;
    private JList<String> headingList;
    private JScrollPane leftScroll;

    private JTextArea resultArea;
    private JScrollPane resultScroll;

    private JPanel figurePanel;
    private JLabel figureHeader;
    private JLabel imageLabel;
    private JScrollPane imageScroll;
    private JLabel imageTitleLabel;
    private JLabel imageDescLabel;

    private JSplitPane centerSplit;
    private JSplitPane mainSplit;

    private JPanel bottomBar;
    private JLabel statusLabel;

    @SuppressWarnings("this-escape")
    public WikiGUI() {
        wikiService = new WikiService();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        loadThemePreference();
        initUI();
        applyTheme();
    }

    private void initUI() {
        setTitle("Wikipedia Search Engine");
        setSize(1350, 850);
        setMinimumSize(new Dimension(900, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // =====================================================
        // 1. TOP SEARCH & ACTION BAR
        // =====================================================
        topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBorder(new EmptyBorder(12, 16, 12, 16));

        appTitle = new JLabel("Wikipedia Explorer");
        appTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JPanel searchControlPanel = new JPanel(new BorderLayout(8, 0));
        searchControlPanel.setOpaque(false);

        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        searchField.setToolTipText("Enter a person, place, or concept to search...");
        searchField.setPreferredSize(new Dimension(400, 36));

        searchButton = new JButton("Search");
        searchButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        searchButton.setPreferredSize(new Dimension(100, 36));
        searchButton.setFocusPainted(false);

        saveButton = new JButton("Save Article");
        saveButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        saveButton.setPreferredSize(new Dimension(110, 36));
        saveButton.setEnabled(false);
        saveButton.setFocusPainted(false);
        saveButton.setToolTipText("Save current article and summary to data/articles/");

        themeToggleButton = new JButton(isDarkMode ? "☀️ Light Mode" : "🌙 Dark Mode");
        themeToggleButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        themeToggleButton.setPreferredSize(new Dimension(120, 36));
        themeToggleButton.setFocusPainted(false);
        themeToggleButton.setToolTipText("Toggle between Dark Mode and Light Mode");

        JPanel buttonBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        buttonBox.setOpaque(false);
        buttonBox.add(searchButton);
        buttonBox.add(saveButton);
        buttonBox.add(themeToggleButton);

        searchControlPanel.add(searchField, BorderLayout.CENTER);
        searchControlPanel.add(buttonBox, BorderLayout.EAST);

        topPanel.add(appTitle, BorderLayout.WEST);
        topPanel.add(searchControlPanel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        // =====================================================
        // 2. LEFT PANEL: TABLE OF CONTENTS / SECTIONS
        // =====================================================
        headingModel = new DefaultListModel<>();
        headingList = new JList<>(headingModel);
        headingList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        headingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        headingList.setFixedCellHeight(28);

        leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

        contentsHeader = new JLabel("Table of Contents");
        contentsHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
        contentsHeader.setBorder(new EmptyBorder(4, 4, 8, 4));
        leftPanel.add(contentsHeader, BorderLayout.NORTH);

        leftScroll = new JScrollPane(headingList);
        leftScroll.setPreferredSize(new Dimension(260, 0));
        leftPanel.add(leftScroll, BorderLayout.CENTER);

        // =====================================================
        // 3. CENTER PANEL: ARTICLE CONTENT & SUMMARY
        // =====================================================
        resultArea = new JTextArea();
        resultArea.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setEditable(false);
        resultArea.setMargin(new Insets(16, 20, 16, 20));
        resultArea.setText("Welcome to Wikipedia Explorer!\n\n"
                + "Type the name of any person, topic, or event in the search box above and press Search.\n"
                + "The article's figure (photo), summary, and detailed content will be displayed here.");

        resultScroll = new JScrollPane(resultArea);

        // =====================================================
        // 4. RIGHT PANEL: FIGURE & PROFILE CARD
        // =====================================================
        figurePanel = new JPanel(new BorderLayout(8, 8));
        figurePanel.setPreferredSize(new Dimension(340, 0));
        figurePanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        figureHeader = new JLabel("Figure / Photo", JLabel.CENTER);
        figureHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
        figurePanel.add(figureHeader, BorderLayout.NORTH);

        imageLabel = new JLabel("No figure loaded", JLabel.CENTER);
        imageLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        imageLabel.setPreferredSize(new Dimension(320, 360));
        imageLabel.setOpaque(true);

        imageScroll = new JScrollPane(imageLabel);
        imageScroll.setBorder(null);
        figurePanel.add(imageScroll, BorderLayout.CENTER);

        JPanel imageMetaPanel = new JPanel();
        imageMetaPanel.setLayout(new BoxLayout(imageMetaPanel, BoxLayout.Y_AXIS));
        imageMetaPanel.setOpaque(false);
        imageMetaPanel.setBorder(new EmptyBorder(8, 4, 4, 4));

        imageTitleLabel = new JLabel("", JLabel.CENTER);
        imageTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        imageTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));

        imageDescLabel = new JLabel("", JLabel.CENTER);
        imageDescLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        imageDescLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));

        imageMetaPanel.add(imageTitleLabel);
        imageMetaPanel.add(Box.createVerticalStrut(4));
        imageMetaPanel.add(imageDescLabel);

        figurePanel.add(imageMetaPanel, BorderLayout.SOUTH);

        // =====================================================
        // 5. SPLIT PANES
        // =====================================================
        centerSplit = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                resultScroll,
                figurePanel
        );
        centerSplit.setResizeWeight(0.70);
        centerSplit.setDividerLocation(680);

        mainSplit = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                leftPanel,
                centerSplit
        );
        mainSplit.setDividerLocation(260);

        add(mainSplit, BorderLayout.CENTER);

        // =====================================================
        // 6. BOTTOM STATUS BAR
        // =====================================================
        bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBorder(new EmptyBorder(6, 16, 6, 16));

        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        bottomBar.add(statusLabel, BorderLayout.WEST);
        add(bottomBar, BorderLayout.SOUTH);

        // =====================================================
        // 7. EVENT LISTENERS
        // =====================================================
        searchButton.addActionListener(e -> performSearch());
        searchField.addActionListener(e -> performSearch());
        saveButton.addActionListener(e -> saveCurrentArticle());
        themeToggleButton.addActionListener(e -> toggleTheme());

        headingList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && currentArticle != null) {
                displaySelectedSection();
            }
        });

        getRootPane().setDefaultButton(searchButton);
        setVisible(true);
    }

    // =====================================================
    // THEME SWITCHING (DARK / LIGHT MODE)
    // =====================================================

    private void toggleTheme() {
        isDarkMode = !isDarkMode;
        applyTheme();
        saveThemePreference();
    }

    private void applyTheme() {
        Color bgTopBar;
        Color bgContent;
        Color bgSidePanels;
        Color bgCard;
        Color bgInput;
        Color textPrimary;
        Color textSecondary;
        Color textBody;
        Color accentColor;
        Color borderColor;
        Color listSelectionBg;
        Color listSelectionFg;
        Color buttonBg;

        if (isDarkMode) {
            bgTopBar = new Color(28, 33, 40);
            bgContent = new Color(22, 27, 34);
            bgSidePanels = new Color(28, 33, 40);
            bgCard = new Color(33, 38, 45);
            bgInput = new Color(33, 38, 45);
            textPrimary = new Color(240, 246, 252);
            textSecondary = new Color(139, 148, 158);
            textBody = new Color(201, 209, 217);
            accentColor = new Color(46, 120, 210);
            borderColor = new Color(48, 54, 61);
            listSelectionBg = new Color(31, 75, 133);
            listSelectionFg = Color.WHITE;
            buttonBg = new Color(45, 52, 64);
            themeToggleButton.setText("☀️ Light Mode");
        } else {
            bgTopBar = new Color(245, 247, 250);
            bgContent = new Color(255, 255, 255);
            bgSidePanels = new Color(248, 249, 252);
            bgCard = new Color(255, 255, 255);
            bgInput = new Color(255, 255, 255);
            textPrimary = new Color(25, 42, 86);
            textSecondary = new Color(90, 100, 115);
            textBody = new Color(33, 37, 41);
            accentColor = new Color(41, 128, 185);
            borderColor = new Color(220, 224, 230);
            listSelectionBg = new Color(218, 234, 250);
            listSelectionFg = new Color(25, 42, 86);
            buttonBg = new Color(238, 242, 246);
            themeToggleButton.setText("🌙 Dark Mode");
        }

        // Apply colors to all components
        getContentPane().setBackground(bgSidePanels);

        topPanel.setBackground(bgTopBar);
        appTitle.setForeground(textPrimary);

        searchField.setBackground(bgInput);
        searchField.setForeground(textBody);
        searchField.setCaretColor(textBody);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(borderColor, 1),
                new EmptyBorder(4, 10, 4, 10)
        ));

        Color paleYellow = new Color(254, 240, 138);
        Color buttonTextColor = Color.BLACK;
        Color paleYellowBorder = new Color(225, 205, 110);

        searchButton.setBackground(paleYellow);
        searchButton.setForeground(buttonTextColor);
        searchButton.setBorder(new LineBorder(paleYellowBorder, 1));

        saveButton.setBackground(paleYellow);
        saveButton.setForeground(buttonTextColor);
        saveButton.setBorder(new LineBorder(paleYellowBorder, 1));

        themeToggleButton.setBackground(paleYellow);
        themeToggleButton.setForeground(buttonTextColor);
        themeToggleButton.setBorder(new LineBorder(paleYellowBorder, 1));

        leftPanel.setBackground(bgSidePanels);
        contentsHeader.setForeground(textPrimary);

        headingList.setBackground(bgInput);
        headingList.setForeground(textBody);
        headingList.setSelectionBackground(listSelectionBg);
        headingList.setSelectionForeground(listSelectionFg);
        leftScroll.getViewport().setBackground(bgInput);
        leftScroll.setBorder(new LineBorder(borderColor, 1));

        resultArea.setBackground(bgContent);
        resultArea.setForeground(textBody);
        resultArea.setCaretColor(textBody);
        resultScroll.getViewport().setBackground(bgContent);
        resultScroll.setBorder(new LineBorder(borderColor, 1));

        figurePanel.setBackground(bgSidePanels);
        figureHeader.setForeground(textSecondary);

        imageLabel.setBackground(bgCard);
        imageLabel.setForeground(textSecondary);
        imageLabel.setBorder(new LineBorder(borderColor, 1));
        imageScroll.getViewport().setBackground(bgCard);

        imageTitleLabel.setForeground(textPrimary);
        imageDescLabel.setForeground(textSecondary);

        centerSplit.setBackground(bgSidePanels);
        centerSplit.setBorder(null);

        mainSplit.setBackground(bgSidePanels);
        mainSplit.setBorder(null);

        bottomBar.setBackground(bgTopBar);
        statusLabel.setForeground(textSecondary);

        repaint();
        revalidate();
    }

    private void loadThemePreference() {
        File file = new File("data/theme.txt");
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
                String line = reader.readLine();
                if (line != null && line.trim().equalsIgnoreCase("dark")) {
                    isDarkMode = true;
                }
            } catch (Exception ignored) {
            }
        }
    }

    private void saveThemePreference() {
        try {
            File file = new File("data/theme.txt");
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
                writer.write(isDarkMode ? "dark" : "light");
            }
        } catch (Exception ignored) {
        }
    }

    // =====================================================
    // SEARCH EXECUTION (ASYNC WORKER)
    // =====================================================

    private void performSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a name or topic to search.",
                    "Input Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        searchButton.setEnabled(false);
        searchButton.setText("Searching...");
        saveButton.setEnabled(false);
        statusLabel.setText("Searching Wikipedia for \"" + query + "\"...");
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<SearchResult, Void> worker = new SwingWorker<>() {
            @Override
            protected SearchResult doInBackground() throws Exception {
                WikiArticle article = wikiService.searchArticle(query);
                BufferedImage image = null;
                if (!article.getImageUrl().isEmpty()) {
                    image = wikiService.downloadImage(article.getImageUrl());
                }
                return new SearchResult(article, image);
            }

            @Override
            protected void done() {
                try {
                    SearchResult result = get();
                    displayArticle(result.article, result.image);
                    SearchHistory.saveSearch(query);
                    statusLabel.setText("Found: " + result.article.getTitle()
                            + (result.article.getDescription().isEmpty() ? "" : " — " + result.article.getDescription()));
                } catch (Exception ex) {
                    String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    if (msg == null || msg.isBlank()) {
                        msg = "An unexpected error occurred while searching Wikipedia.";
                    }
                    resultArea.setText("Search failed: " + msg);
                    imageLabel.setIcon(null);
                    imageLabel.setText("No figure available");
                    imageTitleLabel.setText("");
                    imageDescLabel.setText("");
                    headingModel.clear();
                    statusLabel.setText("Error: " + msg);
                    JOptionPane.showMessageDialog(WikiGUI.this,
                            msg,
                            "Search Error",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    searchButton.setEnabled(true);
                    searchButton.setText("Search");
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };

        worker.execute();
    }

    // =====================================================
    // DISPLAY ARTICLE CONTENT & FIGURE
    // =====================================================

    private void displayArticle(WikiArticle article, BufferedImage image) {
        this.currentArticle = article;
        this.saveButton.setEnabled(true);

        headingModel.clear();
        headingModel.addElement("📋 Summary & Overview");
        if (!article.getFullContent().isEmpty()) {
            headingModel.addElement("📖 Full Article Content");
        }
        for (String heading : article.getSections().keySet()) {
            headingModel.addElement(heading);
        }
        headingList.setSelectedIndex(0);

        if (image != null) {
            imageLabel.setText("");
            int maxW = 310;
            int maxH = 370;
            int origW = image.getWidth();
            int origH = image.getHeight();
            double scale = Math.min((double) maxW / origW, (double) maxH / origH);
            int targetW = Math.max(1, (int) (origW * scale));
            int targetH = Math.max(1, (int) (origH * scale));

            Image scaled = image.getScaledInstance(targetW, targetH, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaled));
        } else {
            imageLabel.setIcon(null);
            imageLabel.setText("No figure available");
        }

        imageTitleLabel.setText(article.getTitle());
        imageDescLabel.setText(article.getDescription());

        renderOverview();
    }

    private void renderOverview() {
        if (currentArticle == null) return;

        StringBuilder sb = new StringBuilder();
        sb.append(currentArticle.getTitle().toUpperCase()).append("\n");
        if (!currentArticle.getDescription().isEmpty()) {
            sb.append(currentArticle.getDescription()).append("\n");
        }
        sb.append("======================================================================\n\n");

        sb.append("SUMMARY\n");
        sb.append("----------------------------------------------------------------------\n");
        sb.append(currentArticle.getSummary()).append("\n\n");

        if (!currentArticle.getSections().isEmpty()) {
            sb.append("AVAILABLE SECTIONS (Select from Table of Contents on the left):\n");
            sb.append("----------------------------------------------------------------------\n");
            int i = 1;
            for (String section : currentArticle.getSections().keySet()) {
                sb.append(i++).append(". ").append(section).append("\n");
            }
        }

        resultArea.setText(sb.toString());
        resultArea.setCaretPosition(0);
    }

    private void displaySelectedSection() {
        String selected = headingList.getSelectedValue();
        if (selected == null || currentArticle == null) return;

        if (selected.equals("📋 Summary & Overview")) {
            renderOverview();
        } else if (selected.equals("📖 Full Article Content")) {
            StringBuilder sb = new StringBuilder();
            sb.append("FULL ARTICLE: ").append(currentArticle.getTitle()).append("\n");
            sb.append("======================================================================\n\n");
            sb.append(currentArticle.getFullContent());
            resultArea.setText(sb.toString());
            resultArea.setCaretPosition(0);
        } else {
            String sectionContent = currentArticle.getSections().get(selected);
            if (sectionContent == null || sectionContent.isBlank()) {
                sectionContent = "No details available for this section.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("TITLE: ").append(currentArticle.getTitle()).append("\n");
            sb.append("SECTION: ").append(selected).append("\n");
            sb.append("======================================================================\n\n");
            sb.append(sectionContent);
            resultArea.setText(sb.toString());
            resultArea.setCaretPosition(0);
        }
    }

    // =====================================================
    // SAVE ARTICLE ACTION
    // =====================================================

    private void saveCurrentArticle() {
        if (currentArticle == null) return;

        try {
            File savedFile = FileManager.saveArticle(currentArticle);
            statusLabel.setText("Article saved to " + savedFile.getPath());
            JOptionPane.showMessageDialog(this,
                    "Article saved successfully to:\n" + savedFile.getAbsolutePath(),
                    "Saved",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to save article: " + ex.getMessage(),
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Helper holder class for SwingWorker
    private static class SearchResult {
        final WikiArticle article;
        final BufferedImage image;

        SearchResult(WikiArticle article, BufferedImage image) {
            this.article = article;
            this.image = image;
        }
    }
}
