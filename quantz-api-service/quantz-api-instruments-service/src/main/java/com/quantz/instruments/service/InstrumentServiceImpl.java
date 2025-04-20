package com.quantz.instruments.service;

import com.quantz.instruments.dto.InstrumentDataDto;
import com.quantz.instruments.dto.InstrumentDto;
import com.quantz.instruments.dto.InstrumentListResponseDto;
import com.quantz.instruments.dto.PricePointDto;
import com.quantz.instruments.exception.InstrumentNotFoundException;
import com.quantz.instruments.mapper.EntityMapper;
import com.quantz.instruments.repository.InstrumentRepository;
import com.quantz.instruments.repository.TimeSeriesRepository;
import com.quantz.model.Instrument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InstrumentServiceImpl implements InstrumentService {

    private final InstrumentRepository instrumentRepository;
    private final TimeSeriesRepository timeSeriesRepository;
    private final EntityMapper entityMapper;

    @Override
    @Cacheable(value = "instrumentListings", key = "'instruments:' + #type + ':' + #exchange + ':' + #search + ':' + #limit + ':' + #offset")
    public InstrumentListResponseDto findInstruments(
            Optional<String> type,
            Optional<String> exchange,
            Optional<String> search,
            Optional<Integer> limit,
            Optional<Integer> offset) {

        log.info("Finding instruments with type={}, exchange={}, search={}, limit={}, offset={}",
                type.orElse(null), exchange.orElse(null), search.orElse(null),
                limit.orElse(50), offset.orElse(0));

        // Build the specification based on provided filters
        Specification<Instrument> spec = Specification.where(null);

        if (type.isPresent() && !type.get().isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("type"), type.get()));
        }

        if (exchange.isPresent() && !exchange.get().isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("exchange"), exchange.get()));
        }

        if (search.isPresent() && !search.get().isEmpty()) {
            String searchTerm = "%" + search.get().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("name")), searchTerm),
                    cb.like(cb.lower(root.get("symbol")), searchTerm)
            ));
        }

        // Query with pagination
        int pageLimit = limit.orElse(50);
        int pageOffset = offset.orElse(0);

        Page<Instrument> instrumentPage = instrumentRepository.findAll(
                spec, PageRequest.of(pageOffset / pageLimit, pageLimit));

        // Map to DTOs
        List<InstrumentDto> instrumentDtos = instrumentPage.getContent().stream()
                .map(entityMapper::toDto)
                .collect(Collectors.toList());

        // Build the response
        InstrumentListResponseDto.FiltersDto filters = InstrumentListResponseDto.FiltersDto.builder()
                .type(type.orElse(null))
                .exchange(exchange.orElse(null))
                .search(search.orElse(null))
                .build();

        return InstrumentListResponseDto.builder()
                .instruments(instrumentDtos)
                .total((int) instrumentPage.getTotalElements())
                .limit(pageLimit)
                .offset(pageOffset)
                .filters(filters)
                .build();
    }

    @Override
    @Cacheable(value = "instrumentData", key = "'instrumentData:' + #instrumentId + ':' + #interval + ':' + #startDate + ':' + #endDate")
    public InstrumentDataDto getInstrumentData(
            String instrumentId,
            LocalDate startDate,
            LocalDate endDate,
            Optional<String> interval) {

        log.info("Getting instrument data for id={}, startDate={}, endDate={}, interval={}",
                instrumentId, startDate, endDate, interval.orElse("1d"));

        // Find the instrument or throw an exception if not found
        Instrument instrument = instrumentRepository.findById(instrumentId)
                .orElseThrow(() -> new InstrumentNotFoundException("Instrument not found: " + instrumentId));

        // Convert to DTO
        InstrumentDto instrumentDto = entityMapper.toDto(instrument);

        // Fetch time series data
        List<PricePointDto> pricePoints = timeSeriesRepository.fetchTimeSeries(
                instrumentId,
                startDate.atStartOfDay(),
                endDate.plusDays(1).atStartOfDay(),
                interval.orElse("1d"));

        // Build the response
        return InstrumentDataDto.builder()
                .instrument(instrumentDto)
                .interval(interval.orElse("1d"))
                .startDate(startDate.atStartOfDay())
                .endDate(endDate.atStartOfDay())
                .dataPoints(pricePoints)
                .build();
    }
}