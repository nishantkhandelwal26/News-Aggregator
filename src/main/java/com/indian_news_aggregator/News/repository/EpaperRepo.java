package com.indian_news_aggregator.News.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.indian_news_aggregator.News.models.Epaper;

@Repository
public interface EpaperRepo extends JpaRepository<Epaper, Long> {
} 