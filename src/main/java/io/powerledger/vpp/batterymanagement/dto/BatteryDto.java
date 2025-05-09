package io.powerledger.vpp.batterymanagement.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import static io.powerledger.vpp.batterymanagement.util.Constant.AUSTRALIAN_POSTCODE_REGEX;

public class BatteryDto {

    @NotBlank(message = "Name cannot be null or empty.")
    private String name;

    @NotBlank(message = "Postcode cannot be empty.")
    @Pattern(regexp = AUSTRALIAN_POSTCODE_REGEX, message = "Invalid Australian postcode.")
    private String postcode;

    @NotNull(message = "Capacity cannot be null.")
    @Min(value = 1, message = "Capacity must be greater than 0.")
    private Integer capacity;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
}
