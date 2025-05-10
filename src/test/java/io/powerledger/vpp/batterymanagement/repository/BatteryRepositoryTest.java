package io.powerledger.vpp.batterymanagement.repository;

import io.powerledger.vpp.batterymanagement.model.Battery;
import io.powerledger.vpp.batterymanagement.model.BatterySummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private static final PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @Autowired
    private BatteryRepository batteryRepository;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
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
        Pageable pageable = PageRequest.of(0, 10);

        // when
        List<Battery> batteries = batteryRepository.findByPostcodeRangeOrderByName(
                "2000", "3000", pageable
        ).stream().toList();

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
        Pageable pageable = PageRequest.of(0, 10);

        // when
        List<Battery> batteries = batteryRepository.findByPostcodeRangeOrderByName(
                "0", "1999", pageable
        ).stream().toList();

        // then
        assertThat(batteries).hasSize(0);
    }

    @Test
    void return_empty_list_when_no_records_in_table() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        List<Battery> batteries = batteryRepository.findByPostcodeRangeOrderByName(
                "0", "1999", pageable).stream().toList();

        // then
        assertThat(batteries).hasSize(0);
    }

    @Test
    void find_by_postcode_range_with_pagination() {
        // given 3 battery
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

        Pageable firstPage = PageRequest.of(0, 2);

        // when with page = 0 and size = 2
        Page<Battery> firstPageResult = batteryRepository.findByPostcodeRangeOrderByName(
                "2000", "3500", firstPage
        );

        // then
        assertThat(firstPageResult.getContent()).hasSize(2);
        assertThat(firstPageResult.getContent().get(0).getName()).isEqualTo("Battery A");
        assertThat(firstPageResult.getContent().get(1).getName()).isEqualTo("Battery B");


        // again when with page = 1 and size = 2
        Pageable secondPage = PageRequest.of(1, 2);
        Page<Battery> secondPageResult = batteryRepository.findByPostcodeRangeOrderByName(
                "2000", "3500", secondPage
        );

        // then
        assertThat(secondPageResult.getContent()).hasSize(1);
        assertThat(secondPageResult.getContent().get(0).getName()).isEqualTo("Battery C");

        // again when with page = 2 and size = 2
        Pageable thirdPage = PageRequest.of(2, 2);
        Page<Battery> thirdPageResult = batteryRepository.findByPostcodeRangeOrderByName(
                "2000", "3500", thirdPage
        );

        // then
        assertThat(thirdPageResult.getContent()).hasSize(0);

    }

    @Test
    void find_by_search_criteria_with_pagination() {
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

        Pageable firstPage = PageRequest.of(0, 2);

        // when with page = 0 and size = 2
        Page<Battery> firstPageResult = batteryRepository.findBySearchCriteria(
                "2000", "3500", 400, 700, firstPage
        );

        // then
        assertThat(firstPageResult.getContent()).hasSize(2);
        assertThat(firstPageResult.getContent().get(0).getName()).isEqualTo("Battery A");
        assertThat(firstPageResult.getContent().get(1).getName()).isEqualTo("Battery B");

        // again when with page = 1 and size = 2
        Pageable secondPage = PageRequest.of(1, 2);
        Page<Battery> secondPageResult = batteryRepository.findBySearchCriteria(
                "2000", "3500", 400, 700, secondPage
        );

        // then
        assertThat(secondPageResult.getContent()).hasSize(1);
        assertThat(secondPageResult.getContent().get(0).getName()).isEqualTo("Battery C");
    }

    @Test
    void find_summary_by_postcode_range() {
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
        BatterySummary summary = batteryRepository.findSummaryByPostcodeRange(
                "2000", "3500"
        );

        // then
        assertThat(summary.count()).isEqualTo(3L);
        assertThat(summary.totalWattCapacity()).isEqualTo(1800L);
        assertThat(summary.averageWattCapacity()).isEqualTo(600.0);
    }

    @Test
    void find_summary_by_search_criteria() {
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
        BatterySummary summary = batteryRepository.findSummaryBySearchCriteria(
                "2000", "3500", 400, 700
        );

        // then
        assertThat(summary.count()).isEqualTo(3L);
        assertThat(summary.totalWattCapacity()).isEqualTo(1800L);
        assertThat(summary.averageWattCapacity()).isEqualTo(600.0);
    }

    @Test
    void find_summary_by_search_criteria_with_null_parameters() {
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
        BatterySummary summary = batteryRepository.findSummaryBySearchCriteria(
                null, null, null, null
        );

        // then
        assertThat(summary.count()).isEqualTo(3L);
        assertThat(summary.totalWattCapacity()).isEqualTo(1800L);
        assertThat(summary.averageWattCapacity()).isEqualTo(600.0);
    }
}
