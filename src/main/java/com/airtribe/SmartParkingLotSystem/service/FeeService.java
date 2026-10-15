package com.airtribe.SmartParkingLotSystem.service;

import com.airtribe.SmartParkingLotSystem.enums.BookingStatus;
import com.airtribe.SmartParkingLotSystem.enums.VehicleType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
@Service
public class FeeService {

    public BigDecimal calculate(VehicleType vehicleType, Duration stay) {

        long hours = Math.max(1, (long) Math.ceil(stay.toMinutes() / 60.0));

        BigDecimal rate = switch (vehicleType) {
            case MOTORCYCLE -> BigDecimal.valueOf(40);
            case CAR        -> BigDecimal.valueOf(70);
            case HEAVY      -> BigDecimal.valueOf(120);
        };

        return rate.multiply(BigDecimal.valueOf(hours));

    }
}
