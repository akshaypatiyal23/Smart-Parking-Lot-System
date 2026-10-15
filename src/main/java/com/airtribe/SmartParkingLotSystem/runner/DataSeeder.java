package com.airtribe.SmartParkingLotSystem.runner;


import com.airtribe.SmartParkingLotSystem.entity.Floor;
import com.airtribe.SmartParkingLotSystem.entity.ParkingLot;
import com.airtribe.SmartParkingLotSystem.entity.ParkingSpot;
import com.airtribe.SmartParkingLotSystem.enums.SpotType;
import com.airtribe.SmartParkingLotSystem.repository.ParkingLotRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class DataSeeder implements CommandLineRunner {

    private final ParkingLotRepository lotRepository;

    public DataSeeder(ParkingLotRepository lotRepository) {
        this.lotRepository = lotRepository;
    }

    @Override
    public void run(String... args) {
        if (lotRepository.count() > 0) return;          // don't seed twice

        ParkingLot lot = new ParkingLot("Luxury Parking");

        for (int f = 1; f <= 2; f++) {
            Floor floor = new Floor(f);
            lot.addFloor(floor);

            floor.addSpot(new ParkingSpot("F" + f + "-S1", SpotType.SMALL));
            floor.addSpot(new ParkingSpot("F" + f + "-S2", SpotType.SMALL));
            floor.addSpot(new ParkingSpot("F" + f + "-M1", SpotType.MEDIUM));
            floor.addSpot(new ParkingSpot("F" + f + "-M2", SpotType.MEDIUM));
            floor.addSpot(new ParkingSpot("F" + f + "-L1", SpotType.LARGE));
        }

        lotRepository.save(lot);
    }
}