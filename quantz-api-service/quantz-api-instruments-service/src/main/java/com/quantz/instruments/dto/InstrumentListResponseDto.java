package com.quantz.instruments.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data transfer object representing a paginated response of instruments.
 * Used for listing instruments with filtering and pagination support.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstrumentListResponseDto {


    private List<InstrumentDto> instruments;
    private int total;
    private int limit;
    private int offset;
    private FiltersDto filters;

    /**
     * Helper method to calculate the current page number based on limit and offset
     *
     * @return The current page number (1-based)
     */

    public int getPage() {
        return limit > 0 ? (offset / limit) + 1 : 1;
    }

    /**
     * Helper method to calculate the total number of pages
     *
     * @return The total number of pages
     */

    public int getTotalPages() {
        return limit > 0 ? (int) Math.ceil((double) total / limit) : 1;
    }

    /**
     * Helper method to check if there's a next page available
     *
     * @return True if there's a next page
     */

    public boolean isHasNext() {
        return getPage() < getTotalPages();
    }

    /**
     * Helper method to check if there's a previous page available
     *
     * @return True if there's a previous page
     */

    public boolean isHasPrevious() {
        return getPage() > 1;
    }

    /**
     * Nested class for representing the filters applied to the instrument list
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FiltersDto {

        private String type;
        private String exchange;
        private String search;
        private List<FilterCriteriaDto> additionalFilters;
    }

    /**
     * Nested class for representing additional filter criteria
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FilterCriteriaDto {

        private String field;
        private String operator;
        private Object value;
    }
}