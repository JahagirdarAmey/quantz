# Quantz Market Data Service - Documentation

## Service Overview

The Quantz Market Data Service is a microservice designed to scrape, store, and provide financial market data from Upstox. This service is part of the broader Quantz platform, which likely includes other services for trading strategies, execution, and risk management.

## Architecture & Workflow

The service follows a scheduled data collection pattern with the following workflow:

1. **Authentication**:
    - The service authenticates with Upstox using OAuth 2.0
    - `UpstoxAuthService` handles token acquisition, storage, and refresh

2. **Scheduled Data Collection**:
    - The service runs at 4:00 PM on weekdays (configured via cron expression)
    - `DataScraperService` orchestrates the entire scraping process

3. **Instrument Management**:
    - First, the service fetches all available instruments from Upstox
    - `UpstoxInstrumentService` downloads instrument definitions from Upstox's JSON files
    - Instruments are stored in the database for reference

4. **Historical Data Collection**:
    - For first-time runs, the service fetches 10 years of historical data
    - For subsequent runs, it only fetches data since the last successful scrape (delta approach)
    - `UpstoxMarketDataService` retrieves historical OHLC (Open, High, Low, Close) candle data

5. **Data Storage**:
    - All fetched data is stored in a PostgreSQL database
    - Entities include `Instrument`, `CandleData`, `ScrapingMetadata`, and `OAuthToken`

6. **Data Access**:
    - `MarketDataController` provides REST endpoints for clients to access the stored data
    - Clients can retrieve instruments, historical candle data, and scraping history

## Key Components

### 1. Configuration
- `UpstoxProperties`: Contains all configuration for Upstox API connection
- `AppConfig`: Configures Spring beans like RestTemplate and thread pools

### 2. Authentication
- `UpstoxAuthService`: Handles OAuth authentication with Upstox
- `OAuthToken`: Entity for storing access and refresh tokens

### 3. Data Collection
- `DataScraperService`: Orchestrates the entire scraping process
- `UpstoxInstrumentService`: Fetches and manages instrument data
- `UpstoxMarketDataService`: Fetches historical price data

### 4. Data Models
- `UpstoxInstrument`: Represents financial instruments like stocks and options
- `CandleData`: Represents OHLC price data for a specific time interval
- `LtpQuoteData`: Represents Last Traded Price data for instruments

### 5. Data Storage
- JPA Repositories: Interface with the database for CRUD operations
- Postgres Database: Stores all the collected data

### 6. API Layer
- `MarketDataController`: Provides REST endpoints for accessing the data
- Supports filtering by various criteria like instrument type, date range, etc.

## Authentication Flow

1. The user triggers authentication by accessing the authentication URL
2. The service redirects to Upstox's login page
3. After successful login, Upstox redirects back with an authorization code
4. The service exchanges this code for access and refresh tokens
5. Tokens are stored in the database for future use
6. The `UpstoxAuthService` automatically refreshes tokens before they expire

## Scraping Process

1. **Check Authentication**: Verify that valid tokens are available
2. **Fetch Instruments**: Download and update instrument definitions
3. **Determine Date Range**:
    - If first run: Scrape 10 years of historical data
    - If subsequent run: Scrape data since the last run
4. **Fetch Historical Data**:
    - Process instruments in batches to avoid overwhelming the API
    - Prioritize equity instruments for initial scraping
5. **Save Data**:
    - Store all data in the database
    - Save metadata about the scraping operation
6. **Handle Errors**:
    - Log and record any failures
    - Store partial results when possible

## API Endpoints

1. **Manual Scraping**:
    - `POST /api/market-data/scrape`: Trigger a manual scraping operation

2. **Instrument Data**:
    - `GET /api/market-data/instruments`: Get all instruments with optional filtering
    - `GET /api/market-data/instruments/{instrumentKey}`: Get a specific instrument

3. **Price Data**:
    - `GET /api/market-data/candles/{instrumentKey}`: Get candle data for an instrument
    - Supports filtering by interval, start time, and end time

4. **Metadata**:
    - `GET /api/market-data/scraping-history`: Get history of scraping operations
    - `GET /api/market-data/scraping-history/latest`: Get the latest scraping operation

## Configuration Options

The service is configured through `application.properties`, including:

1. **Database Connection**:
    - Connection URL, username, password, etc.

2. **Upstox API Settings**:
    - Base URL, client ID, client secret, redirect URI
    - Timeout settings and rate limits

3. **Scheduling**:
    - Cron expression for scheduled scraping (4:00 PM weekdays)

4. **Instrument Sources**:
    - URLs for instrument definition files from Upstox

## Getting Started

1. Configure database settings in `application.properties`
2. Set up Upstox API credentials
3. Build the service with Maven
4. Run the service
5. Access the authentication endpoint to set up OAuth tokens
6. The service will automatically scrape data at the scheduled time
7. Alternatively, trigger a manual scrape using the API

## Deployment Considerations

1. **Database**: Ensure PostgreSQL is available and properly sized
2. **Memory**: Historical data scraping can be memory-intensive
3. **API Limits**: Be mindful of Upstox's rate limits
4. **Security**: Protect OAuth tokens and ensure secure communication
5. **Monitoring**: Set up alerts for failed scraping operations

This documentation provides a comprehensive overview of the Quantz Market Data Service, explaining its architecture, workflow, and usage patterns.