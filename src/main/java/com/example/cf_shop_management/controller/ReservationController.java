package com.example.cf_shop_management.controller;

import com.example.cf_shop_management.dto.ApiResponse;
import com.example.cf_shop_management.dto.request.ReservationCreateRequest;
import com.example.cf_shop_management.dto.request.ReservationUpdateRequest;

import com.example.cf_shop_management.dto.response.ReservationResponse;
import com.example.cf_shop_management.service.ReservationService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReservationController {

    ReservationService reservationService;

    @PostMapping
    public ApiResponse<ReservationResponse> createReservation(@Valid @RequestBody ReservationCreateRequest request) {
        return ApiResponse.<ReservationResponse>builder()
                .result(reservationService.createReservation(request))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<ReservationResponse> getReservationById(@PathVariable("id") Long id) {
        return ApiResponse.<ReservationResponse>builder()
                .result(reservationService.getReservationById(id))
                .build();
    }

    @GetMapping
    public ApiResponse<List<ReservationResponse>> getAllReservations() {
        return ApiResponse.<List<ReservationResponse>>builder()
                .result(reservationService.getAllReservations())
                .build();
    }

    @GetMapping("/account/{accountId}")
    public ApiResponse<List<ReservationResponse>> getReservationsByAccount(@PathVariable("accountId") Long accountId) {
        return ApiResponse.<List<ReservationResponse>>builder()
                .result(reservationService.getReservationsByAccount(accountId))
                .build();
    }

    @GetMapping("/status/{status}")
    public ApiResponse<List<ReservationResponse>> getReservationsByStatus(@PathVariable("status") String status) {
        return ApiResponse.<List<ReservationResponse>>builder()
                .result(reservationService.getReservationsByStatus(status))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<ReservationResponse> updateReservation(
            @PathVariable("id") Long id,
            @Valid @RequestBody ReservationUpdateRequest request) {
        return ApiResponse.<ReservationResponse>builder()
                .result(reservationService.updateReservation(id, request))
                .build();
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<ReservationResponse> updateReservationStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") String status) {
        return ApiResponse.<ReservationResponse>builder()
                .result(reservationService.updateReservationStatus(id, status))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteReservation(@PathVariable("id") Long id) {
        reservationService.deleteReservation(id);
        return ApiResponse.<String>builder()
                .result("Xóa lịch đặt bàn thành công")
                .build();
    }
}
