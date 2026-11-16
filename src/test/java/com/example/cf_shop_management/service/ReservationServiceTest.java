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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationTableRepository reservationTableRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TablesRepository tablesRepository;

    @Mock
    private ReservationMapper reservationMapper;

    @Mock
    private TablesMapper tablesMapper;

    @InjectMocks
    private ReservationService reservationService;

    private Account account;
    private Tables table1;
    private TablesResponse tableResponse1;
    private Reservation reservation;
    private ReservationResponse reservationResponse;

    @BeforeEach
    void setUp() {
        account = Account.builder()
                .id(5L)
                .email("customer@gmail.com")
                .build();

        table1 = Tables.builder()
                .id(1L)
                .tableNumber("B01")
                .status("AVAILABLE")
                .build();

        tableResponse1 = TablesResponse.builder()
                .id(1L)
                .tableNumber("B01")
                .status("AVAILABLE")
                .build();

        reservation = Reservation.builder()
                .id(10L)
                .account(account)
                .status("PENDING")
                .build();

        reservationResponse = ReservationResponse.builder()
                .id(10L)
                .status("PENDING")
                .build();
    }

    @Test
    void createReservation_WithAccountAndDistinctTables_Success() {
        ReservationCreateRequest request = ReservationCreateRequest.builder()
                .accountId(5L)
                .tableIds(List.of(1L, 1L)) // duplicate IDs should be deduplicated
                .build();

        Reservation mappedReservation = new Reservation();
        ReservationTable rt = ReservationTable.builder()
                .reservation(reservation)
                .table(table1)
                .build();

        when(reservationMapper.toReservation(request)).thenReturn(mappedReservation);
        when(accountRepository.findById(5L)).thenReturn(Optional.of(account));
        when(reservationRepository.save(mappedReservation)).thenReturn(reservation);
        when(tablesRepository.findById(1L)).thenReturn(Optional.of(table1));
        when(reservationTableRepository.findByReservationId(10L)).thenReturn(List.of(rt));
        when(reservationMapper.toReservationResponse(reservation)).thenReturn(reservationResponse);
        when(tablesMapper.toTablesResponse(table1)).thenReturn(tableResponse1);

        ReservationResponse result = reservationService.createReservation(request);

        assertNotNull(result);
        assertEquals("PENDING", mappedReservation.getStatus());
        assertEquals(account, mappedReservation.getAccount());
        assertEquals(1, result.getTables().size());
        assertEquals("B01", result.getTables().get(0).getTableNumber());
        verify(reservationTableRepository, times(1)).save(any(ReservationTable.class));
    }

    @Test
    void createReservation_UserNotExisted_ThrowsException() {
        ReservationCreateRequest request = ReservationCreateRequest.builder()
                .accountId(99L)
                .build();

        when(reservationMapper.toReservation(request)).thenReturn(new Reservation());
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> reservationService.createReservation(request));

        assertEquals(ErrorCode.USER_NOT_EXISTED, exception.getErrorCode());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservation_TableNotExisted_ThrowsException() {
        ReservationCreateRequest request = ReservationCreateRequest.builder()
                .tableIds(List.of(99L))
                .build();

        Reservation mappedReservation = new Reservation();
        when(reservationMapper.toReservation(request)).thenReturn(mappedReservation);
        when(reservationRepository.save(mappedReservation)).thenReturn(reservation);
        when(tablesRepository.findById(99L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> reservationService.createReservation(request));

        assertEquals(ErrorCode.TABLE_NOT_EXISTED, exception.getErrorCode());
        verify(reservationTableRepository, never()).save(any());
    }

    @Test
    void getReservationById_Success() {
        when(reservationRepository.findById(10L)).thenReturn(Optional.of(reservation));
        when(reservationMapper.toReservationResponse(reservation)).thenReturn(reservationResponse);
        when(reservationTableRepository.findByReservationId(10L)).thenReturn(List.of());

        ReservationResponse result = reservationService.getReservationById(10L);

        assertEquals(10L, result.getId());
        assertTrue(result.getTables().isEmpty());
    }

    @Test
    void getReservationById_NotFound_ThrowsException() {
        when(reservationRepository.findById(99L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> reservationService.getReservationById(99L));

        assertEquals(ErrorCode.RESERVATION_NOT_EXISTED, exception.getErrorCode());
    }

    @Test
    void getAllReservations_Success() {
        when(reservationRepository.findAll()).thenReturn(List.of(reservation));
        when(reservationMapper.toReservationResponse(reservation)).thenReturn(reservationResponse);
        when(reservationTableRepository.findByReservationId(10L)).thenReturn(List.of());

        List<ReservationResponse> result = reservationService.getAllReservations();

        assertEquals(1, result.size());
    }

    @Test
    void getReservationsByAccount_Success() {
        when(reservationRepository.findByAccountIdOrderByReservationTimeDesc(5L)).thenReturn(List.of(reservation));
        when(reservationMapper.toReservationResponse(reservation)).thenReturn(reservationResponse);
        when(reservationTableRepository.findByReservationId(10L)).thenReturn(List.of());

        List<ReservationResponse> result = reservationService.getReservationsByAccount(5L);

        assertEquals(1, result.size());
    }

    @Test
    void getReservationsByStatus_Success() {
        when(reservationRepository.findByStatusOrderByReservationTimeAsc("CONFIRMED")).thenReturn(List.of(reservation));
        when(reservationMapper.toReservationResponse(reservation)).thenReturn(reservationResponse);
        when(reservationTableRepository.findByReservationId(10L)).thenReturn(List.of());

        List<ReservationResponse> result = reservationService.getReservationsByStatus("  confirmed  ");

        assertEquals(1, result.size());
        verify(reservationRepository).findByStatusOrderByReservationTimeAsc("CONFIRMED");
    }

    @Test
    void updateReservation_ReplacesTablesAndUpdatesStatus_Success() {
        ReservationUpdateRequest request = ReservationUpdateRequest.builder()
                .status("  confirmed  ")
                .tableIds(List.of(1L))
                .build();

        ReservationTable rt = ReservationTable.builder()
                .reservation(reservation)
                .table(table1)
                .build();

        when(reservationRepository.findById(10L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(reservation)).thenReturn(reservation);
        when(tablesRepository.findById(1L)).thenReturn(Optional.of(table1));
        when(reservationTableRepository.findByReservationId(10L)).thenReturn(List.of(rt));
        when(reservationMapper.toReservationResponse(reservation)).thenReturn(reservationResponse);
        when(tablesMapper.toTablesResponse(table1)).thenReturn(tableResponse1);

        ReservationResponse result = reservationService.updateReservation(10L, request);

        assertNotNull(result);
        assertEquals("CONFIRMED", request.getStatus());
        verify(reservationMapper).updateReservation(request, reservation);
        verify(reservationTableRepository).deleteByReservationId(10L);
        verify(reservationTableRepository).save(any(ReservationTable.class));
    }

    @Test
    void updateReservationStatus_Success() {
        when(reservationRepository.findById(10L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(reservation)).thenReturn(reservation);
        when(reservationMapper.toReservationResponse(reservation)).thenReturn(reservationResponse);
        when(reservationTableRepository.findByReservationId(10L)).thenReturn(List.of());

        ReservationResponse result = reservationService.updateReservationStatus(10L, "  cancelled  ");

        assertNotNull(result);
        assertEquals("CANCELLED", reservation.getStatus());
        verify(reservationRepository).save(reservation);
    }

    @Test
    void deleteReservation_Success() {
        when(reservationRepository.findById(10L)).thenReturn(Optional.of(reservation));
        doNothing().when(reservationTableRepository).deleteByReservationId(10L);
        doNothing().when(reservationRepository).delete(reservation);

        reservationService.deleteReservation(10L);

        verify(reservationTableRepository).deleteByReservationId(10L);
        verify(reservationRepository).delete(reservation);
    }

    @Test
    void deleteReservation_NotFound_ThrowsException() {
        when(reservationRepository.findById(99L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> reservationService.deleteReservation(99L));

        assertEquals(ErrorCode.RESERVATION_NOT_EXISTED, exception.getErrorCode());
        verify(reservationRepository, never()).delete(any());
    }
}