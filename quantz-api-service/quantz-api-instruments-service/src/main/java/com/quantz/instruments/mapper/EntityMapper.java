package com.quantz.instruments.mapper;

import com.quantz.instruments.dto.InstrumentDto;
import com.quantz.model.Instrument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * MapStruct mapper for converting between entity objects and internal DTOs
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EntityMapper {

    /**
     * Convert entity to DTO
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "exchange", source = "exchange")
    @Mapping(target = "symbol", source = "symbol")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "currency", source = "currency")
    // Remove the active property mapping since it doesn't exist in the Instrument entity
    @Mapping(target = "listedDate", ignore = true)  // Not present in entity
    @Mapping(target = "delistedDate", ignore = true)  // Not present in entity
    @Mapping(target = "lastUpdated", ignore = true)  // Not present in entity
    @Mapping(target = "details", ignore = true)  // Not present in entity
    @Mapping(target = "availableIntervals", ignore = true)  // Not present in entity
    @Mapping(target = "dataStartDate", ignore = true)  // Not present in entity
    @Mapping(target = "dataEndDate", ignore = true)  // Not present in entity
    InstrumentDto toDto(Instrument entity);

    /**
     * Convert DTO to entity
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "exchange", source = "exchange")
    @Mapping(target = "symbol", source = "symbol")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "currency", source = "currency")
    // Remove the active property mapping since it doesn't exist in the Instrument entity
    Instrument toEntity(InstrumentDto dto);
}