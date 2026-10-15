package com.airtribe.SmartParkingLotSystem;

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
import com.airtribe.SmartParkingLotSystem.service.BookingService;
import com.airtribe.SmartParkingLotSystem.service.FeeService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    private static final String PLATE = "HP01AB1234";
    private static final Pageable FIRST_ONLY = PageRequest.of(0, 1);

    @Mock private VehicleRepository vehicleRepository;
    @Mock private ParkingSpotRepository spotRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private ReceiptRepository receiptRepository;
    @Mock private FeeService feeService;

    @InjectMocks private BookingService bookingService;



    private static Vehicle vehicle(String plate, VehicleType type) {
        return new Vehicle(plate, type);
    }

    private static ParkingSpot spot(String number, SpotType type) {
        return new ParkingSpot(number, type);
    }



    @ParameterizedTest(name = "new {0} gets a {1} spot, marked OCCUPIED, booking ACTIVE")
    @CsvSource({
            "MOTORCYCLE, SMALL",
            "CAR,        MEDIUM",
            "HEAVY,      LARGE"
    })
    void bookParking_newVehicle_getsPreferredSpotAndActiveBooking(VehicleType type, SpotType expectedSpotType) {
        ParkingSpot freeSpot = spot("F1-X1", expectedSpotType);

        when(bookingRepository.existsByVehicleNumberPlateAndStatus(PLATE, BookingStatus.ACTIVE)).thenReturn(false);
        when(vehicleRepository.findByNumberPlate(PLATE)).thenReturn(Optional.empty());
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(returnsFirstArg());
        when(spotRepository.findFreeSpots(eq(expectedSpotType), eq(SpotStatus.FREE), eq(FIRST_ONLY)))
                .thenReturn(List.of(freeSpot));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(returnsFirstArg());

        LocalDateTime before = LocalDateTime.now();
        Booking booking = bookingService.bookParking(PLATE, type);
        LocalDateTime after = LocalDateTime.now();

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.ACTIVE);
        assertThat(booking.getExitTime()).isNull();
        assertThat(booking.getEntryTime()).isBetween(before, after);
        assertThat(booking.getSpot()).isSameAs(freeSpot);
        assertThat(freeSpot.getSpotStatus()).isEqualTo(SpotStatus.OCCUPIED);

        ArgumentCaptor<Vehicle> savedVehicle = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleRepository).save(savedVehicle.capture());
        assertThat(savedVehicle.getValue().getNumberPlate()).isEqualTo(PLATE);
        assertThat(savedVehicle.getValue().getVehicleType()).isEqualTo(type);

        verify(spotRepository).findFreeSpots(eq(expectedSpotType), eq(SpotStatus.FREE), eq(FIRST_ONLY));
        verifyNoMoreInteractions(spotRepository);
    }

    @ParameterizedTest(name = "{0}: no {1} spot free, falls back to {2}")
    @CsvSource({
            "MOTORCYCLE, SMALL,  MEDIUM",
            "CAR,        MEDIUM, LARGE"
    })
    void bookParking_preferredTypeFull_fallsBackToNextSpotType(VehicleType type,
                                                               SpotType preferred,
                                                               SpotType fallback) {
        ParkingSpot fallbackSpot = spot("F2-X1", fallback);

        when(bookingRepository.existsByVehicleNumberPlateAndStatus(PLATE, BookingStatus.ACTIVE)).thenReturn(false);
        when(vehicleRepository.findByNumberPlate(PLATE)).thenReturn(Optional.empty());
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(returnsFirstArg());
        when(spotRepository.findFreeSpots(eq(preferred), eq(SpotStatus.FREE), eq(FIRST_ONLY)))
                .thenReturn(Collections.emptyList());
        when(spotRepository.findFreeSpots(eq(fallback), eq(SpotStatus.FREE), eq(FIRST_ONLY)))
                .thenReturn(List.of(fallbackSpot));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(returnsFirstArg());

        Booking booking = bookingService.bookParking(PLATE, type);

        assertThat(booking.getSpot()).isSameAs(fallbackSpot);
        assertThat(fallbackSpot.getSpotStatus()).isEqualTo(SpotStatus.OCCUPIED);

        InOrder order = inOrder(spotRepository);
        order.verify(spotRepository).findFreeSpots(eq(preferred), eq(SpotStatus.FREE), eq(FIRST_ONLY));
        order.verify(spotRepository).findFreeSpots(eq(fallback), eq(SpotStatus.FREE), eq(FIRST_ONLY));
    }

    @ParameterizedTest(name = "{0}: lot full, {1} spot type(s) tried, NoSpotAvailableException")
    @CsvSource({
            "MOTORCYCLE, 2",
            "CAR,        2",
            "HEAVY,      1"
    })
    void bookParking_noSpotAvailable_throwsAndSavesNoBooking(VehicleType type, int expectedLookups) {
        when(bookingRepository.existsByVehicleNumberPlateAndStatus(PLATE, BookingStatus.ACTIVE)).thenReturn(false);
        when(vehicleRepository.findByNumberPlate(PLATE)).thenReturn(Optional.empty());
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(returnsFirstArg());
        when(spotRepository.findFreeSpots(any(SpotType.class), eq(SpotStatus.FREE), any(Pageable.class)))
                .thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> bookingService.bookParking(PLATE, type))
                .isInstanceOf(NoSpotAvailableException.class)
                .hasMessageContaining(type.name());

        verify(spotRepository, times(expectedLookups))
                .findFreeSpots(any(SpotType.class), eq(SpotStatus.FREE), any(Pageable.class));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @ParameterizedTest(name = "plate {0} already parked, AlreadyParkedException")
    @ValueSource(strings = {"HP01AB1234", "PB10XY9999", "DL3CAB0001"})
    void bookParking_vehicleAlreadyParked_throwsBeforeTouchingAnythingElse(String plate) {
        when(bookingRepository.existsByVehicleNumberPlateAndStatus(plate, BookingStatus.ACTIVE)).thenReturn(true);

        assertThatThrownBy(() -> bookingService.bookParking(plate, VehicleType.CAR))
                .isInstanceOf(AlreadyParkedException.class)
                .hasMessageContaining(plate);

        verifyNoInteractions(vehicleRepository, spotRepository);
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @ParameterizedTest(name = "known plate stored as {0}, requested as {1}: uses stored type, looks up {2}")
    @CsvSource({
            "CAR,        CAR,        MEDIUM",
            "CAR,        MOTORCYCLE, MEDIUM",
            "HEAVY,      CAR,        LARGE",
            "MOTORCYCLE, HEAVY,      SMALL"
    })
    void bookParking_knownVehicle_isReusedAndStoredTypeDecidesTheSpot(VehicleType storedType,
                                                                      VehicleType requestedType,
                                                                      SpotType expectedSpotType) {

        Vehicle existing = vehicle(PLATE, storedType);
        ParkingSpot freeSpot = spot("F1-X1", expectedSpotType);

        when(bookingRepository.existsByVehicleNumberPlateAndStatus(PLATE, BookingStatus.ACTIVE)).thenReturn(false);
        when(vehicleRepository.findByNumberPlate(PLATE)).thenReturn(Optional.of(existing));
        when(spotRepository.findFreeSpots(eq(expectedSpotType), eq(SpotStatus.FREE), eq(FIRST_ONLY)))
                .thenReturn(List.of(freeSpot));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(returnsFirstArg());

        Booking booking = bookingService.bookParking(PLATE, requestedType);

        assertThat(booking.getVehicle()).isSameAs(existing);
        assertThat(booking.getSpot()).isSameAs(freeSpot);
        verify(vehicleRepository, never()).save(any(Vehicle.class));   // no duplicate vehicle row
    }


    @ParameterizedTest(name = "{0} parked {1} min: booking CLOSED, spot FREE, receipt amount {2}")
    @CsvSource({
            "MOTORCYCLE, 0,   40",
            "MOTORCYCLE, 5,   40",
            "CAR,        60,  70",
            "HEAVY,      61,  240",
            "CAR,        125, 210"
    })
    void checkOut_closesBookingFreesSpotAndSavesReceipt(VehicleType type, long minutes, String fee) {
        Vehicle vehicle = vehicle(PLATE, type);
        ParkingSpot occupiedSpot = spot("F1-X1", SpotType.MEDIUM);
        occupiedSpot.setSpotStatus(SpotStatus.OCCUPIED);
        Booking booking = new Booking(vehicle, occupiedSpot, LocalDateTime.now().minusMinutes(minutes));
        BigDecimal expectedFee = new BigDecimal(fee);

        when(bookingRepository.findByVehicleNumberPlateAndStatus(PLATE, BookingStatus.ACTIVE))
                .thenReturn(Optional.of(booking));
        when(feeService.calculate(eq(type), any(Duration.class))).thenReturn(expectedFee);
        when(receiptRepository.save(any(Receipt.class))).thenAnswer(returnsFirstArg());

        LocalDateTime before = LocalDateTime.now();
        Receipt receipt = bookingService.checkOut(PLATE);
        LocalDateTime after = LocalDateTime.now();

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CLOSED);
        assertThat(booking.getExitTime()).isBetween(before, after);

        assertThat(occupiedSpot.getSpotStatus()).isEqualTo(SpotStatus.FREE);

        assertThat(receipt.getBooking()).isSameAs(booking);
        assertThat(receipt.getDurationMinutes()).isEqualTo(minutes);
        assertThat(receipt.getAmount()).isEqualByComparingTo(expectedFee);
        assertThat(receipt.getPaidAt()).isEqualTo(booking.getExitTime());

        ArgumentCaptor<Duration> stay = ArgumentCaptor.forClass(Duration.class);
        verify(feeService).calculate(eq(type), stay.capture());
        assertThat(stay.getValue().toMinutes()).isEqualTo(minutes);

        verify(receiptRepository, times(1)).save(any(Receipt.class));
    }

    @ParameterizedTest(name = "plate {0} has no ACTIVE booking, BookingNotFoundException")
    @ValueSource(strings = {"HP01AB1234", "UNKNOWN-PLATE", "hp01ab1234"})
    void checkOut_noActiveBooking_throwsAndCreatesNoReceipt(String plate) {
        when(bookingRepository.findByVehicleNumberPlateAndStatus(plate, BookingStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.checkOut(plate))
                .isInstanceOf(BookingNotFoundException.class)
                .hasMessageContaining(plate);

        verifyNoInteractions(feeService, receiptRepository);
    }



    @ParameterizedTest(name = "free spots SMALL={0}, MEDIUM={1}, LARGE={2}")
    @CsvSource({
            "4, 4, 2",
            "0, 0, 0",
            "1, 0, 7",
            "10, 3, 0"
    })
    void availability_returnsFreeCountForEverySpotType(long small, long medium, long large) {
        when(spotRepository.countBySpotTypeAndSpotStatus(SpotType.SMALL, SpotStatus.FREE)).thenReturn(small);
        when(spotRepository.countBySpotTypeAndSpotStatus(SpotType.MEDIUM, SpotStatus.FREE)).thenReturn(medium);
        when(spotRepository.countBySpotTypeAndSpotStatus(SpotType.LARGE, SpotStatus.FREE)).thenReturn(large);

        Map<SpotType, Long> result = bookingService.availability();

        assertThat(result).containsExactly(
                entry(SpotType.SMALL, small),
                entry(SpotType.MEDIUM, medium),
                entry(SpotType.LARGE, large));
    }
}