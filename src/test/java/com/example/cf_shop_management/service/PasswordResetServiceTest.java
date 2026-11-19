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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
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

    @Mock
    private EmailService emailService;

    @InjectMocks
    private PasswordResetService passwordResetService;

    private Account account;

    @BeforeEach
    void setUp() {
        account = Account.builder()
                .id(1L)
                .email("customer@gmail.com")
                .passwordHash("encodedOldPassword")
                .build();
    }

    @Test
    void createResetToken_ValidEmail_SavesTokenAndSendsEmail() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("  customer@gmail.com  ");

        PasswordResetResponse expectedResponse = new PasswordResetResponse();

        when(accountRepository.findByEmail("customer@gmail.com")).thenReturn(Optional.of(account));
        when(passwordResetRepository.findByAccountId(1L)).thenReturn(Collections.emptyList());
        when(passwordResetRepository.existsByToken(anyString())).thenReturn(false);
        when(passwordResetRepository.save(any(PasswordReset.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordResetMapper.toPasswordResetResponse(any(PasswordReset.class)))
                .thenReturn(expectedResponse);

        PasswordResetResponse actualResponse = passwordResetService.createResetToken(request);

        assertEquals(expectedResponse, actualResponse);

        ArgumentCaptor<PasswordReset> resetCaptor = ArgumentCaptor.forClass(PasswordReset.class);
        verify(passwordResetRepository).save(resetCaptor.capture());

        PasswordReset savedReset = resetCaptor.getValue();
        assertNotNull(savedReset.getToken());
        assertEquals(account, savedReset.getAccount());
        assertFalse(savedReset.getIsUsed());
        assertTrue(savedReset.getExpiresAt().isAfter(LocalDateTime.now().plusMinutes(14)));

        verify(emailService).sendResetPasswordEmail("customer@gmail.com", savedReset.getToken());
        verify(passwordResetRepository, never()).saveAll(any());
    }

    @Test
    void createResetToken_WithExistingUnusedTokens_InvalidatesOldTokensAndSendsEmail() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("customer@gmail.com");

        PasswordReset oldUnusedToken = PasswordReset.builder()
                .id(10L)
                .token("old-unused-token")
                .account(account)
                .isUsed(false)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build();

        PasswordReset oldUsedToken = PasswordReset.builder()
                .id(11L)
                .token("old-used-token")
                .account(account)
                .isUsed(true)
                .expiresAt(LocalDateTime.now().minusMinutes(10))
                .build();

        List<PasswordReset> existingTokens = List.of(oldUnusedToken, oldUsedToken);

        when(accountRepository.findByEmail("customer@gmail.com")).thenReturn(Optional.of(account));
        when(passwordResetRepository.findByAccountId(1L)).thenReturn(existingTokens);
        when(passwordResetRepository.existsByToken(anyString())).thenReturn(false);
        when(passwordResetRepository.save(any(PasswordReset.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordResetMapper.toPasswordResetResponse(any(PasswordReset.class)))
                .thenReturn(new PasswordResetResponse());

        passwordResetService.createResetToken(request);

        assertTrue(oldUnusedToken.getIsUsed());
        assertTrue(oldUsedToken.getIsUsed());
        verify(passwordResetRepository).saveAll(existingTokens);
        verify(emailService).sendResetPasswordEmail(eq("customer@gmail.com"), anyString());
    }

    @Test
    void createResetToken_AccountNotFound_ThrowsExceptionAndDoesNotSendEmail() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("notfound@gmail.com");

        when(accountRepository.findByEmail("notfound@gmail.com")).thenReturn(Optional.empty());

        AppException exception = assertThrows(
                AppException.class,
                () -> passwordResetService.createResetToken(request)
        );

        assertEquals(ErrorCode.ACCOUNT_NOT_EXISTED, exception.getErrorCode());
        verify(passwordResetRepository, never()).save(any());
        verify(emailService, never()).sendResetPasswordEmail(anyString(), anyString());
    }

    @Test
    void createResetToken_EmailSendFails_PropagatesException() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("customer@gmail.com");

        when(accountRepository.findByEmail("customer@gmail.com")).thenReturn(Optional.of(account));
        when(passwordResetRepository.findByAccountId(1L)).thenReturn(Collections.emptyList());
        when(passwordResetRepository.existsByToken(anyString())).thenReturn(false);
        when(passwordResetRepository.save(any(PasswordReset.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new RuntimeException("SMTP error"))
                .when(emailService).sendResetPasswordEmail(eq("customer@gmail.com"), anyString());

        assertThrows(RuntimeException.class, () -> passwordResetService.createResetToken(request));
        verify(passwordResetMapper, never()).toPasswordResetResponse(any());
    }

    @Test
    void resetPassword_ValidToken_UpdatesPasswordAndMarksTokenUsed() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("  valid-uuid-token  ");
        request.setNewPassword("newSecret123");

        PasswordReset passwordReset = PasswordReset.builder()
                .id(1L)
                .token("valid-uuid-token")
                .account(account)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .isUsed(false)
                .build();

        when(passwordResetRepository.findByToken("valid-uuid-token")).thenReturn(Optional.of(passwordReset));
        when(passwordEncoder.encode("newSecret123")).thenReturn("encodedNewPassword");

        passwordResetService.resetPassword(request);

        assertEquals("encodedNewPassword", account.getPasswordHash());
        assertTrue(passwordReset.getIsUsed());
        verify(accountRepository).save(account);
        verify(passwordResetRepository).save(passwordReset);
    }

    @Test
    void resetPassword_TokenNotFound_ThrowsTokenInvalid() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("invalid-token");
        request.setNewPassword("newSecret123");

        when(passwordResetRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        AppException exception = assertThrows(
                AppException.class,
                () -> passwordResetService.resetPassword(request)
        );

        assertEquals(ErrorCode.TOKEN_INVALID, exception.getErrorCode());
        verify(accountRepository, never()).save(any());
        verify(passwordResetRepository, never()).save(any());
    }

    @Test
    void resetPassword_TokenAlreadyUsed_ThrowsTokenAlreadyUsed() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("used-token");
        request.setNewPassword("newSecret123");

        PasswordReset passwordReset = PasswordReset.builder()
                .id(1L)
                .token("used-token")
                .account(account)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .isUsed(true)
                .build();

        when(passwordResetRepository.findByToken("used-token")).thenReturn(Optional.of(passwordReset));

        AppException exception = assertThrows(
                AppException.class,
                () -> passwordResetService.resetPassword(request)
        );

        assertEquals(ErrorCode.TOKEN_ALREADY_USED, exception.getErrorCode());
        verify(accountRepository, never()).save(any());
        verify(passwordResetRepository, never()).save(any());
    }

    @Test
    void resetPassword_TokenExpired_ThrowsTokenExpired() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("expired-token");
        request.setNewPassword("newSecret123");

        PasswordReset passwordReset = PasswordReset.builder()
                .id(1L)
                .token("expired-token")
                .account(account)
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .isUsed(false)
                .build();

        when(passwordResetRepository.findByToken("expired-token")).thenReturn(Optional.of(passwordReset));

        AppException exception = assertThrows(
                AppException.class,
                () -> passwordResetService.resetPassword(request)
        );

        assertEquals(ErrorCode.TOKEN_EXPIRED, exception.getErrorCode());
        verify(accountRepository, never()).save(any());
        verify(passwordResetRepository, never()).save(any());
    }
}