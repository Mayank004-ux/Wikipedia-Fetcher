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
import java.io.File;

@SuppressWarnings("serial")
public class WikiGUI extends JFrame {

    private static final long serialVersionUID = 1L;

    private final transient WikiService wikiService;
    private transient WikiArticle currentArticle;

    // UI Components
    private JTextField searchField;
    private JButton searchButton;
    private JButton saveButton;
    private JLabel statusLabel;

    private DefaultListModel<String> headingModel;
    private JList<String> headingList;

    private JTextArea resultArea;

    private JLabel imageLabel;
    private JLabel imageTitleLabel;
    private JLabel imageDescLabel;

    @SuppressWarnings("this-escape")
    public WikiGUI() {
        wikiService = new WikiService();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        initUI();
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
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBorder(new EmptyBorder(12, 16, 12, 16));
        topPanel.setBackground(new Color(245, 247, 250));

        JLabel appTitle = new JLabel("Wikipedia Explorer");
        appTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        appTitle.setForeground(new Color(25, 42, 86));

        JPanel searchControlPanel = new JPanel(new BorderLayout(8, 0));
        searchControlPanel.setOpaque(false);

        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        searchField.setToolTipText("Enter a person, place, or concept to search...");
        searchField.setPreferredSize(new Dimension(400, 36));

        searchButton = new JButton("Search");
        searchButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        searchButton.setPreferredSize(new Dimension(100, 36));
        searchButton.setBackground(new Color(41, 128, 185));
        searchButton.setForeground(Color.WHITE);
        searchButton.setFocusPainted(false);

        saveButton = new JButton("Save Article");
        saveButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        saveButton.setPreferredSize(new Dimension(110, 36));
        saveButton.setEnabled(false);
        saveButton.setToolTipText("Save current article and summary to data/articles/");

        JPanel buttonBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        buttonBox.setOpaque(false);
        buttonBox.add(searchButton);
        buttonBox.add(saveButton);

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

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

        JLabel contentsHeader = new JLabel("Table of Contents");
        contentsHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
        contentsHeader.setBorder(new EmptyBorder(4, 4, 8, 4));
        leftPanel.add(contentsHeader, BorderLayout.NORTH);

        JScrollPane leftScroll = new JScrollPane(headingList);
        leftScroll.setPreferredSize(new Dimension(260, 0));
        leftScroll.setBorder(new LineBorder(new Color(220, 224, 230)));
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
        resultArea.setBackground(Color.WHITE);
        resultArea.setText("Welcome to Wikipedia Explorer!\n\n"
                + "Type the name of any person, topic, or event in the search box above and press Search.\n"
                + "The article's figure (photo), summary, and detailed content will be displayed here.");

        JScrollPane resultScroll = new JScrollPane(resultArea);
        resultScroll.setBorder(new LineBorder(new Color(220, 224, 230)));

        // =====================================================
        // 4. RIGHT PANEL: FIGURE & PROFILE CARD
        // =====================================================
        JPanel figurePanel = new JPanel(new BorderLayout(8, 8));
        figurePanel.setPreferredSize(new Dimension(340, 0));
        figurePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        figurePanel.setBackground(new Color(250, 251, 253));

        JLabel figureHeader = new JLabel("Figure / Photo", JLabel.CENTER);
        figureHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
        figureHeader.setForeground(new Color(70, 80, 95));
        figurePanel.add(figureHeader, BorderLayout.NORTH);

        imageLabel = new JLabel("No figure loaded", JLabel.CENTER);
        imageLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        imageLabel.setForeground(Color.GRAY);
        imageLabel.setPreferredSize(new Dimension(320, 360));
        imageLabel.setBorder(new LineBorder(new Color(230, 233, 238), 1));
        imageLabel.setOpaque(true);
        imageLabel.setBackground(Color.WHITE);

        JScrollPane imageScroll = new JScrollPane(imageLabel);
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
        imageDescLabel.setForeground(new Color(90, 100, 115));

        imageMetaPanel.add(imageTitleLabel);
        imageMetaPanel.add(Box.createVerticalStrut(4));
        imageMetaPanel.add(imageDescLabel);

        figurePanel.add(imageMetaPanel, BorderLayout.SOUTH);

        // =====================================================
        // 5. SPLIT PANES
        // =====================================================
        JSplitPane centerSplit = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                resultScroll,
                figurePanel
        );
        centerSplit.setResizeWeight(0.70);
        centerSplit.setDividerLocation(680);

        JSplitPane mainSplit = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                leftPanel,
                centerSplit
        );
        mainSplit.setDividerLocation(260);

        add(mainSplit, BorderLayout.CENTER);

        // =====================================================
        // 6. BOTTOM STATUS BAR
        // =====================================================
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBorder(new EmptyBorder(6, 16, 6, 16));
        bottomBar.setBackground(new Color(245, 247, 250));

        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(100, 110, 125));

        bottomBar.add(statusLabel, BorderLayout.WEST);
        add(bottomBar, BorderLayout.SOUTH);

        // =====================================================
        // 7. EVENT LISTENERS
        // =====================================================
        searchButton.addActionListener(e -> performSearch());
        searchField.addActionListener(e -> performSearch());
        saveButton.addActionListener(e -> saveCurrentArticle());

        headingList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && currentArticle != null) {
                displaySelectedSection();
            }
        });

        getRootPane().setDefaultButton(searchButton);
        setVisible(true);
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

        // Update UI to searching state
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

        // 1. Populate Headings in Table of Contents
        headingModel.clear();
        headingModel.addElement("📋 Summary & Overview");
        if (!article.getFullContent().isEmpty()) {
            headingModel.addElement("📖 Full Article Content");
        }
        for (String heading : article.getSections().keySet()) {
            headingModel.addElement(heading);
        }
        headingList.setSelectedIndex(0);

        // 2. Display Figure (Image)
        if (image != null) {
            imageLabel.setText("");
            // Scale preserving aspect ratio
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

        // 3. Display Overview by default
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
