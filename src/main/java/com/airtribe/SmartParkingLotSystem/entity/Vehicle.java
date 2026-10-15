package com.airtribe.SmartParkingLotSystem.entity;

import com.airtribe.SmartParkingLotSystem.enums.VehicleType;
import jakarta.persistence.*;

@Entity
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long vehicleId;

    @Column(nullable = false,  unique = true)
    private String numberPlate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    VehicleType vehicleType;

     public Vehicle(long vehicleId, String numberPlate, VehicleType vehicleType) {
        this.vehicleId = vehicleId;
        this.numberPlate = numberPlate;
        this.vehicleType = vehicleType;
     }
     public Vehicle(){}

    public Vehicle(String numberPlate, VehicleType vehicleType) {
        this.numberPlate = numberPlate;
        this.vehicleType = vehicleType;
    }


    public long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getNumberPlate() {
        return numberPlate;
    }

    public void setNumberPlate(String numberPlate) {
        this.numberPlate = numberPlate;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }
}
