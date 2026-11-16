package com.example.cf_shop_management.service;

import com.example.cf_shop_management.dto.request.ForgotPasswordRequest;
import com.example.cf_shop_management.dto.request.ResetPasswordRequest;
import com.example.cf_shop_management.dto.response.PasswordResetResponse;
import com.example.cf_shop_management.entity.Account;
import com.example.cf_shop_management.entity.PasswordReset;
import com.example.cf_shop_management.exception.AppException;
import com.example.cf_shop_management.exception.ErrorCode;
import com.example.cf_shop_management.mapper.PasswordResetMapper;
import com.example.cf_shop_management.repository.AccountRepository;
import com.example.cf_shop_management.repository.PasswordResetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private PasswordResetRepository passwordResetRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordResetMapper passwordResetMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordResetService passwordResetService;

    private Account account;
    private PasswordReset passwordReset;
    private PasswordResetResponse passwordResetResponse;

    @BeforeEach
    void setUp() {
        account = Account.builder()
                .id(5L)
                .email("user@cfshop.vn")
                .passwordHash("old_hash")
                .build();

        passwordReset = PasswordReset.builder()
                .id(10L)
                .token("valid-reset-token")
                .account(account)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .isUsed(false)
                .build();

        passwordResetResponse = PasswordResetResponse.builder()
                .id(10L)
                .accountId(5L)
                .expiresAt(passwordReset.getExpiresAt())
                .isUsed(false)
                .build();
    }

    @Test
    void createResetToken_InvalidatesOldTokensAndCreatesNew_Success() {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .email("  user@cfshop.vn  ")
                .build();

        PasswordReset oldUnusedToken = PasswordReset.builder()
                .id(1L)
                .token("old-token")
                .account(account)
                .isUsed(false)
                .build();

        when(accountRepository.findByEmail("user@cfshop.vn")).thenReturn(Optional.of(account));
        when(passwordResetRepository.findByAccountId(5L)).thenReturn(List.of(oldUnusedToken));
        when(passwordResetRepository.existsByToken(anyString())).thenReturn(false);
        when(passwordResetRepository.save(any(PasswordReset.class))).thenReturn(passwordReset);
        when(passwordResetMapper.toPasswordResetResponse(passwordReset)).thenReturn(passwordResetResponse);

        PasswordResetResponse result = passwordResetService.createResetToken(request);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertTrue(oldUnusedToken.getIsUsed());
        verify(passwordResetRepository).saveAll(List.of(oldUnusedToken));
        verify(passwordResetRepository).save(any(PasswordReset.class));
    }

    @Test
    void createResetToken_AccountNotExisted_ThrowsException() {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .email("unknown@cfshop.vn")
                .build();

        when(accountRepository.findByEmail("unknown@cfshop.vn")).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> passwordResetService.createResetToken(request));

        assertEquals(ErrorCode.ACCOUNT_NOT_EXISTED, exception.getErrorCode());
        verify(passwordResetRepository, never()).save(any());
    }

    @Test
    void resetPassword_Success() {
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token("  valid-reset-token  ")
                .newPassword("NewPass@123")
                .build();

        when(passwordResetRepository.findByToken("valid-reset-token")).thenReturn(Optional.of(passwordReset));
        when(passwordEncoder.encode("NewPass@123")).thenReturn("new_encoded_hash");
        when(accountRepository.save(account)).thenReturn(account);
        when(passwordResetRepository.save(passwordReset)).thenReturn(passwordReset);

        passwordResetService.resetPassword(request);

        assertEquals("new_encoded_hash", account.getPasswordHash());
        assertTrue(passwordReset.getIsUsed());
        verify(accountRepository).save(account);
        verify(passwordResetRepository).save(passwordReset);
    }

    @Test
    void resetPassword_TokenInvalid_ThrowsException() {
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token("invalid-token")
                .newPassword("NewPass@123")
                .build();

        when(passwordResetRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> passwordResetService.resetPassword(request));

        assertEquals(ErrorCode.TOKEN_INVALID, exception.getErrorCode());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void resetPassword_TokenAlreadyUsed_ThrowsException() {
        passwordReset.setIsUsed(true);
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token("valid-reset-token")
                .newPassword("NewPass@123")
                .build();

        when(passwordResetRepository.findByToken("valid-reset-token")).thenReturn(Optional.of(passwordReset));

        AppException exception = assertThrows(AppException.class,
                () -> passwordResetService.resetPassword(request));

        assertEquals(ErrorCode.TOKEN_ALREADY_USED, exception.getErrorCode());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void resetPassword_TokenExpired_ThrowsException() {
        passwordReset.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token("valid-reset-token")
                .newPassword("NewPass@123")
                .build();

        when(passwordResetRepository.findByToken("valid-reset-token")).thenReturn(Optional.of(passwordReset));

        AppException exception = assertThrows(AppException.class,
                () -> passwordResetService.resetPassword(request));

        assertEquals(ErrorCode.TOKEN_EXPIRED, exception.getErrorCode());
        verify(accountRepository, never()).save(any());
    }
}