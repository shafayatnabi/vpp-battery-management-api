package io.powerledger.vpp.batterymanagement.dto;

import java.util.List;

public class BatteryResponseDto {
    private List<String> batteries;
    private int totalCapacity;
    private double averageCapacity;

    public List<String> getBatteries() {
        return batteries;
    }

    public void setBatteries(List<String> batteries) {
        this.batteries = batteries;
    }

    public int getTotalCapacity() {
        return totalCapacity;
    }

    public void setTotalCapacity(int totalCapacity) {
        this.totalCapacity = totalCapacity;
    }

    public double getAverageCapacity() {
        return averageCapacity;
    }

    public void setAverageCapacity(double averageCapacity) {
        this.averageCapacity = averageCapacity;
    }
}
