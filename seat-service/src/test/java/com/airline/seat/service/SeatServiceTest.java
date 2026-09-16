package com.airline.seat.service;

import com.airline.seat.dto.*;
import com.airline.seat.entity.Seat;
import com.airline.seat.entity.SeatClass;
import com.airline.seat.entity.SeatInventory;
import com.airline.seat.entity.SeatStatus;
import com.airline.seat.exception.InvalidSeatOperationException;
import com.airline.seat.exception.ResourceNotFoundException;
import com.airline.seat.exception.SeatHoldExpiredException;
import com.airline.seat.exception.SeatUnavailableException;
import com.airline.seat.repository.SeatInventoryRepository;
import com.airline.seat.repository.SeatRepository;
import com.airline.seat.service.impl.SeatServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeatServiceTest {

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private SeatInventoryRepository inventoryRepository;

    @InjectMocks
    private SeatServiceImpl seatService;

    private Seat seat1;
    private Seat seat2;
    private SeatInventory inventory1;

    @BeforeEach
    void setUp() {
        seat1 = new Seat("FL-101", "1A", SeatClass.FIRST, true, new BigDecimal("2.50"));
        seat1.setId(1L);

        seat2 = new Seat("FL-101", "1D", SeatClass.FIRST, true, new BigDecimal("2.50"));
        seat2.setId(2L);

        inventory1 = new SeatInventory(seat1, "SCH-101", SeatStatus.AVAILABLE);
        inventory1.setId(10L);
    }

    @Test
    @DisplayName("Hold Seats - Success with valid available seats")
    void holdSeats_Success() {
        SeatHoldRequest req = new SeatHoldRequest();
        req.setFlightScheduleId("SCH-101");
        req.setSeatIds(List.of(1L));
        req.setUserId("USER-1002");
        req.setHoldDurationMinutes(10);

        when(seatRepository.findById(1L)).thenReturn(Optional.of(seat1));
        when(inventoryRepository.findByFlightScheduleIdAndSeatId("SCH-101", 1L))
                .thenReturn(Optional.of(inventory1));
        when(inventoryRepository.save(any(SeatInventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<SeatResponse> responses = seatService.holdSeats(req);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("1A", responses.get(0).getSeatNumber());
        assertEquals(SeatStatus.HELD, responses.get(0).getStatus());
        assertEquals("USER-1002", responses.get(0).getHeldByUserId());
        assertNotNull(responses.get(0).getHoldExpiresAt());
    }

    @Test
    @DisplayName("Hold Seats - Fail when seat already held by another user")
    void holdSeats_AlreadyHeldByOtherUser_ThrowsException() {
        inventory1.setStatus(SeatStatus.HELD);
        inventory1.setHeldByUserId("USER-9999");
        inventory1.setHoldExpiresAt(LocalDateTime.now().plusMinutes(5));

        SeatHoldRequest req = new SeatHoldRequest();
        req.setFlightScheduleId("SCH-101");
        req.setSeatIds(List.of(1L));
        req.setUserId("USER-1002");

        when(seatRepository.findById(1L)).thenReturn(Optional.of(seat1));
        when(inventoryRepository.findByFlightScheduleIdAndSeatId("SCH-101", 1L))
                .thenReturn(Optional.of(inventory1));

        assertThrows(SeatUnavailableException.class, () -> seatService.holdSeats(req));
    }

    @Test
    @DisplayName("Hold Seats - Auto-reclaim expired hold")
    void holdSeats_ExpiredHold_Success() {
        inventory1.setStatus(SeatStatus.HELD);
        inventory1.setHeldByUserId("USER-9999");
        inventory1.setHoldExpiresAt(LocalDateTime.now().minusMinutes(2)); // expired

        SeatHoldRequest req = new SeatHoldRequest();
        req.setFlightScheduleId("SCH-101");
        req.setSeatIds(List.of(1L));
        req.setUserId("USER-1002");
        req.setHoldDurationMinutes(15);

        when(seatRepository.findById(1L)).thenReturn(Optional.of(seat1));
        when(inventoryRepository.findByFlightScheduleIdAndSeatId("SCH-101", 1L))
                .thenReturn(Optional.of(inventory1));
        when(inventoryRepository.save(any(SeatInventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<SeatResponse> responses = seatService.holdSeats(req);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("USER-1002", responses.get(0).getHeldByUserId());
        assertEquals(SeatStatus.HELD, responses.get(0).getStatus());
    }

    @Test
    @DisplayName("Reserve Seats - Success from held state")
    void reserveSeats_FromHeld_Success() {
        inventory1.setStatus(SeatStatus.HELD);
        inventory1.setHeldByUserId("USER-1002");
        inventory1.setHoldExpiresAt(LocalDateTime.now().plusMinutes(5));

        SeatReserveRequest req = new SeatReserveRequest();
        req.setFlightScheduleId("SCH-101");
        req.setSeatIds(List.of(1L));
        req.setUserId("USER-1002");
        req.setBookingReference("BK-2026-8841");

        when(seatRepository.findById(1L)).thenReturn(Optional.of(seat1));
        when(inventoryRepository.findByFlightScheduleIdAndSeatId("SCH-101", 1L))
                .thenReturn(Optional.of(inventory1));
        when(inventoryRepository.save(any(SeatInventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<SeatResponse> responses = seatService.reserveSeats(req);

        assertEquals(1, responses.size());
        assertEquals(SeatStatus.RESERVED, responses.get(0).getStatus());
        assertEquals("BK-2026-8841", responses.get(0).getBookingReference());
        assertNull(responses.get(0).getHeldByUserId());
    }

    @Test
    @DisplayName("Reserve Seats - Fail when hold is expired")
    void reserveSeats_HoldExpired_ThrowsException() {
        inventory1.setStatus(SeatStatus.HELD);
        inventory1.setHeldByUserId("USER-1002");
        inventory1.setHoldExpiresAt(LocalDateTime.now().minusSeconds(10));

        SeatReserveRequest req = new SeatReserveRequest();
        req.setFlightScheduleId("SCH-101");
        req.setSeatIds(List.of(1L));
        req.setUserId("USER-1002");
        req.setBookingReference("BK-2026-8841");

        when(seatRepository.findById(1L)).thenReturn(Optional.of(seat1));
        when(inventoryRepository.findByFlightScheduleIdAndSeatId("SCH-101", 1L))
                .thenReturn(Optional.of(inventory1));

        assertThrows(SeatHoldExpiredException.class, () -> seatService.reserveSeats(req));
    }

    @Test
    @DisplayName("Release Seats - Success when user holds the seat")
    void releaseSeats_Success() {
        inventory1.setStatus(SeatStatus.HELD);
        inventory1.setHeldByUserId("USER-1002");
        inventory1.setHoldExpiresAt(LocalDateTime.now().plusMinutes(5));

        SeatReleaseRequest req = new SeatReleaseRequest();
        req.setFlightScheduleId("SCH-101");
        req.setSeatIds(List.of(1L));
        req.setUserId("USER-1002");

        when(seatRepository.findById(1L)).thenReturn(Optional.of(seat1));
        when(inventoryRepository.findByFlightScheduleIdAndSeatId("SCH-101", 1L))
                .thenReturn(Optional.of(inventory1));
        when(inventoryRepository.save(any(SeatInventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<SeatResponse> responses = seatService.releaseSeats(req);

        assertEquals(1, responses.size());
        assertEquals(SeatStatus.AVAILABLE, responses.get(0).getStatus());
        assertNull(responses.get(0).getHeldByUserId());
    }

    @Test
    @DisplayName("Release Seats - Fail when attempting to release someone else's held seat")
    void releaseSeats_OtherUser_ThrowsException() {
        inventory1.setStatus(SeatStatus.HELD);
        inventory1.setHeldByUserId("USER-9999");

        SeatReleaseRequest req = new SeatReleaseRequest();
        req.setFlightScheduleId("SCH-101");
        req.setSeatIds(List.of(1L));
        req.setUserId("USER-1002");

        when(seatRepository.findById(1L)).thenReturn(Optional.of(seat1));
        when(inventoryRepository.findByFlightScheduleIdAndSeatId("SCH-101", 1L))
                .thenReturn(Optional.of(inventory1));

        assertThrows(InvalidSeatOperationException.class, () -> seatService.releaseSeats(req));
    }

    @Test
    @DisplayName("Block Seats - Success for operations")
    void blockSeats_Success() {
        SeatBlockRequest req = new SeatBlockRequest();
        req.setFlightScheduleId("SCH-101");
        req.setSeatIds(List.of(1L));
        req.setReason("Crew rest allocation");
        req.setBlockedBy("OPS-LEAD");

        when(seatRepository.findById(1L)).thenReturn(Optional.of(seat1));
        when(inventoryRepository.findByFlightScheduleIdAndSeatId("SCH-101", 1L))
                .thenReturn(Optional.of(inventory1));
        when(inventoryRepository.save(any(SeatInventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<SeatResponse> responses = seatService.blockSeats(req);

        assertEquals(1, responses.size());
        assertEquals(SeatStatus.BLOCKED, responses.get(0).getStatus());
        assertEquals(SeatStatus.BLOCKED, inventory1.getStatus());
        assertEquals("Crew rest allocation", inventory1.getBlockedReason());
    }

    @Test
    @DisplayName("Block Seats - Fail if seat is already reserved")
    void blockSeats_AlreadyReserved_ThrowsException() {
        inventory1.setStatus(SeatStatus.RESERVED);
        inventory1.setBookingReference("BK-1234");

        SeatBlockRequest req = new SeatBlockRequest();
        req.setFlightScheduleId("SCH-101");
        req.setSeatIds(List.of(1L));
        req.setReason("Maintenance");

        when(seatRepository.findById(1L)).thenReturn(Optional.of(seat1));
        when(inventoryRepository.findByFlightScheduleIdAndSeatId("SCH-101", 1L))
                .thenReturn(Optional.of(inventory1));

        assertThrows(InvalidSeatOperationException.class, () -> seatService.blockSeats(req));
    }

    @Test
    @DisplayName("Unblock Seats - Success")
    void unblockSeats_Success() {
        inventory1.setStatus(SeatStatus.BLOCKED);
        inventory1.setBlockedReason("Maintenance");

        SeatUnblockRequest req = new SeatUnblockRequest();
        req.setFlightScheduleId("SCH-101");
        req.setSeatIds(List.of(1L));

        when(seatRepository.findById(1L)).thenReturn(Optional.of(seat1));
        when(inventoryRepository.findByFlightScheduleIdAndSeatId("SCH-101", 1L))
                .thenReturn(Optional.of(inventory1));
        when(inventoryRepository.save(any(SeatInventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<SeatResponse> responses = seatService.unblockSeats(req);

        assertEquals(1, responses.size());
        assertEquals(SeatStatus.AVAILABLE, responses.get(0).getStatus());
        assertNull(inventory1.getBlockedReason());
    }

    @Test
    @DisplayName("Change Seat - Atomic transfer to new available seat")
    void changeSeat_Success() {
        inventory1.setStatus(SeatStatus.RESERVED);
        inventory1.setBookingReference("BK-2026-8841");

        SeatInventory inventory2 = new SeatInventory(seat2, "SCH-101", SeatStatus.AVAILABLE);
        inventory2.setId(11L);

        SeatChangeRequest req = new SeatChangeRequest();
        req.setFlightScheduleId("SCH-101");
        req.setCurrentSeatId(1L);
        req.setNewSeatId(2L);
        req.setUserId("USER-1002");

        when(seatRepository.findById(1L)).thenReturn(Optional.of(seat1));
        when(seatRepository.findById(2L)).thenReturn(Optional.of(seat2));
        when(inventoryRepository.findByFlightScheduleIdAndSeatId("SCH-101", 1L)).thenReturn(Optional.of(inventory1));
        when(inventoryRepository.findByFlightScheduleIdAndSeatId("SCH-101", 2L)).thenReturn(Optional.of(inventory2));
        when(inventoryRepository.save(any(SeatInventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SeatResponse response = seatService.changeSeat(req);

        assertNotNull(response);
        assertEquals("1D", response.getSeatNumber());
        assertEquals(SeatStatus.RESERVED, response.getStatus());
        assertEquals("BK-2026-8841", response.getBookingReference());
        assertEquals(SeatStatus.AVAILABLE, inventory1.getStatus());
    }

    @Test
    @DisplayName("Change Seat - Fail if source and destination are identical")
    void changeSeat_SameSeat_ThrowsException() {
        SeatChangeRequest req = new SeatChangeRequest();
        req.setFlightScheduleId("SCH-101");
        req.setCurrentSeatId(1L);
        req.setNewSeatId(1L);
        req.setUserId("USER-1002");

        assertThrows(InvalidSeatOperationException.class, () -> seatService.changeSeat(req));
    }

    @Test
    @DisplayName("Get Available Seats Count - Accurate tally and class breakdown")
    void getAvailableSeatsCount_Success() {
        SeatInventory inv1 = new SeatInventory(seat1, "SCH-101", SeatStatus.AVAILABLE);
        SeatInventory inv2 = new SeatInventory(seat2, "SCH-101", SeatStatus.RESERVED);

        when(seatRepository.findAll()).thenReturn(List.of(seat1, seat2));
        when(inventoryRepository.findByFlightScheduleId("SCH-101")).thenReturn(List.of(inv1, inv2));

        AvailableSeatsCountResponse countResponse = seatService.getAvailableSeatsCount("SCH-101");

        assertNotNull(countResponse);
        assertEquals(2, countResponse.getTotalSeats());
        assertEquals(1, countResponse.getAvailableSeats());
        assertEquals(1, countResponse.getReservedSeats());
        assertEquals(0, countResponse.getHeldSeats());
        assertEquals(0, countResponse.getBlockedSeats());
    }

    @Test
    @DisplayName("Get Flight Seat Map - Returns layout and status")
    void getFlightSeatMap_Success() {
        when(seatRepository.findByFlightId("FL-101")).thenReturn(List.of(seat1, seat2));
        when(inventoryRepository.findByFlightScheduleId("SCH-101")).thenReturn(List.of(inventory1));

        FlightSeatMapResponse mapResponse = seatService.getFlightSeatMap("FL-101", "SCH-101");

        assertNotNull(mapResponse);
        assertEquals("FL-101", mapResponse.getFlightId());
        assertEquals(2, mapResponse.getTotalSeats());
        assertEquals(2, mapResponse.getSeats().size());
    }

    @Test
    @DisplayName("Scheduled Task - Release expired holds")
    void releaseExpiredHolds_Success() {
        SeatInventory expiredInv = new SeatInventory(seat1, "SCH-101", SeatStatus.HELD);
        expiredInv.setHeldByUserId("USER-9999");
        expiredInv.setHoldExpiresAt(LocalDateTime.now().minusMinutes(5));

        when(inventoryRepository.findByStatusAndHoldExpiresAtBefore(eq(SeatStatus.HELD), any(LocalDateTime.class)))
                .thenReturn(List.of(expiredInv));

        seatService.releaseExpiredHolds();

        assertEquals(SeatStatus.AVAILABLE, expiredInv.getStatus());
        assertNull(expiredInv.getHeldByUserId());
        assertNull(expiredInv.getHoldExpiresAt());
        verify(inventoryRepository, times(1)).saveAll(anyList());
    }
}
