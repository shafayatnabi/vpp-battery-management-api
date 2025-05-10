package io.powerledger.vpp.batterymanagement.controller;

import io.powerledger.vpp.batterymanagement.dto.BatteryDto;
import io.powerledger.vpp.batterymanagement.dto.BatteryResponseDto;
import io.powerledger.vpp.batterymanagement.dto.BatterySearchRequestDto;
import io.powerledger.vpp.batterymanagement.service.BatteryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    @PostMapping("/batteries/async")
    public ResponseEntity<String> createBatteriesAsync(@RequestBody @Valid List<BatteryDto> batteries) {
        batteries.forEach(batteryService::sendBatteryCreationMessage);
        log.info("Battery creation messages sent successfully.");
        return ResponseEntity.accepted().body("Battery creation sent successfully.");
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
            String maxPostCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Fetching batteries in postcode range: {} - {} with page {} and size {}", minPostCode, maxPostCode, page, size);
        Pageable pageable = PageRequest.of(page, size);
        var batteries = batteryService.getBatteryByMinAndMaxPostCode(minPostCode, maxPostCode, pageable);

        var response = new BatteryResponseDto();
        // returning summary only for the first page
        if (page == 0) {
            var summary = batteryService.getSummaryByPostcodeRange(
                    minPostCode,
                    maxPostCode
            );
            response.setTotalCapacity(summary.getTotalCapacity());
            response.setAverageCapacity(summary.getAverageCapacity());
            response.setTotalBatteries(summary.getTotalBatteries());
            log.info("Summary for postcode range {} - {}: Total Batteries: {}, Total Capacity: {}, Average Capacity: {}",
                    minPostCode, maxPostCode, summary.getTotalBatteries(), summary.getTotalCapacity(), summary.getAverageCapacity());
        }

        log.info("Found {} batteries in range", batteries.size());

        var batteryNames = batteries.stream()
                .map(BatteryDto::getName)
                .toList();

        response.setBatteries(batteryNames);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/batteries/search")
    public ResponseEntity<BatteryResponseDto> searchBatteries(
            @RequestBody(required = false) @Valid BatterySearchRequestDto searchRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        if (searchRequest == null) {
            log.info("No search criteria provided, using default empty search request.");
            searchRequest = new BatterySearchRequestDto();
        }
        log.info("Searching batteries with criteria: {}, with page {} and size {}", searchRequest, page, size);
        Pageable pageable = PageRequest.of(page, size);
        var batteries = batteryService.searchBatteries(searchRequest, pageable);

        var response = new BatteryResponseDto();

        // returning summary only for the first page
        if (page == 0) {
            var summary = batteryService.getSummaryBySearchCriteria(
                    searchRequest
            );
            response.setTotalCapacity(summary.getTotalCapacity());
            response.setAverageCapacity(summary.getAverageCapacity());
            response.setTotalBatteries(summary.getTotalBatteries());
            log.info("Summary for search criteria {}: Total Batteries: {}, Total Capacity: {}, Average Capacity: {}",
                    searchRequest, summary.getTotalBatteries(), summary.getTotalCapacity(), summary.getAverageCapacity());
        }

        var batteryNames = batteries.stream()
                .map(BatteryDto::getName)
                .toList();
        log.info("Found {} batteries matching criteria", batteryNames.size());
        response.setBatteries(batteryNames);
        return ResponseEntity.ok(response);
    }
}

