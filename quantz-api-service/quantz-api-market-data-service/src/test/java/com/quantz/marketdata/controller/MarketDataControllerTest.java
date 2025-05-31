package com.quantz.marketdata.controller;


import com.quantz.marketdata.service.MarketDataScraperService;
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
    void testTriggerScraping_success() throws Exception {
        doNothing().when(marketDataScraperService).manualScraping();

        mockMvc.perform(post("/api/market-data/scrape")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Market data scraping started successfully"));

        verify(marketDataScraperService).manualScraping();
    }

    // --- Tests for getInstruments ---

    @Test
    void getInstruments_whenNoParameters_shouldReturnInstruments() throws Exception {

        Instrument instrument = new Instrument();
        instrument.setInstrumentKey(UUID.randomUUID().toString());
        instrument.setName("Instrument A");

        List<Instrument> expectedInstruments = Collections.singletonList(instrument);

        when(marketDataScraperService.findInstruments(isNull(), isNull(), isNull(), isNull()))
                .thenReturn(expectedInstruments);

        mockMvc.perform(get("/api/market-data/instruments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Instrument A")));

        verify(marketDataScraperService).findInstruments(isNull(), isNull(), isNull(), isNull());
    }

    @Test
    void getInstruments_whenExchangeParameterProvided_shouldReturnInstruments() throws Exception {
        String exchange = "NSE";
        Instrument instrument = new Instrument();
        instrument.setInstrumentKey(UUID.randomUUID().toString());
        instrument.setName("NSE Instrument");
        instrument.setExchange(exchange);
        List<Instrument> expectedInstruments = Collections.singletonList(instrument);

        when(marketDataScraperService.findInstruments(exchange, null, null, null))
                .thenReturn(expectedInstruments);

        mockMvc.perform(get("/api/market-data/instruments")
                        .param("exchange", exchange)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("NSE Instrument")))
                .andExpect(jsonPath("$[0].exchange", is(exchange)));

        verify(marketDataScraperService).findInstruments(exchange, null, null, null);
    }

    @Test
    void getInstruments_whenSegmentParameterProvided_shouldReturnInstruments() throws Exception {
        String segment = "EQUITY";
        Instrument instrument = new Instrument();
        instrument.setInstrumentKey(UUID.randomUUID().toString());
        instrument.setName("Equity Instrument");
        instrument.setSegment(segment);
        List<Instrument> expectedInstruments = Collections.singletonList(instrument);

        when(marketDataScraperService.findInstruments(null, segment, null, null))
                .thenReturn(expectedInstruments);

        mockMvc.perform(get("/api/market-data/instruments")
                        .param("segment", segment)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].segment", is(segment)));

        verify(marketDataScraperService).findInstruments(null, segment, null, null);
    }

    @Test
    void getInstruments_whenInstrumentTypeParameterProvided_shouldReturnInstruments() throws Exception {
        String instrumentType = "STOCK";
        Instrument instrument = new Instrument();
        instrument.setInstrumentKey(UUID.randomUUID().toString());
        instrument.setName("Stock Instrument");
        instrument.setInstrumentType(instrumentType);
        List<Instrument> expectedInstruments = Collections.singletonList(instrument);

        when(marketDataScraperService.findInstruments(null, null, instrumentType, null))
                .thenReturn(expectedInstruments);

        mockMvc.perform(get("/api/market-data/instruments")
                        .param("instrumentType", instrumentType)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].instrumentType", is(instrumentType)));

        verify(marketDataScraperService).findInstruments(null, null, instrumentType, null);
    }

    @Test
    void getInstruments_whenSearchParameterProvided_shouldReturnInstruments() throws Exception {
        String search = "RELIANCE";
        Instrument instrument = new Instrument();
        instrument.setInstrumentKey(UUID.randomUUID().toString());
        instrument.setName("Reliance Industries");
        List<Instrument> expectedInstruments = Collections.singletonList(instrument);

        when(marketDataScraperService.findInstruments(null, null, null, search))
                .thenReturn(expectedInstruments);

        mockMvc.perform(get("/api/market-data/instruments")
                        .param("search", search)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Reliance Industries")));

        verify(marketDataScraperService).findInstruments(null, null, null, search);
    }

    @Test
    void getInstruments_whenMultipleParametersProvided_shouldPassAllToService() throws Exception {
        // This test assumes your service method findInstruments can handle multiple parameters
        // or has a defined precedence. The controller simply passes them along.
        String exchange = "BSE";
        String search = "TATA";
        Instrument instrument = new Instrument();
        instrument.setInstrumentKey(UUID.randomUUID().toString());
        instrument.setName("Tata Motors - BSE");
        List<Instrument> expectedInstruments = Collections.singletonList(instrument);

        when(marketDataScraperService.findInstruments(exchange, null, null, search))
                .thenReturn(expectedInstruments);

        mockMvc.perform(get("/api/market-data/instruments")
                        .param("exchange", exchange)
                        .param("search", search) // Example of multiple params
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Tata Motors - BSE")));

        // Verify that the controller passes all provided parameters to the service
        verify(marketDataScraperService).findInstruments(exchange, null, null, search);
    }


    @Test
    void getInstruments_whenServiceReturnsEmptyList_shouldReturnOkWithEmptyArray() throws Exception {
        when(marketDataScraperService.findInstruments(isNull(), isNull(), isNull(), isNull()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/market-data/instruments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(marketDataScraperService).findInstruments(isNull(), isNull(), isNull(), isNull());
    }

    @Test
    void getInstruments_whenServiceReturnsMultipleInstruments_shouldReturnOkWithInstrumentArray() throws Exception {

        Instrument instrument1 = new Instrument();
        instrument1.setInstrumentKey(UUID.randomUUID().toString());
        instrument1.setName("Instrument X");

        Instrument instrument2 = new Instrument();
        instrument2.setInstrumentKey(UUID.randomUUID().toString());
        instrument2.setName("Instrument Y");

        List<Instrument> expectedInstruments = Arrays.asList(instrument1, instrument2);

        when(marketDataScraperService.findInstruments(isNull(), isNull(), isNull(), isNull()))
                .thenReturn(expectedInstruments);

        mockMvc.perform(get("/api/market-data/instruments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Instrument X")))
                .andExpect(jsonPath("$[1].name", is("Instrument Y")));

        verify(marketDataScraperService).findInstruments(isNull(), isNull(), isNull(), isNull());
    }

}