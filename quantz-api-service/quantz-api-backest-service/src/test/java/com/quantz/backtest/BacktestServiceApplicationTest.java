package com.quantz.backtest;

import com.quantz.backtest.entity.BacktestEntity;
import com.quantz.backtest.event.MockEventPublisher;
import com.quantz.backtest.model.BacktestRequest;
import com.quantz.backtest.repository.BacktestRepository;
import com.quantz.event.model.BacktestCreatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.testcontainers.containers.PostgreSQLContainer;

import java.time.LocalDate;
import java.util.*;

import static com.quantz.quantcommon.model.OrderStatus.PENDING;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.kafka.producer.properties.spring.json.add.type.headers=false",
                "spring.kafka.consumer.properties.spring.json.trusted.packages=com.quantz"
        }
)
@ActiveProfiles("test")
@Testcontainers
@ContextConfiguration(initializers = {TestContainersConfig.Initializer.class})
class BacktestServiceApplicationTest {

    @Autowired
    private PostgreSQLContainer<?> postgreSQLContainer;

    @Autowired
    private BacktestRepository backtestRepository;

    @Autowired
    private MockEventPublisher mockEventPublisher;

    @Value("${quantz.kafka.topics.backtest-created:backtest-created}")
    private String backtestCreatedTopic;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl;
    private HttpHeaders headers;


    @BeforeEach
    void setUp() {
        // Make sure the URL has the correct format with http:// prefix
        baseUrl = "http://localhost:" + port;

        // Verify the URL is properly formed
        System.out.println("Base URL for tests: " + baseUrl);

        // Set up headers
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Clear database and event publisher before each test
        backtestRepository.deleteAll();
        mockEventPublisher.clearEvents();
    }



    @Test
    void createAndRunBacktest_ShouldReturnAccepted() {
        // Given
        BacktestRequest request = createSampleBacktestRequest();
        HttpEntity<BacktestRequest> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                baseUrl + "/api/backtests",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<>() {
                });

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(Objects.requireNonNull(response.getBody()).containsKey("backtestId")).isTrue();
        assertThat(response.getBody().containsKey("status")).isTrue();
        assertThat(response.getBody().get("status")).isEqualTo("pending");

        // Verify entity was saved to database
        String backtestId = (String) response.getBody().get("backtestId");
        List<BacktestEntity> savedBacktests = backtestRepository.findAll();
        assertThat(savedBacktests).isNotNull();
        assertThat(savedBacktests.size()).isEqualTo(1);

        BacktestEntity savedBacktest = savedBacktests.get(0);
        assertThat(savedBacktest.getId()).isEqualTo(backtestId);
        assertThat(savedBacktest.getStrategyId()).isEqualTo(request.getStrategyId());
        assertThat(savedBacktest.getStatus()).isEqualTo(PENDING);

        // Verify event was published
        Object lastEvent = mockEventPublisher.getLastEventForTopic(backtestCreatedTopic);
        assertThat(lastEvent).isNotNull();
        assertThat(lastEvent).isInstanceOf(BacktestCreatedEvent.class);

        BacktestCreatedEvent event = (BacktestCreatedEvent) lastEvent;
        assertThat(event.backtestId().toString()).isEqualTo(backtestId);
        assertThat(event.strategyId()).isEqualTo(request.getStrategyId());

        // Check the instruments list without using containsExactlyElementsOf
        assertThat(event.instruments()).isNotNull();
        assertThat(event.instruments().size()).isEqualTo(request.getInstruments().size());
        for (int i = 0; i < event.instruments().size(); i++) {
            assertThat(event.instruments().get(i)).isEqualTo(request.getInstruments().get(i));
        }
    }


    private BacktestRequest createSampleBacktestRequest() {
        // Create instruments list
        List<String> instruments = new ArrayList<>();
        instruments.add("AAPL");
        instruments.add("MSFT");
        instruments.add("GOOGL");

        // Create a UUID for strategy ID
        UUID strategyId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        // Create base request with required parameters
        BacktestRequest request = new BacktestRequest(
                strategyId,
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2023, 12, 31),
                instruments,
                100000.0f
        );

        // Add strategy configuration parameters
        request.putStrategyConfigItem("shortPeriod", 10);
        request.putStrategyConfigItem("longPeriod", 50);
        request.putStrategyConfigItem("positionSizing", "equal-weight");

        // Set optional parameters
        request.setCommission(Optional.of(0.0025f));  // 0.25% commission
        request.setSlippage(Optional.of(0.001f));     // 0.1% slippage
        request.setDataInterval(Optional.of(BacktestRequest.DataIntervalEnum._1D)); // Daily data
        request.setName(Optional.of("Sample Moving Average Crossover Backtest"));

        return request;
    }
}