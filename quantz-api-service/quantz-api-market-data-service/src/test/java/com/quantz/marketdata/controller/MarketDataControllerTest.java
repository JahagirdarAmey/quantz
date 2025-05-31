package com.quantz.marketdata.controller;



import com.quantz.marketdata.service.MarketDataScraperService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;


import com.quantz.marketdata.entity.Instrument;


import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;


@WebMvcTest(MarketDataController.class)
@DisplayName("Market Data Controller API Tests")
class MarketDataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MarketDataScraperService marketDataScraperService;

    @TestConfiguration
    static class MarketDataControllerTestConfiguration {

        @Bean
        public MarketDataScraperService marketDataScraperService() {
            // Manually create and return the mock
            return mock(MarketDataScraperService.class);
        }
    }


    @Test
    @DisplayName("Triggering data scraping should start the process successfully")
    void shouldSuccessfullyStartDataScrapingProcessWhenTriggered() throws Exception {
        // Given: The scraping service is ready to start manual scraping
        doNothing().when(marketDataScraperService).manualScraping();

        // When: A request is made to trigger the scraping process
        mockMvc.perform(post("/api/market-data/scrape")
                        .contentType(MediaType.APPLICATION_JSON))
                // Then: The response should indicate success and the service method should be called
                .andExpect(status().isOk())
                .andExpect(content().string("Market data scraping started successfully"));

        verify(marketDataScraperService).manualScraping();
    }

    // --- Tests for getInstruments ---

    @Test
    @DisplayName("Fetching instruments without any filters should return all available instruments")
    void shouldReturnAllInstrumentsWhenNoFiltersAreApplied() throws Exception {
        // Given: The service has a list of instruments to return
        Instrument instrument = new Instrument();
        instrument.setInstrumentKey(UUID.randomUUID().toString());
        instrument.setName("Instrument A");
        List<Instrument> expectedInstruments = Collections.singletonList(instrument);
        when(marketDataScraperService.findInstruments(isNull(), isNull(), isNull(), isNull()))
                .thenReturn(expectedInstruments);

        // When: A request is made to fetch instruments without any query parameters
        mockMvc.perform(get("/api/market-data/instruments")
                        .contentType(MediaType.APPLICATION_JSON))
                // Then: The response should be successful and contain the list of instruments
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Instrument A")));

        // And: The service method for finding instruments should be called with null filters
        verify(marketDataScraperService).findInstruments(isNull(), isNull(), isNull(), isNull());
    }

    @Test
    @DisplayName("Fetching instruments filtered by a specific exchange should return matching instruments")
    void shouldReturnInstrumentsForSpecifiedExchangeWhenFiltered() throws Exception {
        // Given: A specific exchange and a list of instruments matching that exchange
        String exchange = "NSE";
        Instrument instrument = new Instrument();
        instrument.setInstrumentKey(UUID.randomUUID().toString());
        instrument.setName("NSE Instrument");
        instrument.setExchange(exchange);
        List<Instrument> expectedInstruments = Collections.singletonList(instrument);
        when(marketDataScraperService.findInstruments(exchange, null, null, null))
                .thenReturn(expectedInstruments);

        // When: A request is made to fetch instruments with the 'exchange' parameter
        mockMvc.perform(get("/api/market-data/instruments")
                        .param("exchange", exchange)
                        .contentType(MediaType.APPLICATION_JSON))
                // Then: The response should be successful and contain instruments from that exchange
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("NSE Instrument")))
                .andExpect(jsonPath("$[0].exchange", is(exchange)));

        // And: The service method should be called with the specified exchange
        verify(marketDataScraperService).findInstruments(exchange, null, null, null);
    }

    @Test
    @DisplayName("Fetching instruments filtered by a specific segment should return matching instruments")
    void shouldReturnInstrumentsForSpecifiedSegmentWhenFiltered() throws Exception {
        // Given: A specific segment and a list of instruments matching that segment
        String segment = "EQUITY";
        Instrument instrument = new Instrument();
        instrument.setInstrumentKey(UUID.randomUUID().toString());
        instrument.setName("Equity Instrument");
        instrument.setSegment(segment);
        List<Instrument> expectedInstruments = Collections.singletonList(instrument);
        when(marketDataScraperService.findInstruments(null, segment, null, null))
                .thenReturn(expectedInstruments);

        // When: A request is made to fetch instruments with the 'segment' parameter
        mockMvc.perform(get("/api/market-data/instruments")
                        .param("segment", segment)
                        .contentType(MediaType.APPLICATION_JSON))
                // Then: The response should be successful and contain instruments from that segment
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].segment", is(segment)));

        // And: The service method should be called with the specified segment
        verify(marketDataScraperService).findInstruments(null, segment, null, null);
    }

    @Test
    @DisplayName("Fetching instruments filtered by a specific instrument type should return matching instruments")
    void shouldReturnInstrumentsForSpecifiedInstrumentTypeWhenFiltered() throws Exception {
        // Given: A specific instrument type and a list of instruments matching that type
        String instrumentType = "STOCK";
        Instrument instrument = new Instrument();
        instrument.setInstrumentKey(UUID.randomUUID().toString());
        instrument.setName("Stock Instrument");
        instrument.setInstrumentType(instrumentType);
        List<Instrument> expectedInstruments = Collections.singletonList(instrument);
        when(marketDataScraperService.findInstruments(null, null, instrumentType, null))
                .thenReturn(expectedInstruments);

        // When: A request is made to fetch instruments with the 'instrumentType' parameter
        mockMvc.perform(get("/api/market-data/instruments")
                        .param("instrumentType", instrumentType)
                        .contentType(MediaType.APPLICATION_JSON))
                // Then: The response should be successful and contain instruments of that type
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].instrumentType", is(instrumentType)));

        // And: The service method should be called with the specified instrument type
        verify(marketDataScraperService).findInstruments(null, null, instrumentType, null);
    }

    @Test
    @DisplayName("Searching for instruments by a keyword should return matching instruments")
    void shouldReturnInstrumentsMatchingSearchKeyword() throws Exception {
        // Given: A search keyword and a list of instruments matching that keyword
        String search = "RELIANCE";
        Instrument instrument = new Instrument();
        instrument.setInstrumentKey(UUID.randomUUID().toString());
        instrument.setName("Reliance Industries");
        List<Instrument> expectedInstruments = Collections.singletonList(instrument);
        when(marketDataScraperService.findInstruments(null, null, null, search))
                .thenReturn(expectedInstruments);

        // When: A request is made to fetch instruments with the 'search' parameter
        mockMvc.perform(get("/api/market-data/instruments")
                        .param("search", search)
                        .contentType(MediaType.APPLICATION_JSON))
                // Then: The response should be successful and contain instruments matching the search
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Reliance Industries")));

        // And: The service method should be called with the specified search keyword
        verify(marketDataScraperService).findInstruments(null, null, null, search);
    }

    @Test
    @DisplayName("Fetching instruments with multiple filters should pass all filters to the service")
    void shouldPassAllProvidedFiltersToServiceWhenFetchingInstruments() throws Exception {
        // Given: Multiple filter parameters and a list of instruments matching those combined filters
        String exchange = "BSE";
        String search = "TATA";
        Instrument instrument = new Instrument();
        instrument.setInstrumentKey(UUID.randomUUID().toString());
        instrument.setName("Tata Motors - BSE");
        List<Instrument> expectedInstruments = Collections.singletonList(instrument);
        when(marketDataScraperService.findInstruments(exchange, null, null, search))
                .thenReturn(expectedInstruments);

        // When: A request is made to fetch instruments with multiple parameters (e.g., 'exchange' and 'search')
        mockMvc.perform(get("/api/market-data/instruments")
                        .param("exchange", exchange)
                        .param("search", search)
                        .contentType(MediaType.APPLICATION_JSON))
                // Then: The response should be successful and contain the filtered instruments
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Tata Motors - BSE")));

        // And: The service method should be called with all provided filter parameters
        verify(marketDataScraperService).findInstruments(exchange, null, null, search);
    }


    @Test
    @DisplayName("Fetching instruments should return an empty list when no instruments match the criteria")
    void shouldReturnEmptyListWhenNoInstrumentsMatchFilters() throws Exception {
        // Given: The service will return an empty list for the given filters
        when(marketDataScraperService.findInstruments(isNull(), isNull(), isNull(), isNull()))
                .thenReturn(Collections.emptyList());

        // When: A request is made to fetch instruments
        mockMvc.perform(get("/api/market-data/instruments")
                        .contentType(MediaType.APPLICATION_JSON))
                // Then: The response should be successful and contain an empty list
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        // And: The service method for finding instruments should be called
        verify(marketDataScraperService).findInstruments(isNull(), isNull(), isNull(), isNull());
    }

    @Test
    @DisplayName("Fetching instruments should return multiple instruments when several match the criteria")
    void shouldReturnMultipleInstrumentsWhenSeveralMatchCriteria() throws Exception {
        // Given: The service has multiple instruments to return
        Instrument instrument1 = new Instrument();
        instrument1.setInstrumentKey(UUID.randomUUID().toString());
        instrument1.setName("Instrument X");
        Instrument instrument2 = new Instrument();
        instrument2.setInstrumentKey(UUID.randomUUID().toString());
        instrument2.setName("Instrument Y");
        List<Instrument> expectedInstruments = Arrays.asList(instrument1, instrument2);
        when(marketDataScraperService.findInstruments(isNull(), isNull(), isNull(), isNull()))
                .thenReturn(expectedInstruments);

        // When: A request is made to fetch instruments
        mockMvc.perform(get("/api/market-data/instruments")
                        .contentType(MediaType.APPLICATION_JSON))
                // Then: The response should be successful and contain all matching instruments
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Instrument X")))
                .andExpect(jsonPath("$[1].name", is("Instrument Y")));

        // And: The service method for finding instruments should be called
        verify(marketDataScraperService).findInstruments(isNull(), isNull(), isNull(), isNull());
    }
}