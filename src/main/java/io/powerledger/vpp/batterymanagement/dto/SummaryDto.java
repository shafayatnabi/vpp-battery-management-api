package io.powerledger.vpp.batterymanagement.dto;

public class SummaryDto {
    private Long totalBatteries;
    private Long totalCapacity;
    private Double averageCapacity;

    public Long getTotalBatteries() {
        return totalBatteries;
    }

    public void setTotalBatteries(Long totalBatteries) {
        this.totalBatteries = totalBatteries;
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

    public void setAverageCapacity(Double averageCapacity) {
        this.averageCapacity = averageCapacity;
    }
}
