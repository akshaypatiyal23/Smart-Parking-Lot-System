package com.airtribe.SmartParkingLotSystem.runner;

import com.airtribe.SmartParkingLotSystem.entity.*;
import com.airtribe.SmartParkingLotSystem.enums.VehicleType;
import com.airtribe.SmartParkingLotSystem.repository.ParkingLotRepository;
import com.airtribe.SmartParkingLotSystem.service.BookingService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
@Order(2)
public class ConsoleMenuRunner implements CommandLineRunner {

    private final BookingService bookingService;
    private final ConfigurableApplicationContext context;

    public ConsoleMenuRunner(BookingService bookingService, ConfigurableApplicationContext context) {
        this.bookingService = bookingService;
        this.context = context;
    }

    @Override
    public void run(String... args) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> checkIn(scanner);
                    case "2" -> checkOut(scanner);
                    case "3" -> showAvailability();
                    case "4" -> running = false;
                    default  -> System.out.println("Invalid choice, please enter 1-4.");
                }
            } catch (RuntimeException e) {
                // exceptions bubble up from the service; the menu shows them and keeps going
                System.out.println("Error: " + e.getMessage());
            }
        }

        System.out.println("Goodbye!");
        System.exit(SpringApplication.exit(context, () -> 0));    // shut the app down cleanly
    }

    private void printMenu() {
        System.out.println("""

                ===== SMART PARKING LOT =====
                1. Check in a vehicle
                2. Check out a vehicle
                3. View availability
                4. Exit
                Enter choice:""");
    }

    private void checkIn(Scanner scanner) {
        System.out.print("Number plate: ");
        String plate = scanner.nextLine().trim().toUpperCase();

        VehicleType type = readVehicleType(scanner);
        if (type == null) return;

        Booking booking = bookingService.bookParking(plate, type);
        ParkingSpot spot = booking.getSpot();

        System.out.println("Checked in. Spot: " + spot.getSpotNumber()
                + " (" + spot.getSpotType() + ")"
                + ", entry time: " + booking.getEntryTime());
    }

    private void checkOut(Scanner scanner) {
        System.out.print("Number plate: ");
        String plate = scanner.nextLine().trim().toUpperCase();

        Receipt receipt = bookingService.checkOut(plate);

        System.out.println("----- RECEIPT -----");
        System.out.println("Vehicle : " + plate);
        System.out.println("Duration: " + receipt.getDurationMinutes() + " minutes");
        System.out.println("Amount  : Rs. " + receipt.getAmount());
    }

    private void showAvailability() {
        System.out.println("Free spots:");
        bookingService.availability().forEach((type, count) ->
                System.out.println("  " + type + ": " + count));
    }

    private VehicleType readVehicleType(Scanner scanner) {
        System.out.println("Vehicle type: 1. Motorcycle  2. Car  3. Heavy");
        String input = scanner.nextLine().trim();
        return switch (input) {
            case "1" -> VehicleType.MOTORCYCLE;
            case "2" -> VehicleType.CAR;
            case "3" -> VehicleType.HEAVY;
            default -> {
                System.out.println("Invalid vehicle type.");
                yield null;
            }
        };
    }




}