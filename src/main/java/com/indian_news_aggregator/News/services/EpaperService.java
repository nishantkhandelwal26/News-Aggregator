package com.indian_news_aggregator.News.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.indian_news_aggregator.News.models.Epaper;
import com.indian_news_aggregator.News.repository.EpaperRepo;

@Service
public class EpaperService {
    @Autowired
    private EpaperRepo epaperRepo;

    public List<Epaper> getAllEpapers() {
        return epaperRepo.findAll();
    }
} 