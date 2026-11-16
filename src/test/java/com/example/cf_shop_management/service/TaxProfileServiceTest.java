package com.example.cf_shop_management.service;

import com.example.cf_shop_management.dto.request.TaxProfileCreateRequest;
import com.example.cf_shop_management.dto.request.TaxProfileUpdateRequest;
import com.example.cf_shop_management.dto.response.TaxProfileResponse;
import com.example.cf_shop_management.entity.Account;
import com.example.cf_shop_management.entity.Invoice;
import com.example.cf_shop_management.entity.TaxProfile;
import com.example.cf_shop_management.exception.AppException;
import com.example.cf_shop_management.exception.ErrorCode;
import com.example.cf_shop_management.mapper.TaxProfileMapper;
import com.example.cf_shop_management.repository.AccountRepository;
import com.example.cf_shop_management.repository.TaxProfileRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaxProfileServiceTest {

    @Mock
    private TaxProfileRepository taxProfileRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TaxProfileMapper taxProfileMapper;

    @InjectMocks
    private TaxProfileService taxProfileService;

    private Account account;
    private TaxProfile taxProfile;
    private TaxProfileResponse taxProfileResponse;

    @BeforeEach
    void setUp() {
        account = Account.builder()
                .id(5L)
                .email("customer@gmail.com")
                .build();

        taxProfile = TaxProfile.builder()
                .id(1L)
                .companyName("Công ty TNHH ABC")
                .taxCode("0401234567")
                .companyAddress("Đà Nẵng")
                .account(account)
                .build();

        taxProfileResponse = TaxProfileResponse.builder()
                .id(1L)
                .companyName("Công ty TNHH ABC")
                .taxCode("0401234567")
                .companyAddress("Đà Nẵng")
                .accountId(5L)
                .build();
    }

    @Test
    void createTaxProfile_WithAccount_Success() {
        TaxProfileCreateRequest request = TaxProfileCreateRequest.builder()
                .accountId(5L)
                .companyName("  Công ty TNHH ABC  ")
                .taxCode("  0401234567  ")
                .companyAddress("  Đà Nẵng  ")
                .build();

        TaxProfile mappedProfile = new TaxProfile();

        when(accountRepository.findById(5L)).thenReturn(Optional.of(account));
        when(taxProfileRepository.existsByAccountIdAndTaxCodeIgnoreCase(5L, "0401234567")).thenReturn(false);
        when(taxProfileMapper.toTaxProfile(request)).thenReturn(mappedProfile);
        when(taxProfileRepository.save(mappedProfile)).thenReturn(taxProfile);
        when(taxProfileMapper.toTaxProfileResponse(taxProfile)).thenReturn(taxProfileResponse);

        TaxProfileResponse result = taxProfileService.createTaxProfile(request);

        assertNotNull(result);
        assertEquals("Công ty TNHH ABC", mappedProfile.getCompanyName());
        assertEquals("0401234567", mappedProfile.getTaxCode());
        assertEquals("Đà Nẵng", mappedProfile.getCompanyAddress());
        assertEquals(account, mappedProfile.getAccount());
        verify(taxProfileRepository).save(mappedProfile);
    }

    @Test
    void createTaxProfile_AccountNotFound_ThrowsException() {
        TaxProfileCreateRequest request = TaxProfileCreateRequest.builder()
                .accountId(99L)
                .companyName("Công ty ABC")
                .taxCode("0401234567")
                .companyAddress("Đà Nẵng")
                .build();

        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> taxProfileService.createTaxProfile(request));

        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
        verify(taxProfileRepository, never()).save(any());
    }

    @Test
    void createTaxProfile_ExistedForAccount_ThrowsException() {
        TaxProfileCreateRequest request = TaxProfileCreateRequest.builder()
                .accountId(5L)
                .companyName("Công ty ABC")
                .taxCode("0401234567")
                .companyAddress("Đà Nẵng")
                .build();

        when(accountRepository.findById(5L)).thenReturn(Optional.of(account));
        when(taxProfileRepository.existsByAccountIdAndTaxCodeIgnoreCase(5L, "0401234567")).thenReturn(true);

        AppException exception = assertThrows(AppException.class,
                () -> taxProfileService.createTaxProfile(request));

        assertEquals(ErrorCode.TAX_PROFILE_EXISTED, exception.getErrorCode());
        verify(taxProfileRepository, never()).save(any());
    }

    @Test
    void getAllTaxProfiles_Paged_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<TaxProfile> page = new PageImpl<>(List.of(taxProfile), pageable, 1);

        when(taxProfileRepository.searchTaxProfiles(5L, "ABC", pageable)).thenReturn(page);
        when(taxProfileMapper.toTaxProfileResponse(taxProfile)).thenReturn(taxProfileResponse);

        Page<TaxProfileResponse> result = taxProfileService.getAllTaxProfiles(5L, "  ABC  ", pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getTaxProfileById_NotFound_ThrowsException() {
        when(taxProfileRepository.findById(99L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> taxProfileService.getTaxProfileById(99L));

        assertEquals(ErrorCode.TAX_PROFILE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void getTaxProfilesByAccountId_Success() {
        when(accountRepository.existsById(5L)).thenReturn(true);
        when(taxProfileRepository.findByAccountIdOrderByCreatedAtDesc(5L)).thenReturn(List.of(taxProfile));
        when(taxProfileMapper.toTaxProfileResponse(taxProfile)).thenReturn(taxProfileResponse);

        List<TaxProfileResponse> result = taxProfileService.getTaxProfilesByAccountId(5L);

        assertEquals(1, result.size());
    }

    @Test
    void getTaxProfilesByAccountId_AccountNotFound_ThrowsException() {
        when(accountRepository.existsById(99L)).thenReturn(false);

        AppException exception = assertThrows(AppException.class,
                () -> taxProfileService.getTaxProfilesByAccountId(99L));

        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void updateTaxProfile_Success() {
        TaxProfileUpdateRequest request = TaxProfileUpdateRequest.builder()
                .companyName("  Công ty Cổ phần XYZ  ")
                .taxCode("  0409999999  ")
                .companyAddress("  Hà Nội  ")
                .build();

        when(taxProfileRepository.findById(1L)).thenReturn(Optional.of(taxProfile));
        when(taxProfileRepository.existsByAccountIdAndTaxCodeIgnoreCaseAndIdNot(5L, "0409999999", 1L))
                .thenReturn(false);
        when(taxProfileRepository.save(taxProfile)).thenReturn(taxProfile);
        when(taxProfileMapper.toTaxProfileResponse(taxProfile)).thenReturn(taxProfileResponse);

        TaxProfileResponse result = taxProfileService.updateTaxProfile(1L, request);

        assertNotNull(result);
        assertEquals("Công ty Cổ phần XYZ", request.getCompanyName());
        assertEquals("0409999999", request.getTaxCode());
        assertEquals("Hà Nội", request.getCompanyAddress());
        verify(taxProfileMapper).updateTaxProfile(request, taxProfile);
        verify(taxProfileRepository).save(taxProfile);
    }

    @Test
    void updateTaxProfile_TaxCodeExisted_ThrowsException() {
        TaxProfileUpdateRequest request = TaxProfileUpdateRequest.builder()
                .taxCode("0409999999")
                .build();

        when(taxProfileRepository.findById(1L)).thenReturn(Optional.of(taxProfile));
        when(taxProfileRepository.existsByAccountIdAndTaxCodeIgnoreCaseAndIdNot(5L, "0409999999", 1L))
                .thenReturn(true);

        AppException exception = assertThrows(AppException.class,
                () -> taxProfileService.updateTaxProfile(1L, request));

        assertEquals(ErrorCode.TAX_PROFILE_EXISTED, exception.getErrorCode());
        verify(taxProfileRepository, never()).save(any());
    }

    @Test
    void deleteTaxProfile_Success() {
        when(taxProfileRepository.findById(1L)).thenReturn(Optional.of(taxProfile));
        doNothing().when(taxProfileRepository).delete(taxProfile);

        taxProfileService.deleteTaxProfile(1L);

        verify(taxProfileRepository).delete(taxProfile);
    }

    @Test
    void deleteTaxProfile_InUse_ThrowsException() {
        taxProfile.setInvoices(Set.of(Invoice.builder().id(1L).build()));
        when(taxProfileRepository.findById(1L)).thenReturn(Optional.of(taxProfile));

        AppException exception = assertThrows(AppException.class,
                () -> taxProfileService.deleteTaxProfile(1L));

        assertEquals(ErrorCode.TAX_PROFILE_IN_USE, exception.getErrorCode());
        verify(taxProfileRepository, never()).delete(any());
    }
}