# Smart Parking Lot System

A console-based parking management application built with **Java 17, Spring Boot, Spring Data JPA, Hibernate, and H2**. It manages parking-lot floors and spots, checks vehicles in and out, tracks active bookings, calculates parking fees, and generates receipts.

## Features

- **Vehicle check-in:** Registers a vehicle when needed, selects an eligible free spot, marks it occupied, and creates an active booking.
- **Vehicle check-out:** Finds the active booking by number plate, records the exit time, closes the booking, frees the spot, calculates the fee, and saves a receipt.
- **Availability:** Reports the number of free spots grouped by spot type.
- **Vehicle and spot rules:** Supports motorcycles, cars, and heavy vehicles, with spot eligibility rules.
- **Fee calculation:** Charges by started hour, with a minimum charge of one hour.
- **Persistence:** Uses Spring Data JPA and an H2 in-memory database.
- **Seed data:** Creates a sample parking lot with two floors and ten spots on the first startup when no lot exists.

## Technology Stack

- Java 17
- Spring Boot 4.1.1
- Spring Data JPA / Hibernate
- H2 Database
- Gradle
- JUnit 5 / Spring Boot test dependencies

## Project Structure

```text
src/main/java/com/airtribe/SmartParkingLotSystem/
├── SmartParkingLotSystemApplication.java
├── entity/
│   ├── Booking.java
│   ├── Floor.java
│   ├── ParkingLot.java
│   ├── ParkingSpot.java
│   ├── Receipt.java
│   └── Vehicle.java
├── enums/
│   ├── BookingStatus.java
│   ├── SpotStatus.java
│   ├── SpotType.java
│   └── VehicleType.java
├── exception/
│   ├── AlreadyParkedException.java
│   ├── BookingNotFoundException.java
│   └── NoSpotAvailableException.java
├── repository/
│   ├── BookingRepository.java
│   ├── FloorRepository.java
│   ├── ParkingLotRepository.java
│   ├── ParkingSpotRepository.java
│   ├── ReceiptRepository.java
│   └── VehicleRepository.java
├── runner/
│   ├── ConsoleMenuRunner.java
│   └── DataSeeder.java
└── service/
    ├── BookingService.java
    └── FeeService.java
```

## Prerequisites

- JDK 17 or later
- Internet access on the first build so Gradle can download dependencies

## Run the Application

From the project root:

```bash
./gradlew bootRun
```

On Windows:

```bat
gradlew.bat bootRun
```

The console menu offers:

1. Check in a vehicle
2. Check out a vehicle
3. View availability
4. Exit

The application uses an in-memory H2 database configured in `src/main/resources/application.properties`. Data is reset when the application stops.

## Parking Rules

### Vehicle-to-spot eligibility

| Vehicle type | Eligible spot types |
|---|---|
| `MOTORCYCLE` | `SMALL`, then `MEDIUM` |
| `CAR` | `MEDIUM`, then `LARGE` |
| `HEAVY` | `LARGE` only |

The service checks eligible spot types in the order shown and chooses the first free spot returned by the repository.

### Fee rates

| Vehicle type | Rate per started hour |
|---|---:|
| `MOTORCYCLE` | ₹40 |
| `CAR` | ₹70 |
| `HEAVY` | ₹120 |

A stay shorter than one hour is charged for one hour. Additional partial hours are rounded up to the next hour.

## Data Model

- A `ParkingLot` contains multiple `Floor` records.
- A `Floor` contains multiple `ParkingSpot` records.
- A `Vehicle` can have multiple historical `Booking` records.
- Each `Booking` references one vehicle and one parking spot.
- A closed booking can have one `Receipt`.

See [Class_Model.md](Class_Model.md) and [Object_Relationships.md](Object_Relationships.md) for details.

## Running Tests

```bash
./gradlew test
```

The project includes a Spring application context test and a `BookingServiceTest`. Add tests for fee boundaries, booking validation, spot allocation, checkout, receipt generation, and concurrency before treating the application as production-ready.

## Current Scope and Limitations

- This is a console application; REST controllers and a web UI are not implemented.
- H2 is configured in memory, so application data is not durable between runs.
- The sample seeder creates one parking lot with two floors and five spots per floor.
- Authentication, payments, reservations made in advance, refunds, and administrative management are out of scope.
- Concurrency and duplicate-active-booking behavior should be tested against the target database before production use.
