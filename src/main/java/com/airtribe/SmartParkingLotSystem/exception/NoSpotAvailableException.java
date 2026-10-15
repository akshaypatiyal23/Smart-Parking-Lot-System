package com.airtribe.SmartParkingLotSystem.exception;

import com.airtribe.SmartParkingLotSystem.enums.VehicleType;

public class NoSpotAvailableException extends RuntimeException {
    public NoSpotAvailableException(String message) {
        super(message);
    }
}
