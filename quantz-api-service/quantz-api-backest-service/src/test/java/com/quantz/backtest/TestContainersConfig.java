package com.quantz.backtest;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class TestContainersConfig {

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            // PostgreSQL container
            PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:14")
                    .withDatabaseName("quantapp_test")
                    .withUsername("test_user")
                    .withPassword("test_password");

            // Kafka container
            KafkaContainer kafkaContainer = new KafkaContainer(
                    DockerImageName.parse("confluentinc/cp-kafka:7.3.0"));

            // Start containers
            postgreSQLContainer.start();
            kafkaContainer.start();

            // Apply properties
            TestPropertyValues.of(
                    // Database properties
                    "spring.datasource.url=" + postgreSQLContainer.getJdbcUrl(),
                    "spring.datasource.username=" + postgreSQLContainer.getUsername(),
                    "spring.datasource.password=" + postgreSQLContainer.getPassword(),
                    "spring.datasource.driver-class-name=org.postgresql.Driver",
                    "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",

                    // Kafka properties
                    "spring.kafka.bootstrap-servers=" + kafkaContainer.getBootstrapServers(),
                    "spring.kafka.consumer.auto-offset-reset=earliest",
                    "spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer",
                    "spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer",
                    "spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer",
                    "spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer",
                    "spring.kafka.consumer.properties.spring.json.trusted.packages=com.quantz.*"
            ).applyTo(applicationContext.getEnvironment());
        }
    }

    @Bean
    public PostgreSQLContainer<?> postgreSQLContainer() {
        PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:14")
                .withDatabaseName("quantapp_test")
                .withUsername("test_user")
                .withPassword("test_password");

        container.start();

        return container;
    }

    @Bean
    public KafkaContainer kafkaContainer() {
        KafkaContainer container = new KafkaContainer(
                DockerImageName.parse("confluentinc/cp-kafka:7.3.0"));

        container.start();

        return container;
    }
}