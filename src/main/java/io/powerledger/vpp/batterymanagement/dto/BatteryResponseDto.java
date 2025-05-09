package io.powerledger.vpp.batterymanagement.dto;

import java.util.List;

public class BatteryResponseDto {
    private List<String> batteries;
    private Long totalCapacity;
    private Double averageCapacity;
    private Long totalBatteries;

    public List<String> getBatteries() {
        return batteries;
    }

    public void setBatteries(List<String> batteries) {
        this.batteries = batteries;
    }

    public Long getTotalCapacity() {
        return totalCapacity;
    }

    public void setTotalCapacity(Long totalCapacity) {
        this.totalCapacity = totalCapacity;
    }

    public Double getAverageCapacity() {
        return averageCapacity;
    }

    public void setAverageCapacity(double averageCapacity) {
        this.averageCapacity = averageCapacity;
    }

    public Long getTotalBatteries() {
        return totalBatteries;
    }

    public void setTotalBatteries(Long totalBatteries) {
        this.totalBatteries = totalBatteries;
    }
}
