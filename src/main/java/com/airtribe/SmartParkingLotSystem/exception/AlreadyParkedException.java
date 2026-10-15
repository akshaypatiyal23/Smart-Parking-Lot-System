package com.airtribe.SmartParkingLotSystem.exception;

import com.airtribe.SmartParkingLotSystem.entity.Vehicle;

public class AlreadyParkedException extends RuntimeException {
    public AlreadyParkedException(String message) {

        super(message);
    }
}
