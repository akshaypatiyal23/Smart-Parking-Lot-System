package com.airtribe.SmartParkingLotSystem.service;

import com.airtribe.SmartParkingLotSystem.entity.Booking;
import com.airtribe.SmartParkingLotSystem.entity.ParkingSpot;
import com.airtribe.SmartParkingLotSystem.entity.Receipt;
import com.airtribe.SmartParkingLotSystem.entity.Vehicle;
import com.airtribe.SmartParkingLotSystem.enums.BookingStatus;
import com.airtribe.SmartParkingLotSystem.enums.SpotStatus;
import com.airtribe.SmartParkingLotSystem.enums.SpotType;
import com.airtribe.SmartParkingLotSystem.enums.VehicleType;
import com.airtribe.SmartParkingLotSystem.exception.AlreadyParkedException;
import com.airtribe.SmartParkingLotSystem.exception.BookingNotFoundException;
import com.airtribe.SmartParkingLotSystem.exception.NoSpotAvailableException;
import com.airtribe.SmartParkingLotSystem.repository.BookingRepository;
import com.airtribe.SmartParkingLotSystem.repository.ParkingSpotRepository;
import com.airtribe.SmartParkingLotSystem.repository.ReceiptRepository;
import com.airtribe.SmartParkingLotSystem.repository.VehicleRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class BookingService {


    private final VehicleRepository vehicleRepository;
    private final ParkingSpotRepository spotRepository;
    private final BookingRepository bookingRepository;
    private final ReceiptRepository receiptRepository;
    private final FeeService feeService;

    public BookingService(VehicleRepository vehicleRepository,
                          ParkingSpotRepository spotRepository,
                          BookingRepository bookingRepository,
                          ReceiptRepository receiptRepository,
                          FeeService feeService) {
        this.vehicleRepository = vehicleRepository;
        this.spotRepository = spotRepository;
        this.bookingRepository = bookingRepository;
        this.receiptRepository = receiptRepository;
        this.feeService = feeService;
    }
    @Transactional
    public Booking bookParking(String numberPlate, VehicleType vehicleType) {
     if(bookingRepository.existsByVehicleNumberPlateAndStatus(numberPlate, BookingStatus.ACTIVE)) {
         throw new AlreadyParkedException("Vehicle with number plate "+ numberPlate + " already parked");
     }
        Vehicle vehicle = vehicleRepository.findByNumberPlate(numberPlate)
                .orElseGet(() -> vehicleRepository.save(new Vehicle(numberPlate, vehicleType)));

        ParkingSpot spot = findFreeSpot(vehicle.getVehicleType());

        spot.setSpotStatus(SpotStatus.OCCUPIED);

        return bookingRepository.save(new Booking(vehicle, spot, LocalDateTime.now()));





    }

    private static final Map<VehicleType, List<SpotType>> ALLOWED_SPOTS = Map.of(
            VehicleType.MOTORCYCLE, List.of(SpotType.SMALL, SpotType.MEDIUM),
            VehicleType.CAR,        List.of(SpotType.MEDIUM, SpotType.LARGE),
            VehicleType.HEAVY,        List.of(SpotType.LARGE)
    );

    private ParkingSpot findFreeSpot(VehicleType vehicleType) {
        for (SpotType spotType : ALLOWED_SPOTS.get(vehicleType)) {
            List<ParkingSpot> free = spotRepository.findFreeSpots(
                    spotType, SpotStatus.FREE, PageRequest.of(0, 1));
            if (!free.isEmpty()) {
                return free.get(0);
            }
        }
        throw new NoSpotAvailableException("No parking spot available for " + vehicleType);
    }

    @Transactional
    public Receipt checkOut(String numberPlate) {

        Booking booking = bookingRepository
                .findByVehicleNumberPlateAndStatus(numberPlate, BookingStatus.ACTIVE)
                .orElseThrow(() -> new BookingNotFoundException(
                        "No active booking for vehicle " + numberPlate));

        LocalDateTime exitTime = LocalDateTime.now();
        booking.setExitTime(exitTime);
        booking.setStatus(BookingStatus.CLOSED);

        // 3. Free the spot
        booking.getSpot().setSpotStatus(SpotStatus.FREE);

        Duration stay = Duration.between(booking.getEntryTime(), exitTime);
        BigDecimal fee = feeService.calculate(booking.getVehicle().getVehicleType(), stay);

        // 5. Create and save the receipt
        return receiptRepository.save(
                new Receipt(booking, stay.toMinutes(), fee, exitTime));
    }

    @Transactional(readOnly = true)
    public Map<SpotType, Long> availability() {
        Map<SpotType, Long> result = new EnumMap<>(SpotType.class);
        for (SpotType type : SpotType.values()) {
            result.put(type, spotRepository.countBySpotTypeAndSpotStatus(type, SpotStatus.FREE));
        }
        return result;
    }

}
