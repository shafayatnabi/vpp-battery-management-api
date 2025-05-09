package io.powerledger.vpp.batterymanagement.dto;

import jakarta.validation.constraints.Pattern;

import static io.powerledger.vpp.batterymanagement.util.Constant.AUSTRALIAN_POSTCODE_REGEX;

public class BatterySearchRequestDto {

    @Pattern(regexp = AUSTRALIAN_POSTCODE_REGEX, message = "Invalid Australian postcode.")
    private String minPostCode;

    @Pattern(regexp = AUSTRALIAN_POSTCODE_REGEX, message = "Invalid Australian postcode.")
    private String maxPostCode;

    private Integer minCapacity;

    private Integer maxCapacity;

    public String getMinPostCode() {
        return minPostCode;
    }

    public void setMinPostCode(String minPostCode) {
        this.minPostCode = minPostCode;
    }

    public String getMaxPostCode() {
        return maxPostCode;
    }

    public void setMaxPostCode(String maxPostCode) {
        this.maxPostCode = maxPostCode;
    }

    public Integer getMinCapacity() {
        return minCapacity;
    }

    public void setMinCapacity(Integer minCapacity) {
        this.minCapacity = minCapacity;
    }

    public Integer getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(Integer maxCapacity) {
        this.maxCapacity = maxCapacity;
    }
}
