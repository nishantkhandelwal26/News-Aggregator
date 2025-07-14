package com.indian_news_aggregator.News.repository;

import com.indian_news_aggregator.News.models.NewsSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsSourceRepo extends JpaRepository<NewsSource, Long> {
}
