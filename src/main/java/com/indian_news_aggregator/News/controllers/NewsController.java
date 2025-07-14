package com.indian_news_aggregator.News.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.indian_news_aggregator.News.models.Article;
import com.indian_news_aggregator.News.models.NewsSource;
import com.indian_news_aggregator.News.repository.ArticleRepo;
import com.indian_news_aggregator.News.repository.NewsSourceRepo;
import com.indian_news_aggregator.News.rss.RssFetcherService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class NewsController {
    @Autowired
    private ArticleRepo articleRepo;

    @Autowired
    private NewsSourceRepo newsSourceRepo;

    @Autowired
    private RssFetcherService rssFetcherService;

    // Endpoint for news sources (used by frontend)
    @GetMapping("/news-sources")
    public List<NewsSource> getNewsSources(){
        return newsSourceRepo.findAll();
    }

    // Endpoint for articles by source ID (used by frontend)
    @GetMapping("/articles")
    public List<Article> getArticlesBySource(@RequestParam Long sourceId){
        return articleRepo.findBySourceIdOrderByPublishedAtDesc(sourceId);
    }

    // Endpoint to manually trigger RSS fetch
    @GetMapping("/news/fetch")
    public String triggerFetch(){
        rssFetcherService.fetchNews();
        return "RSS fetch triggered successfully!";
    }
}
