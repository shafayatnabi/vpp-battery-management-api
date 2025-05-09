package io.powerledger.vpp.batterymanagement.repository;

import io.powerledger.vpp.batterymanagement.model.Battery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface BatteryRepository extends JpaRepository<Battery, UUID> {

    @Query("SELECT b FROM Battery b WHERE b.postcode >= :minPostCode AND b.postcode <= :maxPostCode ORDER BY b.name ASC")
    List<Battery> findByPostcodeRangeOrderByName(
            @Param("minPostCode") String minPostCode,
            @Param("maxPostCode") String maxPostCode
    );

    @Query("SELECT b FROM Battery b WHERE "
            + "(:minPostCode IS NULL OR b.postcode >= :minPostCode) AND "
            + "(:maxPostCode IS NULL OR b.postcode <= :maxPostCode) AND "
            + "(:minCapacity IS NULL OR b.wattCapacity >= :minCapacity) AND "
            + "(:maxCapacity IS NULL OR b.wattCapacity <= :maxCapacity) "
            + "ORDER BY b.name ASC")
    List<Battery> findBySearchCriteria(
            @Param("minPostCode") String minPostCode,
            @Param("maxPostCode") String maxPostCode,
            @Param("minCapacity") Integer minCapacity,
            @Param("maxCapacity") Integer maxCapacity
    );
}
