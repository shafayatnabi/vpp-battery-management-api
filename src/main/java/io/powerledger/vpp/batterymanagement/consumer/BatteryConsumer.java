package io.powerledger.vpp.batterymanagement.consumer;

import io.powerledger.vpp.batterymanagement.dto.BatteryDto;
import io.powerledger.vpp.batterymanagement.service.BatteryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

@Component
public class BatteryConsumer {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final BatteryService batteryService;

    public BatteryConsumer(BatteryService batteryService) {
        this.batteryService = batteryService;
    }

    @KafkaListener(topics = "${kafka.topic.battery-create}", groupId = "battery-group")
    public void consumeBatteryMessage(BatteryDto batteryDto) {
        batteryService.createBattery(batteryDto);
        log.info("Battery saved to database: {}", batteryDto);
    }
}
