package com.airline.seat.controller;

import com.airline.seat.dto.*;
import com.airline.seat.entity.SeatClass;
import com.airline.seat.entity.SeatStatus;
import com.airline.seat.service.SeatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SeatController.class)
class SeatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SeatService seatService;

    private SeatResponse sampleSeatResponse() {
        SeatResponse res = new SeatResponse();
        res.setSeatId(1L);
        res.setFlightId("FL-101");
        res.setSeatNumber("1A");
        res.setSeatClass(SeatClass.FIRST);
        res.setIsPreferred(true);
        res.setBasePriceMultiplier(new BigDecimal("2.50"));
        res.setStatus(SeatStatus.HELD);
        res.setFlightScheduleId("SCH-101");
        res.setHeldByUserId("USER-1002");
        return res;
    }

    @Test
    @DisplayName("POST /api/v1/seats/hold - Success")
    void holdSeats_Endpoint_Success() throws Exception {
        SeatHoldRequest req = new SeatHoldRequest();
        req.setFlightScheduleId("SCH-101");
        req.setSeatIds(List.of(1L, 2L));
        req.setUserId("USER-1002");
        req.setHoldDurationMinutes(10);

        when(seatService.holdSeats(any(SeatHoldRequest.class))).thenReturn(List.of(sampleSeatResponse()));

        mockMvc.perform(post("/api/v1/seats/hold")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Seats successfully placed on hold"))
                .andExpect(jsonPath("$.data[0].seatNumber").value("1A"));
    }

    @Test
    @DisplayName("POST /api/v1/seats/reserve - Success")
    void reserveSeats_Endpoint_Success() throws Exception {
        SeatReserveRequest req = new SeatReserveRequest();
        req.setFlightScheduleId("SCH-101");
        req.setSeatIds(List.of(1L));
        req.setUserId("USER-1002");
        req.setBookingReference("BK-2026-8841");

        SeatResponse res = sampleSeatResponse();
        res.setStatus(SeatStatus.RESERVED);
        res.setBookingReference("BK-2026-8841");

        when(seatService.reserveSeats(any(SeatReserveRequest.class))).thenReturn(List.of(res));

        mockMvc.perform(post("/api/v1/seats/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].status").value("RESERVED"));
    }

    @Test
    @DisplayName("POST /api/v1/seats/release - Success")
    void releaseSeats_Endpoint_Success() throws Exception {
        SeatReleaseRequest req = new SeatReleaseRequest();
        req.setFlightScheduleId("SCH-101");
        req.setSeatIds(List.of(1L));
        req.setUserId("USER-1002");

        SeatResponse res = sampleSeatResponse();
        res.setStatus(SeatStatus.AVAILABLE);

        when(seatService.releaseSeats(any(SeatReleaseRequest.class))).thenReturn(List.of(res));

        mockMvc.perform(post("/api/v1/seats/release")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Seats successfully released back to available pool"));
    }

    @Test
    @DisplayName("POST /api/v1/seats/block - Success")
    void blockSeats_Endpoint_Success() throws Exception {
        SeatBlockRequest req = new SeatBlockRequest();
        req.setFlightScheduleId("SCH-101");
        req.setSeatIds(List.of(7L, 8L));
        req.setReason("Crew rest allocation");
        req.setBlockedBy("OPS-MANAGER");

        SeatResponse res = sampleSeatResponse();
        res.setStatus(SeatStatus.BLOCKED);

        when(seatService.blockSeats(any(SeatBlockRequest.class))).thenReturn(List.of(res));

        mockMvc.perform(post("/api/v1/seats/block")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Seats successfully blocked"));
    }

    @Test
    @DisplayName("POST /api/v1/seats/unblock - Success")
    void unblockSeats_Endpoint_Success() throws Exception {
        SeatUnblockRequest req = new SeatUnblockRequest();
        req.setFlightScheduleId("SCH-101");
        req.setSeatIds(List.of(7L, 8L));

        SeatResponse res = sampleSeatResponse();
        res.setStatus(SeatStatus.AVAILABLE);

        when(seatService.unblockSeats(any(SeatUnblockRequest.class))).thenReturn(List.of(res));

        mockMvc.perform(post("/api/v1/seats/unblock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Seats successfully unblocked"));
    }

    @Test
    @DisplayName("POST /api/v1/seats/change - Success")
    void changeSeat_Endpoint_Success() throws Exception {
        SeatChangeRequest req = new SeatChangeRequest();
        req.setFlightScheduleId("SCH-101");
        req.setCurrentSeatId(4L);
        req.setNewSeatId(6L);
        req.setUserId("USER-1002");

        SeatResponse res = sampleSeatResponse();
        res.setSeatNumber("2D");

        when(seatService.changeSeat(any(SeatChangeRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/v1/seats/change")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Seat assignment successfully changed"));
    }

    @Test
    @DisplayName("GET /api/v1/seats/available-count - Success")
    void getAvailableSeatsCount_Endpoint_Success() throws Exception {
        AvailableSeatsCountResponse countResponse = new AvailableSeatsCountResponse(
                "SCH-101", 44, 38, 2, 2, 2,
                Map.of(SeatClass.FIRST, 4L, SeatClass.BUSINESS, 12L, SeatClass.ECONOMY, 28L)
        );

        when(seatService.getAvailableSeatsCount("SCH-101")).thenReturn(countResponse);

        mockMvc.perform(get("/api/v1/seats/available-count")
                        .param("flightScheduleId", "SCH-101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalSeats").value(44))
                .andExpect(jsonPath("$.data.availableSeats").value(38));
    }

    @Test
    @DisplayName("GET /api/v1/seats/flight/{flightId} - Success")
    void getFlightSeatMap_Endpoint_Success() throws Exception {
        FlightSeatMapResponse mapResponse = new FlightSeatMapResponse(
                "FL-101", "SCH-101", 1, List.of(sampleSeatResponse())
        );

        when(seatService.getFlightSeatMap(eq("FL-101"), eq("SCH-101"))).thenReturn(mapResponse);

        mockMvc.perform(get("/api/v1/seats/flight/FL-101")
                        .param("flightScheduleId", "SCH-101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.flightId").value("FL-101"))
                .andExpect(jsonPath("$.data.seats[0].seatNumber").value("1A"));
    }
}
