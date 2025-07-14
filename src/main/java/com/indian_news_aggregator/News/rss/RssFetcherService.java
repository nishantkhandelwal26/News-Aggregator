package com.indian_news_aggregator.News.rss;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.indian_news_aggregator.News.models.Article;
import com.indian_news_aggregator.News.models.NewsSource;
import com.indian_news_aggregator.News.repository.ArticleRepo;
import com.indian_news_aggregator.News.repository.NewsSourceRepo;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import jakarta.annotation.PostConstruct;

@Service
public class RssFetcherService {

    private static final Logger logger = LoggerFactory.getLogger(RssFetcherService.class);

    @Autowired
    private ArticleRepo articleRepo;

    @Autowired
    private NewsSourceRepo sourceRepo;

    @PostConstruct
    public void fetchOnStartup() {
        fetchNews();
    }

    @Scheduled(cron = "0 */1 * * * *") // Every 5 minutes
    public void fetchNews() {
        logger.info("🔄 Starting RSS fetch job...");

        List<NewsSource> sources = sourceRepo.findAll();
        int articlesSaved = 0;

        for (NewsSource source : sources) {
            logger.debug("🌐 Fetching from: {}", source.getName());

            try {
                if (isSpecialCase(source.getName())) {
                    handleSpecialCase(source, articlesSaved);
                    continue;
                }

                URL url = new URL(source.getRssUrl());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
                connection.setRequestProperty("Accept", "application/rss+xml, application/xml, text/xml, */*");
                connection.setRequestProperty("Accept-Language", "en-US,en;q=0.9");
                connection.setRequestProperty("Cache-Control", "no-cache");
                connection.setRequestProperty("Pragma", "no-cache");
                connection.setConnectTimeout(10000); // 10 seconds
                connection.setReadTimeout(15000); // 15 seconds
                connection.setInstanceFollowRedirects(true);

                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    logger.error("❌ HTTP {} for '{}': {}", responseCode, source.getName(), url);
                    continue;
                }

                try (InputStream inputStream = connection.getInputStream()) {
                    String rawXml = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

                    // Try to detect encoding from XML declaration
                    String encoding = detectEncoding(rawXml);
                    if (!encoding.equals("UTF-8")) {
                        rawXml = new String(inputStream.readAllBytes(), java.nio.charset.Charset.forName(encoding));
                    }

                    String safeXml = sanitizeXmlEntities(rawXml);
                    safeXml = removeInvalidXmlChars(safeXml);

                    SyndFeedInput input = new SyndFeedInput();
                    input.setXmlHealerOn(true);
                    input.setAllowDoctypes(true);

                    SyndFeed feed = input.build(new XmlReader(new ByteArrayInputStream(safeXml.getBytes(StandardCharsets.UTF_8))));

                    for (SyndEntry entry : feed.getEntries()) {
                        String title = entry.getTitle();
                        String link = entry.getLink();

                        if (title == null || link == null) {
                            logger.warn("⛔ Skipping article with missing title or link from {}", source.getName());
                            continue;
                        }

                        if (articleRepo.existsByLinkAndSource_Id(link, source.getId())) {
                            logger.debug("🔁 Duplicate skipped: '{}' from {}", title, source.getName());
                            continue;
                        }

                        String rawDescription = entry.getDescription() != null
                                ? entry.getDescription().getValue()
                                : (!entry.getContents().isEmpty() ? entry.getContents().get(0).getValue() : "");

                        // Handle various description formats
                        String plainText = extractPlainText(rawDescription);
                        String trimmedDescription = limitToFourLines(plainText);

                        // Ensure description fits in database column (255 chars)
                        if (trimmedDescription.length() > 255) {
                            trimmedDescription = trimmedDescription.substring(0, 252) + "...";
                        }

                        Article article = new Article();
                        article.setTitle(title);
                        article.setLink(link);
                        article.setDescription(trimmedDescription);
                        article.setPublishedAt(
                                entry.getPublishedDate() != null
                                        ? entry.getPublishedDate().toInstant().toString()
                                        : ""
                        );
                        article.setSource(source);
                        
                        // Try to extract image URL from description
                        String imageUrl = extractImageUrl(rawDescription);
                        if (imageUrl != null) {
                            article.setImageUrl(imageUrl);
                        }
                        
                        // Set default category based on source
                        article.setCategory("General");

                        articleRepo.save(article);
                        logger.info("✅ Saved: '{}' from {}", title, source.getName());
                        articlesSaved++;
                    }

                } catch (SocketTimeoutException e) {
                    logger.error("⏰ Timeout fetching from '{}': {}", source.getName(), e.getMessage());
                } catch (Exception e) {
                    logger.error("❌ Failed to fetch/parse feed from '{}': {}", source.getName(), e.getMessage());
                }
            } catch (Exception e) {
                logger.error("❌ Failed to fetch/parse feed from '{}': {}", source.getName(), e.getMessage());
            }
        }

        logger.info("🎯 Fetch complete. New articles: {} | Total in DB: {}", articlesSaved, articleRepo.count());
    }

    @Scheduled(cron = "0 0 * * * *") // Every hour
    @Transactional
    public void deleteOldArticles() {
        // Calculate ISO string for 2 days ago
        java.time.Instant twoDaysAgo = java.time.Instant.now().minus(java.time.Duration.ofDays(2));
        String cutoff = twoDaysAgo.toString();
        int before = (int) articleRepo.count();
        articleRepo.deleteByPublishedAtLessThan(cutoff);
        int after = (int) articleRepo.count();
        logger.info("🗑️ Deleted articles older than 2 days. Before: {} After: {}", before, after);
    }

    private boolean isSpecialCase(String sourceName) {
        return sourceName.contains("Washington Post");
    }

    private void handleSpecialCase(NewsSource source, int articlesSaved) {
        try {
            if (source.getName().contains("Washington Post")) {
                handleWashingtonPost(source, articlesSaved);
            }
        } catch (Exception e) {
            logger.error("❌ Special case handling failed for '{}': {}", source.getName(), e.getMessage());
        }
    }

    private void handleWashingtonPost(NewsSource source, int articlesSaved) {
        logger.info("🔧 Using special handling for Washington Post");
        try {
            // Try multiple RSS endpoints for Washington Post
            String[] urls = {
                    "https://feeds.washingtonpost.com/rss/world",
                    "https://feeds.washingtonpost.com/rss/national",
                    "https://feeds.washingtonpost.com/rss/politics"
            };

            for (String url : urls) {
                try {
                    URL rssUrl = new URL(url);
                    HttpURLConnection connection = (HttpURLConnection) rssUrl.openConnection();

                    connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
                    connection.setRequestProperty("Accept", "application/rss+xml, application/xml, text/xml, */*");
                    connection.setRequestProperty("Accept-Language", "en-US,en;q=0.9");
                    connection.setConnectTimeout(10000); // Reduced timeout
                    connection.setReadTimeout(15000);   // Reduced timeout

                    int responseCode = connection.getResponseCode();
                    if (responseCode == 200) {
                        try (InputStream inputStream = connection.getInputStream()) {
                            String rawXml = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                            processRssXml(rawXml, source, articlesSaved);
                            return; // Success, exit
                        }
                    } else {
                        logger.debug("❌ HTTP {} for Washington Post URL: {}", responseCode, url);
                    }
                } catch (Exception e) {
                    logger.debug("❌ Failed to fetch Washington Post from {}: {}", url, e.getMessage());
                }
            }
            
            logger.info("⏭️ Skipping Washington Post web scraping (too slow)");

        } catch (Exception e) {
            logger.error("❌ Washington Post special case failed: {}", e.getMessage());
        }
    }

    private void processRssXml(String rawXml, NewsSource source, int articlesSaved) {
        try {
            String safeXml = sanitizeXmlEntities(rawXml);
            safeXml = removeInvalidXmlChars(safeXml);

            SyndFeedInput input = new SyndFeedInput();
            input.setXmlHealerOn(true);
            input.setAllowDoctypes(true);

            SyndFeed feed = input.build(new XmlReader(new ByteArrayInputStream(safeXml.getBytes(StandardCharsets.UTF_8))));

            for (SyndEntry entry : feed.getEntries()) {
                String title = entry.getTitle();
                String link = entry.getLink();

                if (title == null || link == null) {
                    continue;
                }

                if (articleRepo.existsByLinkAndSource_Id(link, source.getId())) {
                    continue;
                }

                String rawDescription = entry.getDescription() != null
                        ? entry.getDescription().getValue()
                        : (!entry.getContents().isEmpty() ? entry.getContents().get(0).getValue() : "");

                String plainText = extractPlainText(rawDescription);
                String trimmedDescription = limitToFourLines(plainText);

                if (trimmedDescription.length() > 255) {
                    trimmedDescription = trimmedDescription.substring(0, 252) + "...";
                }

                Article article = new Article();
                article.setTitle(title);
                article.setLink(link);
                article.setDescription(trimmedDescription);
                article.setPublishedAt(
                        entry.getPublishedDate() != null
                                ? entry.getPublishedDate().toInstant().toString()
                                : ""
                );
                article.setSource(source);
                
                // Try to extract image URL from description
                String imageUrl = extractImageUrl(rawDescription);
                if (imageUrl != null) {
                    article.setImageUrl(imageUrl);
                }
                
                // Set default category
                article.setCategory("General");

                articleRepo.save(article);
                logger.info("✅ Saved: '{}' from {}", title, source.getName());
            }
        } catch (Exception e) {
            logger.error("❌ Failed to process RSS XML for '{}': {}", source.getName(), e.getMessage());
        }
    }

    private String limitToFourLines(String text) {
        if (text == null || text.isEmpty()) return "";

        String[] lines = text.split("\r?\n");
        StringBuilder sb = new StringBuilder();
        int count = 0;

        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            sb.append(line.trim()).append("\n");
            count++;
            if (count == 4) break;
        }

        String result = sb.toString().trim();
        return result.length() > 1000 ? result.substring(0, 1000) : result;
    }

    private String sanitizeXmlEntities(String xml) {
        return xml.replaceAll("&(?![a-zA-Z]{2,6};|#[0-9]{1,5};)", "&amp;");
    }

    private String detectEncoding(String xml) {
        if (xml.startsWith("<?xml")) {
            int encodingStart = xml.indexOf("encoding=\"");
            if (encodingStart != -1) {
                encodingStart += 10;
                int encodingEnd = xml.indexOf("\"", encodingStart);
                if (encodingEnd != -1) {
                    return xml.substring(encodingStart, encodingEnd);
                }
            }
        }
        return "UTF-8";
    }

    private String removeInvalidXmlChars(String xml) {
        return xml.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]", "");
    }

    private String extractPlainText(String htmlContent) {
        if (htmlContent == null || htmlContent.trim().isEmpty()) {
            return "";
        }

        try {
            String cleaned = htmlContent
                    .replaceAll("&nbsp;", " ")
                    .replaceAll("&amp;", "&")
                    .replaceAll("&lt;", "<")
                    .replaceAll("&gt;", ">")
                    .replaceAll("&quot;", "\"")
                    .replaceAll("&#39;", "'");
            return Jsoup.parse(cleaned).text();
        } catch (Exception e) {
            return htmlContent.replaceAll("<[^>]*>", "").trim();
        }
    }

    private String extractImageUrl(String htmlContent) {
        if (htmlContent == null || htmlContent.trim().isEmpty()) {
            return null;
        }

        try {
            org.jsoup.nodes.Document doc = Jsoup.parse(htmlContent);
            org.jsoup.nodes.Element img = doc.select("img").first();
            if (img != null) {
                String src = img.attr("src");
                if (!src.isEmpty()) {
                    return src;
                }
            }
        } catch (Exception e) {
            logger.debug("Failed to extract image URL from HTML: {}", e.getMessage());
        }
        return null;
    }
}
