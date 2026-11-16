package com.example.cf_shop_management.service;

import com.example.cf_shop_management.dto.request.VoucherCreateRequest;
import com.example.cf_shop_management.dto.request.VoucherUpdateRequest;
import com.example.cf_shop_management.dto.response.VoucherResponse;
import com.example.cf_shop_management.entity.Voucher;
import com.example.cf_shop_management.exception.AppException;
import com.example.cf_shop_management.exception.ErrorCode;
import com.example.cf_shop_management.mapper.VoucherMapper;
import com.example.cf_shop_management.repository.VoucherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VoucherServiceTest {

    @Mock
    private VoucherRepository voucherRepository;

    @Mock
    private VoucherMapper voucherMapper;

    @InjectMocks
    private VoucherService voucherService;

    private Voucher voucher;
    private VoucherResponse voucherResponse;

    @BeforeEach
    void setUp() {
        voucher = Voucher.builder()
                .id(1L)
                .code("WELCOME10")
                .discountType("PERCENTAGE")
                .discountValue(new BigDecimal("10.00"))
                .usedCount(0)
                .isActive(true)
                .build();

        voucherResponse = VoucherResponse.builder()
                .id(1L)
                .code("WELCOME10")
                .discountType("PERCENTAGE")
                .discountValue(new BigDecimal("10.00"))
                .usedCount(0)
                .isActive(true)
                .build();
    }

    @Test
    void createVoucher_NormalizesCodeAndDefaultsActive_Success() {
        VoucherCreateRequest request = VoucherCreateRequest.builder()
                .code("  welcome10  ")
                .discountType("  percentage  ")
                .discountValue(new BigDecimal("10.00"))
                .build();

        Voucher mappedVoucher = new Voucher();

        when(voucherRepository.existsByCode("WELCOME10")).thenReturn(false);
        when(voucherMapper.toVoucher(request)).thenReturn(mappedVoucher);
        when(voucherRepository.save(mappedVoucher)).thenReturn(voucher);
        when(voucherMapper.toVoucherResponse(voucher)).thenReturn(voucherResponse);

        VoucherResponse result = voucherService.createVoucher(request);

        assertNotNull(result);
        assertEquals("WELCOME10", mappedVoucher.getCode());
        assertEquals("PERCENTAGE", mappedVoucher.getDiscountType());
        assertEquals(0, mappedVoucher.getUsedCount());
        assertEquals(true, mappedVoucher.getIsActive());
        verify(voucherRepository).save(mappedVoucher);
    }

    @Test
    void createVoucher_CodeExisted_ThrowsException() {
        VoucherCreateRequest request = VoucherCreateRequest.builder()
                .code("  welcome10  ")
                .discountType("FIXED")
                .discountValue(new BigDecimal("15000.00"))
                .build();

        when(voucherRepository.existsByCode("WELCOME10")).thenReturn(true);

        AppException exception = assertThrows(AppException.class,
                () -> voucherService.createVoucher(request));

        assertEquals(ErrorCode.VOUCHER_CODE_EXISTED, exception.getErrorCode());
        verify(voucherRepository, never()).save(any());
    }

    @Test
    void updateVoucher_Success() {
        VoucherUpdateRequest request = VoucherUpdateRequest.builder()
                .code("  newcode20  ")
                .discountType("  fixed  ")
                .isActive(false)
                .build();

        when(voucherRepository.findById(1L)).thenReturn(Optional.of(voucher));
        when(voucherRepository.existsByCodeIgnoreCaseAndIdNot("NEWCODE20", 1L)).thenReturn(false);
        when(voucherRepository.save(voucher)).thenReturn(voucher);
        when(voucherMapper.toVoucherResponse(voucher)).thenReturn(voucherResponse);

        VoucherResponse result = voucherService.updateVoucher(1L, request);

        assertNotNull(result);
        assertEquals("NEWCODE20", request.getCode());
        assertEquals("FIXED", request.getDiscountType());
        verify(voucherMapper).updateVoucher(request, voucher);
        verify(voucherRepository).save(voucher);
    }

    @Test
    void updateVoucher_NotFound_ThrowsException() {
        VoucherUpdateRequest request = VoucherUpdateRequest.builder().code("NEWCODE").build();
        when(voucherRepository.findById(99L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> voucherService.updateVoucher(99L, request));

        assertEquals(ErrorCode.VOUCHER_NOT_EXISTED, exception.getErrorCode());
    }

    @Test
    void updateVoucher_CodeExisted_ThrowsException() {
        VoucherUpdateRequest request = VoucherUpdateRequest.builder()
                .code("  duplicate  ")
                .build();

        when(voucherRepository.findById(1L)).thenReturn(Optional.of(voucher));
        when(voucherRepository.existsByCodeIgnoreCaseAndIdNot("DUPLICATE", 1L)).thenReturn(true);

        AppException exception = assertThrows(AppException.class,
                () -> voucherService.updateVoucher(1L, request));

        assertEquals(ErrorCode.VOUCHER_CODE_EXISTED, exception.getErrorCode());
        verify(voucherRepository, never()).save(any());
    }

    @Test
    void getVoucherById_Success() {
        when(voucherRepository.findById(1L)).thenReturn(Optional.of(voucher));
        when(voucherMapper.toVoucherResponse(voucher)).thenReturn(voucherResponse);

        VoucherResponse result = voucherService.getVoucherById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void getVoucherById_NotFound_ThrowsException() {
        when(voucherRepository.findById(99L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> voucherService.getVoucherById(99L));

        assertEquals(ErrorCode.VOUCHER_NOT_EXISTED, exception.getErrorCode());
    }

    @Test
    void getVoucherByCode_Success() {
        when(voucherRepository.findByCode("WELCOME10")).thenReturn(Optional.of(voucher));
        when(voucherMapper.toVoucherResponse(voucher)).thenReturn(voucherResponse);

        VoucherResponse result = voucherService.getVoucherByCode("  welcome10  ");

        assertEquals("WELCOME10", result.getCode());
    }

    @Test
    void getVoucherByCode_NotFound_ThrowsException() {
        when(voucherRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> voucherService.getVoucherByCode("invalid"));

        assertEquals(ErrorCode.VOUCHER_NOT_EXISTED, exception.getErrorCode());
    }

    @Test
    void getAllVouchers_Success() {
        when(voucherRepository.findAll()).thenReturn(List.of(voucher));
        when(voucherMapper.toVoucherResponse(voucher)).thenReturn(voucherResponse);

        List<VoucherResponse> result = voucherService.getAllVouchers();

        assertEquals(1, result.size());
    }

    @Test
    void getActiveVouchers_Success() {
        when(voucherRepository.findByIsActiveTrue()).thenReturn(List.of(voucher));
        when(voucherMapper.toVoucherResponse(voucher)).thenReturn(voucherResponse);

        List<VoucherResponse> result = voucherService.getActiveVouchers();

        assertEquals(1, result.size());
        assertTrue(result.get(0).getIsActive());
    }

    @Test
    void deleteVoucher_Success() {
        when(voucherRepository.existsById(1L)).thenReturn(true);
        doNothing().when(voucherRepository).deleteById(1L);

        voucherService.deleteVoucher(1L);

        verify(voucherRepository).deleteById(1L);
    }

    @Test
    void deleteVoucher_NotFound_ThrowsException() {
        when(voucherRepository.existsById(99L)).thenReturn(false);

        AppException exception = assertThrows(AppException.class,
                () -> voucherService.deleteVoucher(99L));

        assertEquals(ErrorCode.VOUCHER_NOT_EXISTED, exception.getErrorCode());
        verify(voucherRepository, never()).deleteById(any());
    }
}