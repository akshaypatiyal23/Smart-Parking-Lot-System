package com.airtribe.SmartParkingLotSystem.entity;

import com.airtribe.SmartParkingLotSystem.enums.SpotStatus;
import com.airtribe.SmartParkingLotSystem.enums.SpotType;
import jakarta.persistence.*;

@Entity
public class ParkingSpot {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long spotId;

    @Column(nullable = false)
    private String spotNumber;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SpotType spotType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SpotStatus spotStatus = SpotStatus.FREE;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Floor floor;

    protected ParkingSpot() {}

    public ParkingSpot(long spotId, String spotNumber, SpotType spotType, SpotStatus spotStatus, Floor floor) {
        this.spotId = spotId;
        this.spotNumber = spotNumber;
        this.spotType = spotType;
        this.spotStatus = spotStatus;
        this.floor = floor;
    }

    public ParkingSpot(String spotNumber, SpotType spotType) {
        this.spotNumber = spotNumber;
        this.spotType = spotType;
    }

    public long getSpotId() {
        return spotId;
    }

    public void setSpotId(long spotId) {
        this.spotId = spotId;
    }

    public String getSpotNumber() {
        return spotNumber;
    }

    public void setSpotNumber(String spotNumber) {
        this.spotNumber = spotNumber;
    }

    public SpotType getSpotType() {
        return spotType;
    }

    public void setSpotType(SpotType spotType) {
        this.spotType = spotType;
    }

    public SpotStatus getSpotStatus() {
        return spotStatus;
    }

    public void setSpotStatus(SpotStatus spotStatus) {
        this.spotStatus = spotStatus;
    }

    public Floor getFloor() {
        return floor;
    }

    public void setFloor(Floor floor) {
        this.floor = floor;
    }
}
