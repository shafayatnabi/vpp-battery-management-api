package io.powerledger.vpp.batterymanagement.controller;

import io.powerledger.vpp.batterymanagement.dto.BatteryDto;
import io.powerledger.vpp.batterymanagement.dto.BatteryResponseDto;
import io.powerledger.vpp.batterymanagement.dto.SummaryDto;
import io.powerledger.vpp.batterymanagement.service.BatteryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BatteryControllerTest {

    @Mock
    private BatteryService batteryService;

    @InjectMocks
    private BatteryController batteryController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void should_create_batteries_and_return_ids() {
        // given
        BatteryDto battery1 = new BatteryDto();
        battery1.setName("Battery A");
        battery1.setPostcode("2000");
        battery1.setCapacity(500);

        BatteryDto battery2 = new BatteryDto();
        battery2.setName("Battery B");
        battery2.setPostcode("2500");
        battery2.setCapacity(600);

        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        when(batteryService.createBattery(battery1)).thenReturn(id1);
        when(batteryService.createBattery(battery2)).thenReturn(id2);

        // when
        ResponseEntity<List<UUID>> response = batteryController.createBatteries(List.of(battery1, battery2));

        // then
        assertThat(response.getBody()).containsExactly(id1, id2);
        verify(batteryService, times(1)).createBattery(battery1);
        verify(batteryService, times(1)).createBattery(battery2);
    }

    @Test
    void should_create_batteries_async() {
        // given
        BatteryDto battery1 = new BatteryDto();
        battery1.setName("Battery A");
        battery1.setPostcode("2000");
        battery1.setCapacity(500);

        BatteryDto battery2 = new BatteryDto();
        battery2.setName("Battery B");
        battery2.setPostcode("2500");
        battery2.setCapacity(600);

        // when
        ResponseEntity<String> response = batteryController.createBatteriesAsync(List.of(battery1, battery2));

        // then
        assertThat(response.getBody()).isEqualTo("Battery creation sent successfully.");
        verify(batteryService, times(1)).sendBatteryCreationMessage(battery1);
        verify(batteryService, times(1)).sendBatteryCreationMessage(battery2);
    }

    @Test
    void should_return_batteries_in_range_with_summary() {
        // given
        String minPostCode = "2000";
        String maxPostCode = "3000";

        BatteryDto battery1 = new BatteryDto();
        battery1.setName("Battery A");
        battery1.setPostcode("2000");
        battery1.setCapacity(500);

        BatteryDto battery2 = new BatteryDto();
        battery2.setName("Battery B");
        battery2.setPostcode("2500");
        battery2.setCapacity(600);
        Pageable pageable = PageRequest.of(0, 10);

        SummaryDto summaryDto = new SummaryDto();
        summaryDto.setTotalBatteries(2L);
        summaryDto.setTotalCapacity(1100L);
        summaryDto.setAverageCapacity(550.0);
        when(batteryService.getBatteryByMinAndMaxPostCode(minPostCode, maxPostCode, pageable))
                .thenReturn(List.of(battery1, battery2));
        when(batteryService.getSummaryByPostcodeRange(minPostCode, maxPostCode))
                .thenReturn(summaryDto);

        // when
        ResponseEntity<BatteryResponseDto> response = batteryController.getBatteriesInRange(minPostCode, maxPostCode, 0, 10);

        // then
        BatteryResponseDto responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getBatteries()).containsExactly("Battery A", "Battery B");
        assertThat(responseBody.getTotalCapacity()).isEqualTo(1100L);
        assertThat(responseBody.getAverageCapacity()).isEqualTo(550.0);
        assertThat(responseBody.getTotalBatteries()).isEqualTo(2L);

        verify(batteryService, times(1)).getBatteryByMinAndMaxPostCode(minPostCode, maxPostCode, pageable);
        verify(batteryService, times(1)).getSummaryByPostcodeRange(minPostCode, maxPostCode);
    }

    @Test
    void should_return_batteries_in_range_without_summary() {
        // given
        String minPostCode = "2000";
        String maxPostCode = "3000";

        BatteryDto battery1 = new BatteryDto();
        battery1.setName("Battery A");
        battery1.setPostcode("2000");
        battery1.setCapacity(500);

        BatteryDto battery2 = new BatteryDto();
        battery2.setName("Battery B");
        battery2.setPostcode("2500");
        battery2.setCapacity(600);
        Pageable pageable = PageRequest.of(1, 10);

        when(batteryService.getBatteryByMinAndMaxPostCode(minPostCode, maxPostCode, pageable))
                .thenReturn(List.of(battery1, battery2));

        // when
        ResponseEntity<BatteryResponseDto> response = batteryController.getBatteriesInRange(minPostCode, maxPostCode, 1, 10);

        // then
        BatteryResponseDto responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getBatteries()).containsExactly("Battery A", "Battery B");
        assertThat(responseBody.getTotalCapacity()).isNull();
        assertThat(responseBody.getAverageCapacity()).isNull();
        assertThat(responseBody.getTotalBatteries()).isNull();

        verify(batteryService, times(1)).getBatteryByMinAndMaxPostCode(minPostCode, maxPostCode, pageable);
        verify(batteryService, times(0)).getSummaryByPostcodeRange(minPostCode, maxPostCode);
    }
}
