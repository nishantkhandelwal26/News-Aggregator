# Indian News Aggregator - Backend

A Spring Boot backend for aggregating news from various Indian and international news sources via RSS feeds.

## Features

- **News Source Management**: Manage news sources with RSS URLs, logos, and categories
- **E-paper Source Management**: Manage e-paper sources with logos, links, and categories
- **Article Aggregation**: Automatically fetch and store articles from RSS feeds
- **RESTful API**: Clean API endpoints for frontend integration
- **Scheduled Fetching**: Automatic RSS feed updates every 5 minutes
- **Image Extraction**: Extract images from RSS feed descriptions

## Project Structure

src/main/java/com/indian_news_aggregator/News/
├── controllers/
│ ├── NewsController.java # REST API endpoints for news
│ └── EpaperController.java # REST API endpoints for e-papers
├── models/
│ ├── Article.java # Article entity
│ ├── NewsSource.java # News source entity
│ └── Epaper.java # E-paper entity
├── repository/
│ ├── ArticleRepo.java # Article data access
│ ├── NewsSourceRepo.java # News source data access
│ └── EpaperRepo.java # E-paper data access
├── rss/
│ └── RssFetcherService.java # RSS feed fetching service
└── NewsApplication.java # Main application class


## API Endpoints

### News Sources
- `GET /api/news-sources` - Get all news sources
- `GET /api/news/sources` - Legacy endpoint for news sources

### E-paper Sources
- `GET /api/epapers` - Get all e-paper sources

### Articles
- `GET /api/articles?sourceId={id}` - Get articles by source ID
- `GET /api/news/headlines` - Get latest headlines (top 20)

### RSS Management
- `GET /api/news/fetch` - Manually trigger RSS fetch
- `GET /api/update-news-sources` - Update news sources with real logos

## Database Schema

### news_source table
- `id` - Primary key
- `name` - News source name
- `rss_url` - RSS feed URL
- `logo` - Logo image URL
- `category` - News category (national, international, business, regional)

### article table
- `id` - Primary key
- `title` - Article title
- `link` - Article URL
- `description` - Article description
- `published_at` - Publication date
- `image_url` - Article image URL
- `category` - Article category
- `source_id` - Foreign key to news_source

### epaper table
- `id` - Primary key
- `name` - E-paper name
- `logo` - Logo image URL
- `link` - E-paper website link
- `category` - E-paper category

## Setup Instructions

1. **Database Setup**
   ```sql
   -- Run the news_sources_data.sql script in MySQL
   mysql -u root -p < news_sources_data.sql
