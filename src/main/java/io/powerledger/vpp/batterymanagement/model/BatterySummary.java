package io.powerledger.vpp.batterymanagement.model;

public class BatterySummary {
    private Long count;
    private Long totalWattCapacity;
    private Double averageWattCapacity;

    public BatterySummary(Long count, Long totalWattCapacity, Double averageWattCapacity) {
        this.count = count;
        this.totalWattCapacity = totalWattCapacity;
        this.averageWattCapacity = averageWattCapacity;
    }

    public Long getCount() {
        return count;
    }

    public Long getTotalWattCapacity() {
        return totalWattCapacity;
    }

    public Double getAverageWattCapacity() {
        return averageWattCapacity;
    }
}
