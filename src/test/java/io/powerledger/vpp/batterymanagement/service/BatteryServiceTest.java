package io.powerledger.vpp.batterymanagement.service;

import io.powerledger.vpp.batterymanagement.dto.BatteryDto;
import io.powerledger.vpp.batterymanagement.model.Battery;
import io.powerledger.vpp.batterymanagement.repository.BatteryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
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

        when(batteryRepository.findByPostcodeRangeOrderByName("2000", "3000"))
                .thenReturn(List.of(battery1, battery2));

        // when
        List<BatteryDto> result = batteryService.getBatteryByMinAndMaxPostCode("2000", "3000");

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Battery A");
        assertThat(result.get(0).getPostcode()).isEqualTo("2000");
        assertThat(result.get(0).getCapacity()).isEqualTo(500);

        assertThat(result.get(1).getName()).isEqualTo("Battery B");
        assertThat(result.get(1).getPostcode()).isEqualTo("2500");
        assertThat(result.get(1).getCapacity()).isEqualTo(600);

        verify(batteryRepository, times(1)).findByPostcodeRangeOrderByName("2000", "3000");
    }
}
