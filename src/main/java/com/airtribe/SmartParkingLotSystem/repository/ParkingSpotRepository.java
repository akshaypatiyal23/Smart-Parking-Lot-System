package com.airtribe.SmartParkingLotSystem.repository;

import com.airtribe.SmartParkingLotSystem.entity.ParkingSpot;
import com.airtribe.SmartParkingLotSystem.enums.SpotStatus;
import com.airtribe.SmartParkingLotSystem.enums.SpotType;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ParkingSpotRepository extends JpaRepository<ParkingSpot, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT s FROM ParkingSpot s
        WHERE s.spotType = :type AND s.spotStatus = :status
        ORDER BY s.floor.floorNumber, s.spotNumber
        """)
    List<ParkingSpot> findFreeSpots(@Param("type") SpotType type,
                                    @Param("status") SpotStatus status,
                                    Pageable pageable);
    long countBySpotTypeAndSpotStatus(SpotType spotType, SpotStatus spotStatus);
}
