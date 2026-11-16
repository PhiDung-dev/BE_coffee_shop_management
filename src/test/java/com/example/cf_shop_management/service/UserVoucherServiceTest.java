package com.example.cf_shop_management.service;

import com.example.cf_shop_management.dto.request.UserVoucherCreateRequest;
import com.example.cf_shop_management.dto.request.UserVoucherUpdateRequest;
import com.example.cf_shop_management.dto.response.UserVoucherResponse;
import com.example.cf_shop_management.entity.Account;
import com.example.cf_shop_management.entity.UserVoucher;
import com.example.cf_shop_management.entity.Voucher;
import com.example.cf_shop_management.exception.AppException;
import com.example.cf_shop_management.exception.ErrorCode;
import com.example.cf_shop_management.mapper.UserVoucherMapper;
import com.example.cf_shop_management.repository.AccountRepository;
import com.example.cf_shop_management.repository.UserVoucherRepository;
import com.example.cf_shop_management.repository.VoucherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserVoucherServiceTest {

    @Mock
    private UserVoucherRepository userVoucherRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private VoucherRepository voucherRepository;

    @Mock
    private UserVoucherMapper userVoucherMapper;

    @InjectMocks
    private UserVoucherService userVoucherService;

    private Account account;
    private Voucher activeVoucher;
    private UserVoucher userVoucher;
    private UserVoucherResponse userVoucherResponse;

    @BeforeEach
    void setUp() {
        account = Account.builder()
                .id(5L)
                .email("customer@gmail.com")
                .build();

        activeVoucher = Voucher.builder()
                .id(2L)
                .code("SUMMER20")
                .isActive(true)
                .usageLimit(50)
                .usedCount(10)
                .endDate(LocalDateTime.now().plusDays(7))
                .build();

        userVoucher = UserVoucher.builder()
                .id(1L)
                .account(account)
                .voucher(activeVoucher)
                .isUsed(false)
                .usedAt(null)
                .build();

        userVoucherResponse = UserVoucherResponse.builder()
                .id(1L)
                .accountId(5L)
                .voucherId(2L)
                .isUsed(false)
                .build();
    }

    @Test
    void createUserVoucher_Success() {
        UserVoucherCreateRequest request = UserVoucherCreateRequest.builder()
                .accountId(5L)
                .voucherId(2L)
                .build();

        UserVoucher mappedUserVoucher = new UserVoucher();

        when(accountRepository.findById(5L)).thenReturn(Optional.of(account));
        when(voucherRepository.findById(2L)).thenReturn(Optional.of(activeVoucher));
        when(userVoucherRepository.existsByAccountIdAndVoucherId(5L, 2L)).thenReturn(false);
        when(userVoucherMapper.toUserVoucher(request)).thenReturn(mappedUserVoucher);
        when(userVoucherRepository.save(mappedUserVoucher)).thenReturn(userVoucher);
        when(userVoucherMapper.toUserVoucherResponse(userVoucher)).thenReturn(userVoucherResponse);

        UserVoucherResponse result = userVoucherService.createUserVoucher(request);

        assertNotNull(result);
        assertEquals(account, mappedUserVoucher.getAccount());
        assertEquals(activeVoucher, mappedUserVoucher.getVoucher());
        assertEquals(false, mappedUserVoucher.getIsUsed());
        assertNull(mappedUserVoucher.getUsedAt());
        verify(userVoucherRepository).save(mappedUserVoucher);
    }

    @Test
    void createUserVoucher_AccountNotFound_ThrowsException() {
        UserVoucherCreateRequest request = UserVoucherCreateRequest.builder()
                .accountId(99L)
                .voucherId(2L)
                .build();

        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> userVoucherService.createUserVoucher(request));

        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
        verify(userVoucherRepository, never()).save(any());
    }

    @Test
    void createUserVoucher_VoucherNotFound_ThrowsException() {
        UserVoucherCreateRequest request = UserVoucherCreateRequest.builder()
                .accountId(5L)
                .voucherId(99L)
                .build();

        when(accountRepository.findById(5L)).thenReturn(Optional.of(account));
        when(voucherRepository.findById(99L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> userVoucherService.createUserVoucher(request));

        assertEquals(ErrorCode.VOUCHER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void createUserVoucher_AlreadyExisted_ThrowsException() {
        UserVoucherCreateRequest request = UserVoucherCreateRequest.builder()
                .accountId(5L)
                .voucherId(2L)
                .build();

        when(accountRepository.findById(5L)).thenReturn(Optional.of(account));
        when(voucherRepository.findById(2L)).thenReturn(Optional.of(activeVoucher));
        when(userVoucherRepository.existsByAccountIdAndVoucherId(5L, 2L)).thenReturn(true);

        AppException exception = assertThrows(AppException.class,
                () -> userVoucherService.createUserVoucher(request));

        assertEquals(ErrorCode.USER_VOUCHER_EXISTED, exception.getErrorCode());
        verify(userVoucherRepository, never()).save(any());
    }

    @Test
    void createUserVoucher_VoucherInactive_ThrowsException() {
        activeVoucher.setIsActive(false);
        UserVoucherCreateRequest request = UserVoucherCreateRequest.builder()
                .accountId(5L)
                .voucherId(2L)
                .build();

        when(accountRepository.findById(5L)).thenReturn(Optional.of(account));
        when(voucherRepository.findById(2L)).thenReturn(Optional.of(activeVoucher));
        when(userVoucherRepository.existsByAccountIdAndVoucherId(5L, 2L)).thenReturn(false);

        AppException exception = assertThrows(AppException.class,
                () -> userVoucherService.createUserVoucher(request));

        assertEquals(ErrorCode.VOUCHER_UNAVAILABLE, exception.getErrorCode());
    }

    @Test
    void createUserVoucher_VoucherExpired_ThrowsException() {
        activeVoucher.setEndDate(LocalDateTime.now().minusMinutes(5));
        UserVoucherCreateRequest request = UserVoucherCreateRequest.builder()
                .accountId(5L)
                .voucherId(2L)
                .build();

        when(accountRepository.findById(5L)).thenReturn(Optional.of(account));
        when(voucherRepository.findById(2L)).thenReturn(Optional.of(activeVoucher));
        when(userVoucherRepository.existsByAccountIdAndVoucherId(5L, 2L)).thenReturn(false);

        AppException exception = assertThrows(AppException.class,
                () -> userVoucherService.createUserVoucher(request));

        assertEquals(ErrorCode.VOUCHER_UNAVAILABLE, exception.getErrorCode());
    }

    @Test
    void createUserVoucher_VoucherUsageLimitReached_ThrowsException() {
        activeVoucher.setUsageLimit(10);
        activeVoucher.setUsedCount(10);
        UserVoucherCreateRequest request = UserVoucherCreateRequest.builder()
                .accountId(5L)
                .voucherId(2L)
                .build();

        when(accountRepository.findById(5L)).thenReturn(Optional.of(account));
        when(voucherRepository.findById(2L)).thenReturn(Optional.of(activeVoucher));
        when(userVoucherRepository.existsByAccountIdAndVoucherId(5L, 2L)).thenReturn(false);

        AppException exception = assertThrows(AppException.class,
                () -> userVoucherService.createUserVoucher(request));

        assertEquals(ErrorCode.VOUCHER_UNAVAILABLE, exception.getErrorCode());
    }

    @Test
    void getAllUserVouchers_Paged_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserVoucher> page = new PageImpl<>(List.of(userVoucher), pageable, 1);

        when(userVoucherRepository.searchUserVouchers(5L, 2L, false, pageable)).thenReturn(page);
        when(userVoucherMapper.toUserVoucherResponse(userVoucher)).thenReturn(userVoucherResponse);

        Page<UserVoucherResponse> result = userVoucherService.getAllUserVouchers(5L, 2L, false, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getUserVoucherById_NotFound_ThrowsException() {
        when(userVoucherRepository.findById(99L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> userVoucherService.getUserVoucherById(99L));

        assertEquals(ErrorCode.USER_VOUCHER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void getUserVouchersByAccountId_Success() {
        when(accountRepository.existsById(5L)).thenReturn(true);
        when(userVoucherRepository.findByAccountIdOrderByCreatedAtDesc(5L)).thenReturn(List.of(userVoucher));
        when(userVoucherMapper.toUserVoucherResponse(userVoucher)).thenReturn(userVoucherResponse);

        List<UserVoucherResponse> result = userVoucherService.getUserVouchersByAccountId(5L);

        assertEquals(1, result.size());
    }

    @Test
    void getUserVouchersByAccountId_AccountNotFound_ThrowsException() {
        when(accountRepository.existsById(99L)).thenReturn(false);

        AppException exception = assertThrows(AppException.class,
                () -> userVoucherService.getUserVouchersByAccountId(99L));

        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void updateUserVoucher_MarkUsed_SetsUsedAt_Success() {
        UserVoucherUpdateRequest request = UserVoucherUpdateRequest.builder()
                .isUsed(true)
                .build();

        when(userVoucherRepository.findById(1L)).thenReturn(Optional.of(userVoucher));
        doAnswer(inv -> {
            userVoucher.setIsUsed(true);
            return null;
        }).when(userVoucherMapper).updateUserVoucher(request, userVoucher);
        when(userVoucherRepository.save(userVoucher)).thenReturn(userVoucher);
        when(userVoucherMapper.toUserVoucherResponse(userVoucher)).thenReturn(userVoucherResponse);

        UserVoucherResponse result = userVoucherService.updateUserVoucher(1L, request);

        assertNotNull(result);
        assertTrue(userVoucher.getIsUsed());
        assertNotNull(userVoucher.getUsedAt());
        verify(userVoucherRepository).save(userVoucher);
    }

    @Test
    void updateUserVoucher_MarkUnused_ClearsUsedAt_Success() {
        userVoucher.setIsUsed(true);
        userVoucher.setUsedAt(LocalDateTime.now());

        UserVoucherUpdateRequest request = UserVoucherUpdateRequest.builder()
                .isUsed(false)
                .build();

        when(userVoucherRepository.findById(1L)).thenReturn(Optional.of(userVoucher));
        doAnswer(inv -> {
            userVoucher.setIsUsed(false);
            return null;
        }).when(userVoucherMapper).updateUserVoucher(request, userVoucher);
        when(userVoucherRepository.save(userVoucher)).thenReturn(userVoucher);
        when(userVoucherMapper.toUserVoucherResponse(userVoucher)).thenReturn(userVoucherResponse);

        userVoucherService.updateUserVoucher(1L, request);

        assertFalse(userVoucher.getIsUsed());
        assertNull(userVoucher.getUsedAt());
    }

    @Test
    void deleteUserVoucher_Success() {
        when(userVoucherRepository.findById(1L)).thenReturn(Optional.of(userVoucher));
        doNothing().when(userVoucherRepository).delete(userVoucher);

        userVoucherService.deleteUserVoucher(1L);

        verify(userVoucherRepository).delete(userVoucher);
    }

    @Test
    void deleteUserVoucher_AlreadyUsed_ThrowsException() {
        userVoucher.setIsUsed(true);
        when(userVoucherRepository.findById(1L)).thenReturn(Optional.of(userVoucher));

        AppException exception = assertThrows(AppException.class,
                () -> userVoucherService.deleteUserVoucher(1L));

        assertEquals(ErrorCode.USER_VOUCHER_ALREADY_USED, exception.getErrorCode());
        verify(userVoucherRepository, never()).delete(any());
    }
}