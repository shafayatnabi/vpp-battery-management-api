package io.powerledger.vpp.batterymanagement.repository;

import io.powerledger.vpp.batterymanagement.model.Battery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class BatteryRepositoryTest {

    @Container
    private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @Autowired
    private BatteryRepository batteryRepository;

    @DynamicPropertySource
    static void setProperties(org.springframework.test.context.DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @BeforeEach
    void setUp() {
        batteryRepository.deleteAll();
    }

    @Test
    void find_by_postcode_range_order_by_name_filter_batteries_successfully() {
        // given
        Battery battery1 = new Battery();
        battery1.setName("Battery A");
        battery1.setPostcode("2000");
        battery1.setWattCapacity(500);

        Battery battery2 = new Battery();
        battery2.setName("Battery B");
        battery2.setPostcode("3500");
        battery2.setWattCapacity(600);

        Battery battery3 = new Battery();
        battery3.setName("Battery C");
        battery3.setPostcode("2500");
        battery3.setWattCapacity(700);
        batteryRepository.saveAll(List.of(battery1, battery2, battery3));

        // when
        List<Battery> batteries = batteryRepository.findByPostcodeRangeOrderByName("2000", "3000");

        // then
        assertThat(batteries).hasSize(2);
        assertThat(batteries.get(0).getName()).isEqualTo("Battery A");
        assertThat(batteries.get(1).getName()).isEqualTo("Battery C");
    }

    @Test
    void return_empty_list_when_no_batteries_in_range() {
        // given
        Battery battery1 = new Battery();
        battery1.setName("Battery A");
        battery1.setPostcode("2000");
        battery1.setWattCapacity(500);

        Battery battery2 = new Battery();
        battery2.setName("Battery B");
        battery2.setPostcode("3500");
        battery2.setWattCapacity(600);

        Battery battery3 = new Battery();
        battery3.setName("Battery C");
        battery3.setPostcode("2500");
        battery3.setWattCapacity(700);
        batteryRepository.saveAll(List.of(battery1, battery2, battery3));

        // when
        List<Battery> batteries = batteryRepository.findByPostcodeRangeOrderByName("0", "1999");

        // then
        assertThat(batteries).hasSize(0);
    }

    @Test
    void return_empty_list_when_no_records_in_table() {
        // when
        List<Battery> batteries = batteryRepository.findByPostcodeRangeOrderByName("0", "1999");

        // then
        assertThat(batteries).hasSize(0);
    }
}
