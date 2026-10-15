package com.airtribe.SmartParkingLotSystem.repository;

import com.airtribe.SmartParkingLotSystem.entity.Vehicle;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
     Optional<Vehicle> findByNumberPlate(String numberPlate);
}
