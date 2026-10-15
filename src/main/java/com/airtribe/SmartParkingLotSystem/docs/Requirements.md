# Requirements — Smart Parking Lot System

## 1. Purpose

The Smart Parking Lot System manages parking occupancy for a multi-floor parking facility. It supports vehicle check-in and check-out, eligible parking-spot allocation, availability reporting, fee calculation, and receipt generation.

## 2. Scope

The current implementation is a Spring Boot console application using Spring Data JPA and an H2 in-memory database. It is intended as a learning project and does not include authentication, a web UI, payment-gateway integration, or advance reservations.

## 3. Functional Requirements

### FR-01: Initialize parking data
- The system shall create a sample parking lot when no parking lot data exists.
- The sample lot shall contain two floors.
- Each floor shall contain two small, two medium, and one large spot.
- The seeder shall avoid creating the sample data again when a parking lot already exists.

### FR-02: Check in a vehicle
- The system shall accept a vehicle number plate and vehicle type.
- The system shall normalize the number plate to uppercase and remove surrounding whitespace in the console flow.
- The system shall reject a check-in when the vehicle already has an active booking.
- The system shall reuse an existing vehicle record with the same number plate, or create a new vehicle record.
- The system shall select a free parking spot compatible with the vehicle type.
- The system shall mark the selected spot as occupied and create an active booking.
- The system shall report an error if no compatible spot is available.

### FR-03: Vehicle and parking-spot compatibility
- A motorcycle may use a small or medium spot.
- A car may use a medium or large spot.
- A heavy vehicle may use a large spot only.
- The system shall prefer the first eligible spot type in the configured order.
- Spot selection shall consider only spots with `FREE` status.

### FR-04: Check out a vehicle
- The system shall locate the active booking for a vehicle by number plate.
- The system shall record the exit time and change the booking status to `CLOSED`.
- The system shall change the occupied spot status back to `FREE`.
- The system shall calculate the stay duration and parking fee.
- The system shall create and persist a receipt containing the booking, duration in minutes, amount, and payment/checkout timestamp.
- The system shall report an error when no active booking exists for the supplied number plate.

### FR-05: Calculate parking fees
- The fee shall be based on vehicle type and the number of started hours.
- Any stay shorter than or equal to one hour shall be charged for at least one hour.
- Partial hours beyond the first hour shall be rounded up.
- Rates shall be ₹40/hour for motorcycles, ₹70/hour for cars, and ₹120/hour for heavy vehicles.
- The calculated amount shall use `BigDecimal`.

### FR-06: Show availability
- The system shall report the number of free parking spots for each `SpotType`.
- Availability shall be calculated from persisted spot status.

### FR-07: Console interaction
- The console shall offer check-in, check-out, availability, and exit options.
- The console shall display a clear message when an operation fails.
- The console shall accept vehicle types through a menu choice and reject an unsupported choice.

## 4. Data Requirements

### Vehicle
- Unique generated ID
- Unique, non-null number plate
- Non-null vehicle type

### ParkingLot
- Unique generated ID
- Non-null name
- Collection of floors

### Floor
- Unique generated ID
- Non-null floor number
- Reference to its parking lot
- Collection of parking spots

### ParkingSpot
- Unique generated ID
- Non-null spot number
- Non-null spot type
- Non-null spot status
- Reference to its floor

### Booking
- Unique generated ID
- Non-null vehicle reference
- Non-null parking-spot reference
- Non-null entry time
- Optional exit time
- Non-null booking status

### Receipt
- Unique generated ID
- Required booking reference
- Unique booking reference for one receipt per booking
- Duration in minutes
- Amount
- Timestamp

## 5. Business Rules

1. A vehicle must not have more than one active booking at a time.
2. Only a free and compatible parking spot may be allocated.
3. A spot is marked occupied when a booking is created.
4. A spot is marked free when its active booking is checked out.
5. A booking is active at check-in and closed at checkout.
6. A receipt is associated with one booking.
7. Fees are rounded up to started hours with a one-hour minimum.
8. Vehicle number plates are treated as unique identifiers.

## 6. Non-Functional Requirements

- **Maintainability:** Keep entity, repository, service, exception, enum, and runner responsibilities separate.
- **Persistence:** Use JPA mappings to persist entities and relationships.
- **Consistency:** Booking, spot status, and receipt updates should be handled transactionally.
- **Usability:** Console prompts and errors should be understandable.
- **Testability:** Services should be testable independently of console I/O.
- **Reliability:** Duplicate active bookings and concurrent spot allocation should be protected against and covered by tests.
- **Portability:** The application should run on a system with a compatible JDK and Gradle dependencies.

## 7. Out of Scope

- User accounts and role-based access
- REST API and web/mobile frontend
- Online payment processing
- Advance reservations and cancellations
- Discounts, taxes, and promotional pricing
- Multiple independently managed parking lots through an admin interface
- Durable production database configuration and deployment

## 8. Acceptance Criteria

- The application starts and initializes sample parking data when the database is empty.
- A compatible vehicle can be checked in and assigned a free spot.
- A second active check-in for the same vehicle is rejected.
- An unavailable compatible spot produces a clear error.
- Checkout closes the active booking, releases its spot, calculates the fee, and saves a receipt.
- Checkout without an active booking produces a clear error.
- Availability reflects current spot status.
- Fee calculations are correct for each vehicle type and for partial-hour stays.
