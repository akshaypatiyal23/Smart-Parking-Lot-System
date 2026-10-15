package com.airtribe.SmartParkingLotSystem.repository;

import com.airtribe.SmartParkingLotSystem.entity.ParkingLot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParkingLotRepository extends JpaRepository<ParkingLot, Long> {
}
