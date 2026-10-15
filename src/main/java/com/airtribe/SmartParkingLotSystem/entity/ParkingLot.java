package com.airtribe.SmartParkingLotSystem.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class ParkingLot {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long lotId;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "parkingLot", cascade = CascadeType.ALL)
    private List<Floor> floors = new ArrayList<>();

    protected ParkingLot() {}

    public ParkingLot(String name) {
        this.name = name;
    }

    public void addFloor(Floor floor) {
        floors.add(floor);
        floor.setParkingLot(this);
    }

    public long getLotId() { return lotId; }
    public void setLotId(long lotId) { this.lotId = lotId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Floor> getFloors() { return floors; }
    public void setFloors(List<Floor> floors) { this.floors = floors; }
}