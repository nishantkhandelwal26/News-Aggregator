package com.indian_news_aggregator.News.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.indian_news_aggregator.News.models.Epaper;
import com.indian_news_aggregator.News.services.EpaperService;

@RestController
@RequestMapping("/api/epapers")
@CrossOrigin(origins = "*")
public class EpaperController {
    @Autowired
    private EpaperService epaperService;

    @GetMapping
    public List<Epaper> getAllEpapers() {
        return epaperService.getAllEpapers();
    }
} 