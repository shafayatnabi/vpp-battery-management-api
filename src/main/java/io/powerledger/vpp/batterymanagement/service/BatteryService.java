package io.powerledger.vpp.batterymanagement.service;

import io.powerledger.vpp.batterymanagement.dto.BatteryDto;
import io.powerledger.vpp.batterymanagement.dto.BatterySearchRequestDto;
import io.powerledger.vpp.batterymanagement.dto.SummaryDto;
import io.powerledger.vpp.batterymanagement.model.Battery;
import io.powerledger.vpp.batterymanagement.model.BatterySummary;
import io.powerledger.vpp.batterymanagement.repository.BatteryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BatteryService {

    private final BatteryRepository batteryRepository;

    @Autowired
    public BatteryService(BatteryRepository batteryRepository) {
        this.batteryRepository = batteryRepository;
    }

    public UUID createBattery(BatteryDto batteryDto) {
        var battery = new Battery();
        battery.setName(batteryDto.getName());
        battery.setPostcode(batteryDto.getPostcode());
        battery.setWattCapacity(batteryDto.getCapacity());
        var savedBattery = batteryRepository.save(battery);
        return savedBattery.getId();
    }

    public List<BatteryDto> getBatteryByMinAndMaxPostCode(String minPostCode, String maxPostCode, Pageable pageable) {
        Page<Battery> batteries = batteryRepository.findByPostcodeRangeOrderByName(minPostCode, maxPostCode, pageable);
        return batteries.stream().map(battery -> {
            BatteryDto dto = new BatteryDto();
            dto.setName(battery.getName());
            dto.setPostcode(battery.getPostcode());
            dto.setCapacity(battery.getWattCapacity());
            return dto;
        }).collect(Collectors.toList());
    }

    public List<BatteryDto> searchBatteries(BatterySearchRequestDto searchRequest, Pageable pageable) {
        Page<Battery> batteries = batteryRepository.findBySearchCriteria(
                searchRequest.getMinPostCode(),
                searchRequest.getMaxPostCode(),
                searchRequest.getMinCapacity(),
                searchRequest.getMaxCapacity(),
                pageable
        );
        return batteries.stream().map(battery -> {
            BatteryDto dto = new BatteryDto();
            dto.setName(battery.getName());
            dto.setPostcode(battery.getPostcode());
            dto.setCapacity(battery.getWattCapacity());
            return dto;
        }).collect(Collectors.toList());
    }

    public SummaryDto getSummaryByPostcodeRange(String minPostCode, String maxPostCode) {
        BatterySummary summary = batteryRepository.findSummaryByPostcodeRange(minPostCode, maxPostCode);

        SummaryDto summaryDto = new SummaryDto();
        summaryDto.setTotalBatteries(summary.getCount());
        summaryDto.setTotalCapacity(summary.getTotalWattCapacity());
        summaryDto.setAverageCapacity(summary.getAverageWattCapacity());

        return summaryDto;
    }

    public SummaryDto getSummaryBySearchCriteria(BatterySearchRequestDto searchRequest) {
        BatterySummary summary = batteryRepository.findSummaryBySearchCriteria(
                searchRequest.getMinPostCode(),
                searchRequest.getMaxPostCode(),
                searchRequest.getMinCapacity(),
                searchRequest.getMaxCapacity()
        );

        SummaryDto summaryDto = new SummaryDto();
        summaryDto.setTotalBatteries(summary.getCount());
        summaryDto.setTotalCapacity(summary.getTotalWattCapacity());
        summaryDto.setAverageCapacity(summary.getAverageWattCapacity());

        return summaryDto;
    }
}
