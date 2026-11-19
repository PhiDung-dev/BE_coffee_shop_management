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
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PasswordResetService {

    PasswordResetRepository passwordResetRepository;
    AccountRepository accountRepository;
    PasswordResetMapper passwordResetMapper;
    PasswordEncoder passwordEncoder;
    EmailService emailService;

    @Transactional
    public PasswordResetResponse createResetToken(ForgotPasswordRequest request) {
        String trimmedEmail = request.getEmail().trim();
        Account account = accountRepository.findByEmail(trimmedEmail)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_EXISTED));

        List<PasswordReset> existingTokens = passwordResetRepository.findByAccountId(account.getId());
        for (PasswordReset oldToken : existingTokens) {
            if (Boolean.FALSE.equals(oldToken.getIsUsed())) {
                oldToken.setIsUsed(true);
            }
        }
        if (!existingTokens.isEmpty()) {
            passwordResetRepository.saveAll(existingTokens);
        }

        String token;
        do {
            token = UUID.randomUUID().toString();
        } while (passwordResetRepository.existsByToken(token));

        PasswordReset passwordReset = PasswordReset.builder()
                .token(token)
                .account(account)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .isUsed(false)
                .build();

        PasswordReset savedReset = passwordResetRepository.save(passwordReset);
        emailService.sendResetPasswordEmail(trimmedEmail, token);

        return passwordResetMapper.toPasswordResetResponse(savedReset);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordReset passwordReset = passwordResetRepository.findByToken(request.getToken().trim())
                .orElseThrow(() -> new AppException(ErrorCode.TOKEN_INVALID));

        if (Boolean.TRUE.equals(passwordReset.getIsUsed())) {
            throw new AppException(ErrorCode.TOKEN_ALREADY_USED);
        }
        if (passwordReset.getExpiresAt() != null && passwordReset.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        }

        Account account = passwordReset.getAccount();
        account.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        accountRepository.save(account);

        passwordReset.setIsUsed(true);
        passwordResetRepository.save(passwordReset);
    }
}