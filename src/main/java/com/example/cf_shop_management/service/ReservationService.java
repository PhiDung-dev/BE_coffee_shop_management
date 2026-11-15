package com.example.cf_shop_management.service;

import com.example.cf_shop_management.dto.request.ReservationCreateRequest;
import com.example.cf_shop_management.dto.request.ReservationUpdateRequest;
import com.example.cf_shop_management.dto.response.ReservationResponse;
import com.example.cf_shop_management.dto.response.TablesResponse;
import com.example.cf_shop_management.entity.Account;
import com.example.cf_shop_management.entity.Reservation;
import com.example.cf_shop_management.entity.ReservationTable;
import com.example.cf_shop_management.entity.Tables;
import com.example.cf_shop_management.exception.AppException;
import com.example.cf_shop_management.exception.ErrorCode;
import com.example.cf_shop_management.mapper.ReservationMapper;
import com.example.cf_shop_management.mapper.TablesMapper;
import com.example.cf_shop_management.repository.AccountRepository;
import com.example.cf_shop_management.repository.ReservationRepository;
import com.example.cf_shop_management.repository.ReservationTableRepository;
import com.example.cf_shop_management.repository.TablesRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReservationService {

    ReservationRepository reservationRepository;
    ReservationTableRepository reservationTableRepository;
    AccountRepository accountRepository;
    TablesRepository tablesRepository;
    ReservationMapper reservationMapper;
    TablesMapper tablesMapper;

    @Transactional
    public ReservationResponse createReservation(ReservationCreateRequest request) {
        Reservation reservation = reservationMapper.toReservation(request);
        reservation.setStatus("PENDING");

        if (request.getAccountId() != null) {
            Account account = accountRepository.findById(request.getAccountId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            reservation.setAccount(account);
        }

        Reservation savedReservation = reservationRepository.save(reservation);

        if (request.getTableIds() != null && !request.getTableIds().isEmpty()) {
            assignTablesToReservation(savedReservation, request.getTableIds());
        }

        return buildFullResponse(savedReservation);
    }

    @Transactional(readOnly = true)
    public ReservationResponse getReservationById(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_EXISTED));
        return buildFullResponse(reservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAll()
                .stream()
                .map(this::buildFullResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservationsByAccount(Long accountId) {
        return reservationRepository.findByAccountIdOrderByReservationTimeDesc(accountId)
                .stream()
                .map(this::buildFullResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservationsByStatus(String status) {
        return reservationRepository.findByStatusOrderByReservationTimeAsc(status)
                .stream()
                .map(this::buildFullResponse)
                .toList();
    }

    @Transactional
    public ReservationResponse updateReservation(Long id, ReservationUpdateRequest request) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_EXISTED));

        reservationMapper.updateReservation(request, reservation);
        Reservation updatedReservation = reservationRepository.save(reservation);

        if (request.getTableIds() != null) {
            reservationTableRepository.deleteByReservationId(id);
            if (!request.getTableIds().isEmpty()) {
                assignTablesToReservation(updatedReservation, request.getTableIds());
            }
        }

        return buildFullResponse(updatedReservation);
    }

    @Transactional
    public ReservationResponse updateReservationStatus(Long id, String status) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_EXISTED));
        reservation.setStatus(status);
        return buildFullResponse(reservationRepository.save(reservation));
    }

    @Transactional
    public void deleteReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_EXISTED));
        reservationTableRepository.deleteByReservationId(id);
        reservationRepository.delete(reservation);
    }

    // --- Helper methods ---

    private void assignTablesToReservation(Reservation reservation, List<Long> tableIds) {
        for (Long tableId : tableIds) {
            Tables table = tablesRepository.findById(tableId)
                    .orElseThrow(() -> new AppException(ErrorCode.TABLE_NOT_EXISTED));

            ReservationTable reservationTable = ReservationTable.builder()
                    .reservation(reservation)
                    .table(table)
                    .build();
            reservationTableRepository.save(reservationTable);
        }
    }

    private ReservationResponse buildFullResponse(Reservation reservation) {
        ReservationResponse response = reservationMapper.toReservationResponse(reservation);
        List<TablesResponse> tables = reservationTableRepository.findByReservationId(reservation.getId())
                .stream()
                .map(rt -> tablesMapper.toTablesResponse(rt.getTable()))
                .toList();
        response.setTables(tables);
        return response;
    }
}
