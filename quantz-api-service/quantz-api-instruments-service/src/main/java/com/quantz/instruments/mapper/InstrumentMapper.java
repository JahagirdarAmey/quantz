package com.quantz.instruments.mapper;

import com.quantz.instruments.dto.InstrumentDataDto;
import com.quantz.instruments.dto.InstrumentDto;
import com.quantz.instruments.dto.InstrumentListResponseDto;
import com.quantz.instruments.dto.PricePointDto;
import com.quantz.instruments.model.PricePoint;
import com.quantz.model.Instrument;
import com.quantz.model.InstrumentData;
import com.quantz.model.InstrumentDataDataInner;
import com.quantz.model.ListInstruments200Response;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MapStruct mapper for converting between internal DTOs and API models
 */
@Mapper(componentModel = "spring")
public interface InstrumentMapper {

    /**
     * Convert internal instrument DTO to API model
     */
    @Mapping(target = "type", source = "type")
    @Mapping(target = "exchange", source = "exchange")
    @Mapping(target = "symbol", source = "symbol")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "currency", source = "currency")
    Instrument toApiModel(InstrumentDto dto);

    /**
     * Convert list of internal instrument DTOs to list of API models
     */
    List<Instrument> toApiModelList(List<InstrumentDto> dtos);

    /**
     * Convert internal price point DTO to API model
     */
    @Mapping(target = "timestamp", expression = "java(localDateTimeToOffsetDateTime(dto.getTimestamp()))")
    @Mapping(target = "open", source = "open")
    @Mapping(target = "high", source = "high")
    @Mapping(target = "low", source = "low")
    @Mapping(target = "close", source = "close")
    @Mapping(target = "volume", source = "volume")
    @Mapping(target = "adjustedClose", source = "adjustedClose")
    InstrumentDataDataInner toApiModelDataPoint(PricePointDto dto);

    /**
     * Convert list of internal price point DTOs to list of API models
     */
    List<InstrumentDataDataInner> toApiModelDataPoints(List<PricePointDto> dtos);

    /**
     * Convert internal instrument data DTO to API model
     */
    @Mapping(target = "instrumentId", source = "instrument.id")
    @Mapping(target = "symbol", source = "instrument.symbol")
    @Mapping(target = "data", source = "dataPoints")
    @Mapping(target = "interval", expression = "java(mapIntervalToEnum(dto.getInterval()))")
    @Mapping(target = "startDate", expression = "java(toLocalDate(dto.getStartDate()))")
    @Mapping(target = "endDate", expression = "java(toLocalDate(dto.getEndDate()))")
    InstrumentData toApiModel(InstrumentDataDto dto);

    /**
     * Convert internal instrument list response DTO to API model
     */
    @Mapping(target = "instruments", expression = "java(toApiModelList(dto.getInstruments()))")
    @Mapping(target = "total", source = "total")
    @Mapping(target = "limit", source = "limit")
    @Mapping(target = "offset", source = "offset")
    ListInstruments200Response toApiModel(InstrumentListResponseDto dto);

    /**
     * Convert API model to internal instrument DTO
     */
    @Mapping(target = "type", source = "type")
    @Mapping(target = "exchange", source = "exchange")
    @Mapping(target = "symbol", source = "symbol")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "currency", source = "currency")
    InstrumentDto toDto(Instrument model);

    /**
     * Convert API model to internal price point DTO
     */
    @Mapping(target = "timestamp", expression = "java(offsetDateTimeToLocalDateTime(model.getTimestamp()))")
    @Mapping(target = "open", source = "open")
    @Mapping(target = "high", source = "high")
    @Mapping(target = "low", source = "low")
    @Mapping(target = "close", source = "close")
    @Mapping(target = "volume", source = "volume")
    @Mapping(target = "adjustedClose", source = "adjustedClose")
    PricePointDto toDto(PricePoint model);

    /**
     * Convert API model to internal instrument data DTO
     */
    @Mapping(target = "instrument", ignore = true)  // We'll handle this separately
    @Mapping(target = "dataPoints", expression = "java(toDtoDataPoints(model.getData()))")
    @Mapping(target = "interval", expression = "java(model.getInterval().getValue())")
    @Mapping(target = "startDate", expression = "java(toLocalDateTime(model.getStartDate()))")
    @Mapping(target = "endDate", expression = "java(toLocalDateTime(model.getEndDate()))")
    InstrumentDataDto toDto(InstrumentData model);

    /**
     * Convert API model data point to internal DTO
     */
    @Mapping(target = "timestamp", expression = "java(offsetDateTimeToLocalDateTime(dataPoint.getTimestamp()))")
    @Mapping(target = "open", source = "open")
    @Mapping(target = "high", source = "high")
    @Mapping(target = "low", source = "low")
    @Mapping(target = "close", source = "close")
    @Mapping(target = "volume", source = "volume")
    @Mapping(target = "adjustedClose", source = "adjustedClose")
    PricePointDto toDataPointDto(InstrumentDataDataInner dataPoint);

    /**
     * Convert list of API models to list of internal price point DTOs
     */
    default List<PricePointDto> toDtoDataPoints(List<InstrumentDataDataInner> dataPoints) {
        if (dataPoints == null) {
            return null;
        }
        return dataPoints.stream()
                .map(this::toDataPointDto)
                .collect(Collectors.toList());
    }

    /**
     * Helper method to convert LocalDateTime to LocalDate
     */
    default LocalDate toLocalDate(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.toLocalDate() : null;
    }

    /**
     * Helper method to convert LocalDate to LocalDateTime
     */
    default LocalDateTime toLocalDateTime(LocalDate date) {
        return date != null ? date.atStartOfDay() : null;
    }

    /**
     * Helper method to convert LocalDateTime to OffsetDateTime
     */
    default OffsetDateTime localDateTimeToOffsetDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atZone(ZoneId.systemDefault()).toOffsetDateTime();
    }

    /**
     * Helper method to convert OffsetDateTime to LocalDateTime
     */
    default LocalDateTime offsetDateTimeToLocalDateTime(OffsetDateTime offsetDateTime) {
        if (offsetDateTime == null) {
            return null;
        }
        return offsetDateTime.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Helper method to map string interval to enum
     */
    default InstrumentData.IntervalEnum mapIntervalToEnum(String interval) {
        if (interval == null) {
            return InstrumentData.IntervalEnum._1D; // Default
        }

        switch (interval) {
            case "1m": return InstrumentData.IntervalEnum._1M;
            case "5m": return InstrumentData.IntervalEnum._5M;
            case "15m": return InstrumentData.IntervalEnum._15M;
            case "30m": return InstrumentData.IntervalEnum._30M;
            case "1h": return InstrumentData.IntervalEnum._1H;
            case "4h": return InstrumentData.IntervalEnum._4H;
            case "1d": return InstrumentData.IntervalEnum._1D;
            case "1w": return InstrumentData.IntervalEnum._1W;
            case "1mo": return InstrumentData.IntervalEnum._1MO;
            default: return InstrumentData.IntervalEnum._1D; // Default
        }
    }
}