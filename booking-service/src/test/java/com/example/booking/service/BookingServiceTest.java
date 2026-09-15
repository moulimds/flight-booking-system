package com.example.booking.service;

import com.example.booking.dto.message.BookingCancelledEvent;
import com.example.booking.dto.message.BookingCreatedEvent;
import com.example.booking.dto.message.FareValidationResponse;
import com.example.booking.dto.request.CancelBookingRequest;
import com.example.booking.dto.request.CreateBookingRequest;
import com.example.booking.dto.request.PassengerRequest;
import com.example.booking.dto.response.BookingCancellationResponse;
import com.example.booking.dto.response.BookingResponse;
import com.example.booking.entity.*;
import com.example.booking.exception.BookingNotFoundException;
import com.example.booking.exception.FareValidationException;
import com.example.booking.exception.InvalidBookingStateException;
import com.example.booking.mapper.BookingMapper;
import com.example.booking.messaging.BookingEventPublisher;
import com.example.booking.messaging.FareMessagePublisher;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.BookingStatusHistoryRepository;
import com.example.booking.service.impl.BookingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingStatusHistoryRepository historyRepository;

    @Mock
    private com.example.booking.repository.FlightSeatRepository flightSeatRepository;

    @Mock
    private PnrGenerator pnrGenerator;

    @Mock
    private BookingReferenceGenerator referenceGenerator;

    @Mock
    private FareMessagePublisher fareMessagePublisher;

    @Mock
    private BookingEventPublisher bookingEventPublisher;

    @Spy
    private BookingMapper bookingMapper = new BookingMapper();

    @InjectMocks
    private BookingServiceImpl bookingService;

    private CreateBookingRequest createRequest;
    private FareValidationResponse validFareResponse;
    private Booking testBooking;

    @BeforeEach
    void setUp() {
        List<PassengerRequest> passengerRequests = List.of(
                PassengerRequest.builder()
                        .firstName("John")
                        .lastName("Doe")
                        .dateOfBirth(LocalDate.of(1990, 5, 15))
                        .gender(Gender.MALE)
                        .passengerType(PassengerType.ADULT)
                        .build()
        );

        createRequest = CreateBookingRequest.builder()
                .userId(1001L)
                .flightId(101L)
                .fareId(501L)
                .passengers(passengerRequests)
                .build();

        validFareResponse = FareValidationResponse.builder()
                .requestId("REQ-123")
                .valid(true)
                .flightId(101L)
                .fareId(501L)
                .fareCode("ECONOMY-FLEX")
                .fareType(FareType.FLEXIBLE)
                .cabinClass(CabinClass.ECONOMY)
                .basePrice(BigDecimal.valueOf(5000))
                .taxAmount(BigDecimal.valueOf(900))
                .totalPrice(BigDecimal.valueOf(5900))
                .currency("INR")
                .availableSeats(10)
                .build();

        Passenger testPassenger = Passenger.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .gender(Gender.MALE)
                .passengerType(PassengerType.ADULT)
                .build();

        testBooking = Booking.builder()
                .id(10001L)
                .bookingReference("BK-20260913-A7K2")
                .pnr("A7K92P")
                .userId(1001L)
                .flightId(101L)
                .fareId(501L)
                .fareCode("ECONOMY-FLEX")
                .fareType(FareType.FLEXIBLE)
                .cabinClass(CabinClass.ECONOMY)
                .basePrice(BigDecimal.valueOf(5000))
                .taxAmount(BigDecimal.valueOf(900))
                .totalAmount(BigDecimal.valueOf(5900))
                .currency("INR")
                .bookingStatus(BookingStatus.PAYMENT_PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .passengers(new ArrayList<>(List.of(testPassenger)))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .version(0L)
                .build();
    }

    @Test
    @DisplayName("Should create booking successfully with RabbitMQ fare validation and price snapshot")
    void testCreateBooking_Success() {
        when(fareMessagePublisher.requestFareValidation(101L, 501L, 1)).thenReturn(validFareResponse);
        when(pnrGenerator.generatePnr()).thenReturn("A7K92P");
        when(referenceGenerator.generateReference()).thenReturn("BK-20260913-A7K2");
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);

        BookingResponse response = bookingService.createBooking(createRequest, "idemp-key-1");

        assertNotNull(response);
        assertEquals("A7K92P", response.getPnr());
        assertEquals("BK-20260913-A7K2", response.getBookingReference());
        assertEquals(BigDecimal.valueOf(5900), response.getTotalAmount());
        assertEquals(BookingStatus.PAYMENT_PENDING, response.getBookingStatus());

        verify(fareMessagePublisher).requestFareValidation(101L, 501L, 1);
        verify(bookingEventPublisher).publishBookingCreated(any(BookingCreatedEvent.class));
        verify(historyRepository, times(2)).save(any(BookingStatusHistory.class));
    }

    @Test
    @DisplayName("Should return existing booking if idempotency key exists")
    void testCreateBooking_Idempotent() {
        when(bookingRepository.findByIdempotencyKey("idemp-key-1")).thenReturn(Optional.of(testBooking));

        BookingResponse response = bookingService.createBooking(createRequest, "idemp-key-1");

        assertNotNull(response);
        assertEquals("A7K92P", response.getPnr());
        verifyNoInteractions(fareMessagePublisher);
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("Should throw FareValidationException when Fare Service rejects validation")
    void testCreateBooking_FareRejected() {
        FareValidationResponse invalidResponse = FareValidationResponse.builder()
                .requestId("REQ-123")
                .valid(false)
                .message("Insufficient seats available")
                .build();

        when(fareMessagePublisher.requestFareValidation(101L, 501L, 1)).thenReturn(invalidResponse);

        assertThrows(FareValidationException.class, () -> bookingService.createBooking(createRequest, null));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("Should retrieve booking by PNR")
    void testGetBookingByPnr_Success() {
        when(bookingRepository.findByPnr("A7K92P")).thenReturn(Optional.of(testBooking));

        BookingResponse response = bookingService.getBookingByPnr("a7k92p");

        assertNotNull(response);
        assertEquals("A7K92P", response.getPnr());
        assertEquals(1001L, response.getUserId());
    }

    @Test
    @DisplayName("Should cancel booking and calculate refund amount")
    void testCancelBooking_Success() {
        testBooking.setBookingStatus(BookingStatus.CONFIRMED);
        testBooking.setPaymentStatus(PaymentStatus.SUCCESS);

        when(bookingRepository.findById(10001L)).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);

        CancelBookingRequest cancelReq = CancelBookingRequest.builder().reason("Travel plan changed").build();
        BookingCancellationResponse cancelRes = bookingService.cancelBooking(10001L, cancelReq);

        assertNotNull(cancelRes);
        assertEquals(BookingStatus.CANCELLED, cancelRes.getStatus());
        assertEquals("Travel plan changed", cancelRes.getReason());
        assertEquals(BigDecimal.valueOf(1500), cancelRes.getCancellationFee());
        assertEquals(BigDecimal.valueOf(4400), cancelRes.getRefundAmount()); // 5900 - 1500

        verify(bookingEventPublisher).publishBookingCancelled(any(BookingCancelledEvent.class));
        verify(historyRepository).save(any(BookingStatusHistory.class));
    }

    @Test
    @DisplayName("Should throw InvalidBookingStateException when cancelling an already cancelled booking")
    void testCancelBooking_AlreadyCancelled() {
        testBooking.setBookingStatus(BookingStatus.CANCELLED);
        when(bookingRepository.findById(10001L)).thenReturn(Optional.of(testBooking));

        CancelBookingRequest cancelReq = CancelBookingRequest.builder().reason("Duplicate cancel").build();
        assertThrows(InvalidBookingStateException.class, () -> bookingService.cancelBooking(10001L, cancelReq));
    }

    @Test
    @DisplayName("Should hold booking successfully and set HELD status with expiration")
    void testHoldBooking_Success() {
        com.example.booking.dto.request.HoldBookingRequest holdRequest = com.example.booking.dto.request.HoldBookingRequest.builder()
                .userId(1001L)
                .flightId(101L)
                .fareId(501L)
                .holdDurationMinutes(20)
                .passengers(List.of(
                        PassengerRequest.builder()
                                .firstName("Jane")
                                .lastName("Doe")
                                .dateOfBirth(LocalDate.of(1995, 3, 10))
                                .gender(Gender.FEMALE)
                                .passengerType(PassengerType.ADULT)
                                .seatNumber("2A")
                                .build()
                ))
                .build();

        when(fareMessagePublisher.requestFareValidation(101L, 501L, 1)).thenReturn(validFareResponse);
        when(pnrGenerator.generatePnr()).thenReturn("HLD123");
        when(referenceGenerator.generateReference()).thenReturn("BK-HOLD-999");
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            b.setId(20002L);
            return b;
        });

        BookingResponse response = bookingService.holdBooking(holdRequest);

        assertNotNull(response);
        assertEquals(BookingStatus.HELD, response.getBookingStatus());
        assertNotNull(response.getHoldExpiresAt());
        verify(flightSeatRepository).save(any(FlightSeat.class));
        verify(bookingEventPublisher).publishBookingCreated(any(BookingCreatedEvent.class));
    }

    @Test
    @DisplayName("Should release booking hold and cancel reservation")
    void testReleaseBookingHold_Success() {
        Booking heldBooking = Booking.builder()
                .id(20002L)
                .pnr("HLD123")
                .bookingReference("BK-HOLD-999")
                .bookingStatus(BookingStatus.HELD)
                .totalAmount(BigDecimal.valueOf(5900))
                .currency("INR")
                .build();

        when(bookingRepository.findById(20002L)).thenReturn(Optional.of(heldBooking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingResponse response = bookingService.releaseBookingHold(20002L);

        assertNotNull(response);
        assertEquals(BookingStatus.CANCELLED, response.getBookingStatus());
        assertEquals("Hold released by customer", response.getCancellationReason());
        verify(flightSeatRepository).findByBookingId(20002L);
        verify(bookingEventPublisher).publishBookingCancelled(any(BookingCancelledEvent.class));
    }

    @Test
    @DisplayName("Should auto-generate 30 seats when fetching flight seats for empty flight")
    void testGetFlightSeats_AutoGenerate() {
        when(flightSeatRepository.findByFlightIdOrderBySeatNumberAsc(101L)).thenReturn(List.of());
        when(flightSeatRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<com.example.booking.dto.response.FlightSeatResponse> seats = bookingService.getFlightSeats(101L);

        assertEquals(30, seats.size());
        verify(flightSeatRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should admin block and unblock seats")
    void testAdminBlockAndUnblockSeats() {
        com.example.booking.dto.request.AdminBlockSeatsRequest blockReq = com.example.booking.dto.request.AdminBlockSeatsRequest.builder()
                .flightId(101L)
                .seatNumbers(List.of("1A", "1B"))
                .reason("VIP Block")
                .build();

        FlightSeat s1 = FlightSeat.builder().flightId(101L).seatNumber("1A").seatStatus(SeatStatus.AVAILABLE).build();
        FlightSeat s2 = FlightSeat.builder().flightId(101L).seatNumber("1B").seatStatus(SeatStatus.AVAILABLE).build();

        when(flightSeatRepository.findByFlightIdAndSeatNumber(101L, "1A")).thenReturn(Optional.of(s1));
        when(flightSeatRepository.findByFlightIdAndSeatNumber(101L, "1B")).thenReturn(Optional.of(s2));
        when(flightSeatRepository.save(any(FlightSeat.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<com.example.booking.dto.response.FlightSeatResponse> blocked = bookingService.adminBlockSeats(blockReq);
        assertEquals(2, blocked.size());
        assertEquals(SeatStatus.BLOCKED, blocked.get(0).getSeatStatus());
        assertEquals("VIP Block", blocked.get(0).getBlockedReason());

        // Now test unblock
        com.example.booking.dto.request.AdminUnblockSeatsRequest unblockReq = com.example.booking.dto.request.AdminUnblockSeatsRequest.builder()
                .flightId(101L)
                .seatNumbers(List.of("1A"))
                .build();

        s1.setSeatStatus(SeatStatus.BLOCKED);
        List<com.example.booking.dto.response.FlightSeatResponse> unblocked = bookingService.adminUnblockSeats(unblockReq);
        assertEquals(1, unblocked.size());
        assertEquals(SeatStatus.AVAILABLE, unblocked.get(0).getSeatStatus());
    }
}
