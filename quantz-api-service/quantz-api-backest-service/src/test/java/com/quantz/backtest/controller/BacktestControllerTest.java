package com.quantz.backtest.controller;

import com.quantz.backtest.model.BacktestCreationResponse;
import com.quantz.backtest.model.BacktestDetail;
import com.quantz.backtest.model.BacktestRequest;
import com.quantz.backtest.model.ListBacktests200Response;
import com.quantz.backtest.service.BacktestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the BacktestController class.
 * <p>
 * These tests verify that the controller correctly delegates to the service
 * and returns appropriate HTTP responses without loading the Spring context.
 */
@ExtendWith(MockitoExtension.class)
class BacktestControllerTest {

    @Mock
    private BacktestService backtestService;

    @InjectMocks
    private BacktestController controller;

    // Test data
    private UUID testBacktestId;
    private BacktestRequest testRequest;
    private BacktestCreationResponse testCreationResponse;
    private BacktestDetail testBacktestDetail;
    private ListBacktests200Response testListResponse;

    @BeforeEach
    void setUp() {
        // Initialize test data
        testBacktestId = UUID.randomUUID();

        // Create test request
        testRequest = new BacktestRequest(
                UUID.randomUUID(),
                LocalDate.now(),
                LocalDate.now().plusMonths(1),
                Arrays.asList("AAPL", "MSFT"),
                10000.0f
        );

        // Create test creation response
        testCreationResponse = new BacktestCreationResponse();
        testCreationResponse.setBacktestId(testBacktestId);
        testCreationResponse.setStatus(BacktestCreationResponse.StatusEnum.PENDING);

        // Create test backtest detail
        testBacktestDetail = new BacktestDetail();
        testBacktestDetail.setBacktestId(testBacktestId);

        // Create test list response
        testListResponse = new ListBacktests200Response();
        testListResponse.setTotal(Optional.of(1));
        testListResponse.setOffset(Optional.of(0));
        testListResponse.setLimit(Optional.of(20));
    }

    @Nested
    @DisplayName("Create Backtest Tests")
    class CreateBacktestTests {

        @Test
        @DisplayName("Should create backtest and return OK response")
        void createBacktest_ShouldReturnOkResponse() {
            // Arrange
            when(backtestService.createBacktest(any(BacktestRequest.class)))
                    .thenReturn(testCreationResponse);

            // Act
            ResponseEntity<BacktestCreationResponse> response = controller.createBacktest(testRequest);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(testCreationResponse);

            // Verify service called
            verify(backtestService).createBacktest(testRequest);
        }
    }

    @Nested
    @DisplayName("List Backtests Tests")
    class ListBacktestsTests {

        @Test
        @DisplayName("Should list backtests with default parameters")
        void listBacktests_WithDefaultParams_ShouldReturnOkResponse() {
            // Arrange
            when(backtestService.listBacktests(isNull(), eq(20), eq(0)))
                    .thenReturn(testListResponse);

            // Act
            ResponseEntity<ListBacktests200Response> response = controller.listBacktests(
                    Optional.empty(), Optional.empty(), Optional.empty());

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(testListResponse);

            // Verify service called with defaults
            verify(backtestService).listBacktests(null, 20, 0);
        }

        @Test
        @DisplayName("Should list backtests with custom parameters")
        void listBacktests_WithCustomParams_ShouldReturnOkResponse() {
            // Arrange
            when(backtestService.listBacktests(eq("COMPLETED"), eq(10), eq(5)))
                    .thenReturn(testListResponse);

            // Act
            ResponseEntity<ListBacktests200Response> response = controller.listBacktests(
                    Optional.of("COMPLETED"), Optional.of(10), Optional.of(5));

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(testListResponse);

            // Verify service called with custom params
            verify(backtestService).listBacktests("COMPLETED", 10, 5);
        }
    }

    @Nested
    @DisplayName("Get Backtest Tests")
    class GetBacktestTests {

        @Test
        @DisplayName("Should get backtest details and return OK response")
        void getBacktest_ShouldReturnOkResponse() {
            // Arrange
            when(backtestService.getBacktest(eq(testBacktestId)))
                    .thenReturn(testBacktestDetail);

            // Act
            ResponseEntity<BacktestDetail> response = controller.getBacktest(testBacktestId);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(testBacktestDetail);

            // Verify service called
            verify(backtestService).getBacktest(testBacktestId);
        }
    }

    @Nested
    @DisplayName("Delete Backtest Tests")
    class DeleteBacktestTests {

        @Test
        @DisplayName("Should delete backtest and return no content response")
        void deleteBacktest_ShouldReturnNoContentResponse() {
            // Arrange - no specific setup needed for void method
            doNothing().when(backtestService).deleteBacktest(any(UUID.class));

            // Act
            ResponseEntity<Void> response = controller.deleteBacktest(testBacktestId);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(response.getBody()).isNull();

            // Verify service called
            verify(backtestService).deleteBacktest(testBacktestId);
        }
    }
}