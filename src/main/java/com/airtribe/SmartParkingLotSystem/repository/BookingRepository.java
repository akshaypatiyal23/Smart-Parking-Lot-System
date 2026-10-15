package com.airtribe.SmartParkingLotSystem.repository;

import com.airtribe.SmartParkingLotSystem.entity.Booking;
import com.airtribe.SmartParkingLotSystem.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    boolean existsByVehicleNumberPlateAndStatus(String numberPlate, BookingStatus status);
    Optional<Booking> findByVehicleNumberPlateAndStatus(String numberPlate, BookingStatus status);
}
