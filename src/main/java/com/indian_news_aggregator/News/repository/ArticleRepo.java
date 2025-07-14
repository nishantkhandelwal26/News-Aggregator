package com.indian_news_aggregator.News.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.indian_news_aggregator.News.models.Article;

@Repository
public interface ArticleRepo extends JpaRepository<Article, Long> {
    List<Article> findTop20ByOrderByPublishedAtDesc();

    List<Article> findTop50BySourceIdInOrderByPublishedAtDesc(List<Long> sourceIds);

    List<Article> findBySourceIdOrderByPublishedAtDesc(Long sourceId);

    boolean existsByLinkAndSource_Id(String link, Long sourceId);

    void deleteByPublishedAtLessThan(String publishedAt);
}
