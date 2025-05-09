package io.powerledger.vpp.batterymanagement.service;

import io.powerledger.vpp.batterymanagement.dto.BatteryDto;
import io.powerledger.vpp.batterymanagement.dto.BatterySearchRequestDto;
import io.powerledger.vpp.batterymanagement.dto.SummaryDto;
import io.powerledger.vpp.batterymanagement.model.Battery;
import io.powerledger.vpp.batterymanagement.model.BatterySummary;
import io.powerledger.vpp.batterymanagement.repository.BatteryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BatteryServiceTest {

    @Mock
    private BatteryRepository batteryRepository;

    @InjectMocks
    private BatteryService batteryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void should_save_battery_when_create_battery_called() {
        // given
        BatteryDto batteryDto = new BatteryDto();
        batteryDto.setName("Battery A");
        batteryDto.setPostcode("2000");
        batteryDto.setCapacity(500);

        Battery savedBattery = new Battery();
        savedBattery.setId(UUID.randomUUID());
        when(batteryRepository.save(any(Battery.class))).thenReturn(savedBattery);

        // when
        UUID batteryId = batteryService.createBattery(batteryDto);

        // then
        ArgumentCaptor<Battery> batteryCaptor = ArgumentCaptor.forClass(Battery.class);
        verify(batteryRepository, times(1)).save(batteryCaptor.capture());

        Battery capturedBattery = batteryCaptor.getValue();
        assertThat(capturedBattery.getName()).isEqualTo("Battery A");
        assertThat(capturedBattery.getPostcode()).isEqualTo("2000");
        assertThat(capturedBattery.getWattCapacity()).isEqualTo(500);
        assertThat(batteryId).isEqualTo(savedBattery.getId());
    }

    @Test
    void should_return_filtered_batteries_in_get_battery_by_min_and_max_postcode() {
        // given
        Battery battery1 = new Battery();
        battery1.setName("Battery A");
        battery1.setPostcode("2000");
        battery1.setWattCapacity(500);

        Battery battery2 = new Battery();
        battery2.setName("Battery B");
        battery2.setPostcode("2500");
        battery2.setWattCapacity(600);

        Pageable pageable = PageRequest.of(0, 10);
        when(batteryRepository.findByPostcodeRangeOrderByName("2000", "3000", pageable))
                .thenReturn(new PageImpl<>(List.of(battery1, battery2), pageable, 2));

        // when
        List<BatteryDto> result = batteryService.getBatteryByMinAndMaxPostCode("2000", "3000", pageable);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Battery A");
        assertThat(result.get(0).getPostcode()).isEqualTo("2000");
        assertThat(result.get(0).getCapacity()).isEqualTo(500);

        assertThat(result.get(1).getName()).isEqualTo("Battery B");
        assertThat(result.get(1).getPostcode()).isEqualTo("2500");
        assertThat(result.get(1).getCapacity()).isEqualTo(600);

        verify(batteryRepository, times(1)).findByPostcodeRangeOrderByName("2000", "3000", pageable);
    }

    @Test
    void should_return_filtered_batteries_with_pagination() {
        // given
        Battery battery1 = new Battery();
        battery1.setName("Battery A");
        battery1.setPostcode("2000");
        battery1.setWattCapacity(500);

        Battery battery2 = new Battery();
        battery2.setName("Battery B");
        battery2.setPostcode("2500");
        battery2.setWattCapacity(600);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Battery> batteryPage = new PageImpl<>(List.of(battery1, battery2), pageable, 2);

        when(batteryRepository.findByPostcodeRangeOrderByName("2000", "3000", pageable))
                .thenReturn(batteryPage);

        // when
        List<BatteryDto> result = batteryService.getBatteryByMinAndMaxPostCode("2000", "3000", pageable);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Battery A");
        assertThat(result.get(1).getName()).isEqualTo("Battery B");
    }

    @Test
    void should_search_batteries_with_pagination() {
        // given
        Battery battery1 = new Battery();
        battery1.setName("Battery A");
        battery1.setPostcode("2000");
        battery1.setWattCapacity(500);

        Battery battery2 = new Battery();
        battery2.setName("Battery B");
        battery2.setPostcode("2500");
        battery2.setWattCapacity(600);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Battery> batteryPage = new PageImpl<>(List.of(battery1, battery2), pageable, 2);

        when(batteryRepository.findBySearchCriteria(any(), any(), any(), any(), eq(pageable)))
                .thenReturn(batteryPage);

        // when
        List<BatteryDto> result = batteryService.searchBatteries(new BatterySearchRequestDto(), pageable);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Battery A");
        assertThat(result.get(1).getName()).isEqualTo("Battery B");
    }

    @Test
    void should_return_summary_by_postcode_range() {
        // given
        String minPostCode = "2000";
        String maxPostCode = "3000";

        BatterySummary summary = new BatterySummary(2L, 1100L, 550.0);

        when(batteryRepository.findSummaryByPostcodeRange(minPostCode, maxPostCode)).thenReturn(summary);

        // when
        SummaryDto result = batteryService.getSummaryByPostcodeRange(minPostCode, maxPostCode);

        // then
        assertThat(result.getTotalBatteries()).isEqualTo(2);
        assertThat(result.getTotalCapacity()).isEqualTo(1100);
        assertThat(result.getAverageCapacity()).isEqualTo(550);
    }

    @Test
    void should_return_summary_by_search_criteria() {
        // given
        BatterySearchRequestDto searchRequest = new BatterySearchRequestDto();
        searchRequest.setMinPostCode("2000");
        searchRequest.setMaxPostCode("3000");
        searchRequest.setMinCapacity(400);
        searchRequest.setMaxCapacity(700);

        BatterySummary summary = new BatterySummary(3L, 1800L, 600.0);

        when(batteryRepository.findSummaryBySearchCriteria(
                searchRequest.getMinPostCode(),
                searchRequest.getMaxPostCode(),
                searchRequest.getMinCapacity(),
                searchRequest.getMaxCapacity()
        )).thenReturn(summary);

        // when
        SummaryDto result = batteryService.getSummaryBySearchCriteria(searchRequest);

        // then
        assertThat(result.getTotalBatteries()).isEqualTo(3);
        assertThat(result.getTotalCapacity()).isEqualTo(1800);
        assertThat(result.getAverageCapacity()).isEqualTo(600.0);

        verify(batteryRepository, times(1)).findSummaryBySearchCriteria(
                searchRequest.getMinPostCode(),
                searchRequest.getMaxPostCode(),
                searchRequest.getMinCapacity(),
                searchRequest.getMaxCapacity()
        );
    }
}
