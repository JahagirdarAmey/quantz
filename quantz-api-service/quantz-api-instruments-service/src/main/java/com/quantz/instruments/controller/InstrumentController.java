package com.quantz.instruments.controller;


import com.quantz.api.InstrumentsApi;
import com.quantz.instruments.dto.InstrumentDataDto;
import com.quantz.instruments.dto.InstrumentListResponseDto;
import com.quantz.instruments.mapper.InstrumentMapper;
import com.quantz.instruments.service.InstrumentService;
import com.quantz.model.InstrumentData;
import com.quantz.model.ListInstruments200Response;
import jakarta.validation.constraints.Max;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
public class InstrumentController implements InstrumentsApi {

    private final InstrumentService instrumentService;
    private final InstrumentMapper instrumentMapper;

    @Override
    public ResponseEntity<InstrumentData> _getInstrumentData(String instrumentId, LocalDate startDate, LocalDate endDate, Optional<String> interval) {

        log.info("API request received for instrument data: id={}, startDate={}, endDate={}, interval={}",
                instrumentId, startDate, endDate, interval.orElse("1d"));

        // Get the DTO from the service
        InstrumentDataDto dataDto = instrumentService.getInstrumentData(
                instrumentId,
                startDate,
                endDate,
                interval);

        // Convert the DTO to the expected API model
        InstrumentData instrumentData = instrumentMapper.toApiModel(dataDto);

        log.debug("Returning instrument data with {} price points",
                instrumentData.getData() != null ? instrumentData.getData().size() : 0);

        // Return the API model
        return ResponseEntity.ok(instrumentData);
    }

    @Override
    public ResponseEntity<ListInstruments200Response> _listInstruments(Optional<String> type, Optional<String> exchange, Optional<String> search, Optional<@Max(200) Integer> limit, Optional<Integer> offset) {

        log.info("API request received for instruments list: type={}, exchange={}, search={}, limit={}, offset={}",
                type.orElse(null), exchange.orElse(null), search.orElse(null),
                limit.orElse(50), offset.orElse(0));

        // Get the DTO from the service
        InstrumentListResponseDto responseDto = instrumentService.findInstruments(
                type,
                exchange,
                search,
                limit,
                offset);

        // Convert the DTO to the expected API model
        ListInstruments200Response response = instrumentMapper.toApiModel(responseDto);

        log.debug("Returning instrument list with {} instruments out of {}",
                response.getInstruments().size(), response.getTotal());

        // Return the API model
        return ResponseEntity.ok(response);
    }
}