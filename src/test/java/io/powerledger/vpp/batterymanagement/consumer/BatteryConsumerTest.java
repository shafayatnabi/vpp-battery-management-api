package io.powerledger.vpp.batterymanagement.consumer;

import io.powerledger.vpp.batterymanagement.dto.BatteryDto;
import io.powerledger.vpp.batterymanagement.model.Battery;
import io.powerledger.vpp.batterymanagement.repository.BatteryRepository;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BatteryConsumerTest {

    private static KafkaContainer kafkaContainer;
    private static PostgreSQLContainer<?> postgresContainer;

    @Autowired
    private BatteryRepository batteryRepository;

    @BeforeAll
    static void setUp() {
        kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));
        kafkaContainer.start();

        postgresContainer = new PostgreSQLContainer<>("postgres:15-alpine")
                .withDatabaseName("testdb")
                .withUsername("testuser")
                .withPassword("testpass");
        postgresContainer.start();
    }

    @AfterAll
    static void tearDown() {
        kafkaContainer.stop();
        postgresContainer.stop();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
    }

    @Test
    void should_consume_battery_creation_message() {
        // given
        BatteryDto batteryDto = new BatteryDto();
        batteryDto.setName("Battery A");
        batteryDto.setPostcode("2000");
        batteryDto.setCapacity(500);

        var producerProps = KafkaTestUtils.producerProps(kafkaContainer.getBootstrapServers());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class.getName());

        var producer = new KafkaProducer<String, BatteryDto>(producerProps);

        // when
        producer.send(new ProducerRecord<>("battery-create-topic", batteryDto));
        producer.flush();

        // then

        // Wait for the consumer to process the message
        try {
            Thread.sleep(5000); // Adjust the sleep time as needed
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Pageable pageable = PageRequest.of(0, 10);

        List<Battery> batteries = batteryRepository.findByPostcodeRangeOrderByName(
                "2000", "3000", pageable
        ).stream().toList();
        assertThat(batteries).hasSize(1);
        assertThat(batteries.get(0).getName()).isEqualTo("Battery A");
    }
}