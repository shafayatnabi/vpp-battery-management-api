package io.powerledger.vpp.batterymanagement.integration;

import io.powerledger.vpp.batterymanagement.model.Battery;
import io.powerledger.vpp.batterymanagement.repository.BatteryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@EmbeddedKafka(partitions = 1, topics = {"battery-create-topic"})
class BatteryApiTest {

    @Container
    private static final PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private BatteryRepository batteryRepository;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/batteries";
    }

    @BeforeEach
    void setUp() {
        batteryRepository.deleteAll();
    }

    @Test
    void should_create_batteries_and_return_ids() {
        // given
        var batteries = List.of(
                Map.of("name", "Battery A", "postcode", "2000", "capacity", 500),
                Map.of("name", "Battery B", "postcode", "2500", "capacity", 600)
        );

        // when
        ResponseEntity<List> response = restTemplate.exchange(
                getBaseUrl(),
                HttpMethod.POST,
                new HttpEntity<>(batteries),
                List.class
        );

        // then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).hasSize(2);

        var savedBatteries = batteryRepository.findAll();
        assertThat(savedBatteries).hasSize(2);
        assertThat(savedBatteries.get(0).getName()).isEqualTo("Battery A");
        assertThat(savedBatteries.get(1).getName()).isEqualTo("Battery B");
    }

    @Test
    void should_return_bad_request_when_name_is_empty() {
        // given
        var invalidBatteries = List.of(
                // Invalid name
                Map.of("name", "", "postcode", "2000", "capacity", 500)
        );

        // when
        ResponseEntity<Map> response = restTemplate.exchange(
                getBaseUrl(),
                HttpMethod.POST,
                new HttpEntity<>(invalidBatteries),
                Map.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void should_return_bad_request_when_postcode_is_invalid() {
        // given
        var invalidBatteries = List.of(
                // Invalid postcode
                Map.of("name", "Battery B", "postcode", "25", "capacity", 600)
        );

        // when
        ResponseEntity<Map> response = restTemplate.exchange(
                getBaseUrl(),
                HttpMethod.POST,
                new HttpEntity<>(invalidBatteries),
                Map.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void should_return_batteries_in_range_with_summary() {
        // given
        Battery battery1 = new Battery();
        battery1.setName("Battery A");
        battery1.setPostcode("2000");
        battery1.setWattCapacity(500);

        Battery battery2 = new Battery();
        battery2.setName("Battery B");
        battery2.setPostcode("2500");
        battery2.setWattCapacity(600);

        Battery battery3 = new Battery();
        battery3.setName("Battery C");
        battery3.setPostcode("3500");
        battery3.setWattCapacity(700);

        batteryRepository.saveAll(List.of(battery1, battery2, battery3));

        // when
        ResponseEntity<Map> response = restTemplate.exchange(
                getBaseUrl() + "?minPostCode=2000&maxPostCode=3000",
                HttpMethod.GET,
                null,
                Map.class
        );

        // then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        assertThat((List<String>) responseBody.get("batteries")).containsExactly("Battery A", "Battery B");
        assertThat(responseBody.get("totalCapacity")).isEqualTo(1100);
        assertThat(responseBody.get("averageCapacity")).isEqualTo(550.0);
    }

    @Test
    void should_return_batteries_with_pagination() {
        // given
        Battery battery1 = new Battery();
        battery1.setName("Battery A");
        battery1.setPostcode("2000");
        battery1.setWattCapacity(500);

        Battery battery2 = new Battery();
        battery2.setName("Battery B");
        battery2.setPostcode("2500");
        battery2.setWattCapacity(600);

        Battery battery3 = new Battery();
        battery3.setName("Battery C");
        battery3.setPostcode("3500");
        battery3.setWattCapacity(700);

        batteryRepository.saveAll(List.of(battery1, battery2, battery3));

        // when with page = 0 and size = 2
        ResponseEntity<Map> response = restTemplate.exchange(
                getBaseUrl() + "?minPostCode=2000&maxPostCode=3500&page=0&size=2",
                HttpMethod.GET,
                null,
                Map.class
        );

        // then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        assertThat((List<String>) responseBody.get("batteries")).containsExactly("Battery A", "Battery B");
        assertThat(responseBody.get("totalCapacity")).isEqualTo(1800);
        assertThat(responseBody.get("averageCapacity")).isEqualTo(600.0);
        assertThat(responseBody.get("totalBatteries")).isEqualTo(3);

        // when again with page = 1 and size = 2
        ResponseEntity<Map> responseWithPage1 = restTemplate.exchange(
                getBaseUrl() + "?minPostCode=2000&maxPostCode=3500&page=1&size=2",
                HttpMethod.GET,
                null,
                Map.class
        );

        // then
        assertThat(responseWithPage1.getStatusCode().is2xxSuccessful()).isTrue();
        Map<String, Object> secondResponseBody = responseWithPage1.getBody();
        assertThat(secondResponseBody).isNotNull();
        assertThat((List<String>) secondResponseBody.get("batteries")).containsExactly("Battery C");
        assertThat(secondResponseBody.get("totalCapacity")).isEqualTo(null);
        assertThat(secondResponseBody.get("averageCapacity")).isEqualTo(null);
        assertThat(secondResponseBody.get("totalBatteries")).isEqualTo(null);
    }

    @Test
    void should_return_bad_request_when_get_params_are_invalid() {
        // when
        ResponseEntity<Map> response = restTemplate.exchange(
                getBaseUrl() + "?minPostCode=abc&maxPostCode=3000", // Invalid minPostCode
                HttpMethod.GET,
                null,
                Map.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }


    @Test
    void should_search_batteries_with_criteria() {
        // given
        Battery battery1 = new Battery();
        battery1.setName("Battery A");
        battery1.setPostcode("2000");
        battery1.setWattCapacity(500);

        Battery battery2 = new Battery();
        battery2.setName("Battery B");
        battery2.setPostcode("2500");
        battery2.setWattCapacity(600);

        Battery battery3 = new Battery();
        battery3.setName("Battery C");
        battery3.setPostcode("3500");
        battery3.setWattCapacity(700);

        batteryRepository.saveAll(List.of(battery1, battery2, battery3));

        // when
        ResponseEntity<Map> response = restTemplate.exchange(
                getBaseUrl() + "/search",
                HttpMethod.POST,
                new HttpEntity<>(Map.of(
                        "minPostCode", "2000",
                        "maxPostCode", "3500",
                        "minCapacity", 500,
                        "maxCapacity", 600
                )),
                Map.class
        );

        // then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        assertThat((List<String>) responseBody.get("batteries")).containsExactly("Battery A", "Battery B");
        assertThat(responseBody.get("totalCapacity")).isEqualTo(1100);
        assertThat(responseBody.get("averageCapacity")).isEqualTo(550.0);
    }

    @Test
    void should_return_all_batteries_when_request_is_null_in_search() {
        // given
        Battery battery1 = new Battery();
        battery1.setName("Battery A");
        battery1.setPostcode("2000");
        battery1.setWattCapacity(500);

        Battery battery2 = new Battery();
        battery2.setName("Battery B");
        battery2.setPostcode("2500");
        battery2.setWattCapacity(600);

        Battery battery3 = new Battery();
        battery3.setName("Battery C");
        battery3.setPostcode("3500");
        battery3.setWattCapacity(700);

        batteryRepository.saveAll(List.of(battery1, battery2, battery3));

        // when
        ResponseEntity<Map> responseWithNullBody = restTemplate.exchange(
                getBaseUrl() + "/search",
                HttpMethod.POST,
                new HttpEntity<>(Map.of()),
                Map.class
        );

        // then
        assertThat(responseWithNullBody.getStatusCode().is2xxSuccessful()).isTrue();
        Map<String, Object> responseBodyWithNullBody = responseWithNullBody.getBody();
        assertThat(responseBodyWithNullBody).isNotNull();
        assertThat((List<String>) responseBodyWithNullBody.get("batteries"))
                .containsExactly("Battery A", "Battery B", "Battery C");
        assertThat(responseBodyWithNullBody.get("totalCapacity")).isEqualTo(1800);
        assertThat(responseBodyWithNullBody.get("averageCapacity")).isEqualTo(600.0);
    }

    @Test
    void should_send_battery_creation_messages_to_kafka() throws InterruptedException {
        // given
        var batteries = List.of(
                Map.of("name", "Battery A", "postcode", "2000", "capacity", 500),
                Map.of("name", "Battery B", "postcode", "2500", "capacity", 600)
        );

        // Configure Kafka consumer
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(
                "test-group", "true", embeddedKafkaBroker);
        ConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(
                consumerProps, new StringDeserializer(), new StringDeserializer());
        var consumer = consumerFactory.createConsumer();
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, "battery-create-topic");

        // when
        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl() + "/async",
                HttpMethod.POST,
                new HttpEntity<>(batteries),
                String.class
        );

        // then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEqualTo("Battery creation sent successfully.");

        // Verify Kafka messages
        ConsumerRecords<String, String> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(5));
        assertThat(records.count()).isEqualTo(2);

        for (ConsumerRecord<String, String> record : records) {
            assertThat(record.value()).containsAnyOf("Battery A", "Battery B");
        }

        consumer.close();
    }
}
