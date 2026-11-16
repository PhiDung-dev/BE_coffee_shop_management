package com.example.cf_shop_management.controller;

import com.example.cf_shop_management.dto.request.ReservationCreateRequest;
import com.example.cf_shop_management.dto.request.ReservationUpdateRequest;
import com.example.cf_shop_management.dto.response.ReservationResponse;
import com.example.cf_shop_management.dto.response.TablesResponse;
import com.example.cf_shop_management.exception.AppException;
import com.example.cf_shop_management.exception.ErrorCode;
import com.example.cf_shop_management.service.ReservationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservationController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationService reservationService;

    private ReservationResponse reservationResponse;

    @BeforeEach
    void setUp() {
        TablesResponse table = TablesResponse.builder()
                .id(2L)
                .tableNumber("B02")
                .capacity(4)
                .status("RESERVED")
                .areaId(1L)
                .areaName("Tầng 1")
                .build();

        reservationResponse = ReservationResponse.builder()
                .id(1L)
                .guestCount(4)
                .note("Bàn gần cửa sổ")
                .reservationTime(LocalDateTime.of(2026, 11, 20, 19, 0))
                .status("PENDING")
                .accountId(5L)
                .accountEmail("customer@gmail.com")
                .accountPhone("0905111222")
                .tables(List.of(table))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createReservation_Success() throws Exception {
        ReservationCreateRequest request = ReservationCreateRequest.builder()
                .guestCount(4)
                .note("Bàn gần cửa sổ")
                .reservationTime(LocalDateTime.of(2026, 11, 20, 19, 0))
                .accountId(5L)
                .tableIds(List.of(2L))
                .build();

        when(reservationService.createReservation(any(ReservationCreateRequest.class))).thenReturn(reservationResponse);

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(1L))
                .andExpect(jsonPath("$.result.guestCount").value(4))
                .andExpect(jsonPath("$.result.accountId").value(5L))
                .andExpect(jsonPath("$.result.accountEmail").value("customer@gmail.com"))
                .andExpect(jsonPath("$.result.status").value("PENDING"))
                .andExpect(jsonPath("$.result.tables[0].tableNumber").value("B02"));
    }

    @Test
    void createReservation_TableNotFound_Returns4xx() throws Exception {
        ReservationCreateRequest request = ReservationCreateRequest.builder()
                .guestCount(4)
                .reservationTime(LocalDateTime.of(2026, 11, 20, 19, 0))
                .accountId(5L)
                .tableIds(List.of(999L))
                .build();

        when(reservationService.createReservation(any(ReservationCreateRequest.class)))
                .thenThrow(new AppException(ErrorCode.TABLE_NOT_EXISTED));

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getReservationById_Success() throws Exception {
        when(reservationService.getReservationById(1L)).thenReturn(reservationResponse);

        mockMvc.perform(get("/api/reservations/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(1L))
                .andExpect(jsonPath("$.result.accountPhone").value("0905111222"))
                .andExpect(jsonPath("$.result.guestCount").value(4));
    }

    @Test
    void getReservationById_NotFound_Returns4xx() throws Exception {
        when(reservationService.getReservationById(99L))
                .thenThrow(new AppException(ErrorCode.RESERVATION_NOT_EXISTED));

        mockMvc.perform(get("/api/reservations/{id}", 99L))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getAllReservations_Success() throws Exception {
        when(reservationService.getAllReservations()).thenReturn(List.of(reservationResponse));

        mockMvc.perform(get("/api/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(1));
    }

    @Test
    void getReservationsByAccount_Success() throws Exception {
        when(reservationService.getReservationsByAccount(5L)).thenReturn(List.of(reservationResponse));

        mockMvc.perform(get("/api/reservations/account/{accountId}", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[0].accountId").value(5L));
    }

    @Test
    void getReservationsByStatus_Success() throws Exception {
        when(reservationService.getReservationsByStatus("PENDING")).thenReturn(List.of(reservationResponse));

        mockMvc.perform(get("/api/reservations/status/{status}", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[0].status").value("PENDING"));
    }

    @Test
    void updateReservation_Success() throws Exception {
        ReservationUpdateRequest request = ReservationUpdateRequest.builder()
                .guestCount(6)
                .status("CONFIRMED")
                .tableIds(List.of(2L, 3L))
                .build();

        ReservationResponse updated = ReservationResponse.builder()
                .id(1L)
                .guestCount(6)
                .status("CONFIRMED")
                .accountId(5L)
                .accountEmail("customer@gmail.com")
                .build();

        when(reservationService.updateReservation(eq(1L), any(ReservationUpdateRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/reservations/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.guestCount").value(6))
                .andExpect(jsonPath("$.result.status").value("CONFIRMED"));
    }

    @Test
    void updateReservationStatus_Success() throws Exception {
        ReservationResponse confirmed = ReservationResponse.builder()
                .id(1L)
                .status("CONFIRMED")
                .build();

        when(reservationService.updateReservationStatus(1L, "CONFIRMED")).thenReturn(confirmed);

        mockMvc.perform(patch("/api/reservations/{id}/status", 1L)
                        .param("status", "CONFIRMED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.status").value("CONFIRMED"));
    }

    @Test
    void deleteReservation_Success() throws Exception {
        doNothing().when(reservationService).deleteReservation(1L);

        mockMvc.perform(delete("/api/reservations/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("Xóa lịch đặt bàn thành công"));

        verify(reservationService, times(1)).deleteReservation(1L);
    }
}