package io.powerledger.vpp.batterymanagement.controller;

import io.powerledger.vpp.batterymanagement.dto.BatteryDto;
import io.powerledger.vpp.batterymanagement.dto.BatteryResponseDto;
import io.powerledger.vpp.batterymanagement.dto.BatterySearchRequestDto;
import io.powerledger.vpp.batterymanagement.service.BatteryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.UUID;

import static io.powerledger.vpp.batterymanagement.util.Constant.AUSTRALIAN_POSTCODE_REGEX;

@RestController
@Validated
public class BatteryController {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final BatteryService batteryService;

    public BatteryController(BatteryService batteryService) {
        this.batteryService = batteryService;
    }

    @PostMapping("/batteries")
    public ResponseEntity<List<UUID>> createBatteries(@RequestBody @Valid List<BatteryDto> batteries) {
        var batteryIds = batteries.stream().map(batteryService::createBattery).toList();
        log.info("Batteries created with IDs: {}", batteryIds);
        return ResponseEntity.ok(batteryIds);
    }

    @GetMapping("/batteries")
    public ResponseEntity<BatteryResponseDto> getBatteriesInRange(
            @RequestParam 
            @NotNull(message = "minPostCode is required.") 
            @Pattern(regexp = AUSTRALIAN_POSTCODE_REGEX, message = "Invalid Australian postcode.")
            String minPostCode,
            @RequestParam 
            @NotNull(message = "maxPostCode is required.") 
            @Pattern(regexp = AUSTRALIAN_POSTCODE_REGEX, message = "Invalid Australian postcode.")
            String maxPostCode) {
        log.info("Fetching batteries in postcode range: {} - {}", minPostCode, maxPostCode);
        var batteries = batteryService.getBatteryByMinAndMaxPostCode(minPostCode, maxPostCode);

        var batteryNames = batteries.stream()
                .map(BatteryDto::getName)
                .toList();
        var totalCapacity = batteries.stream()
                .mapToInt(BatteryDto::getCapacity)
                .sum();
        var averageCapacity = batteries.isEmpty() ? 0 : (double) totalCapacity / batteries.size();

        var response = new BatteryResponseDto();
        response.setBatteries(batteryNames);
        response.setTotalCapacity(totalCapacity);
        response.setAverageCapacity(averageCapacity);

        log.info("Found {} batteries in range with total capacity {} and average capacity {}",
                batteries.size(), totalCapacity, averageCapacity);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/batteries/search")
    public ResponseEntity<BatteryResponseDto> searchBatteries(@RequestBody @Valid BatterySearchRequestDto searchRequest) {
        log.info("Searching batteries with criteria: {}", searchRequest);
        var batteries = batteryService.searchBatteries(searchRequest);

        var batteryNames = batteries.stream()
                .map(BatteryDto::getName)
                .toList();
        var totalCapacity = batteries.stream()
                .mapToInt(BatteryDto::getCapacity)
                .sum();
        var averageCapacity = batteries.isEmpty() ? 0 : (double) totalCapacity / batteries.size();

        var response = new BatteryResponseDto();
        response.setBatteries(batteryNames);
        response.setTotalCapacity(totalCapacity);
        response.setAverageCapacity(averageCapacity);
        return ResponseEntity.ok(response);
    }
}
