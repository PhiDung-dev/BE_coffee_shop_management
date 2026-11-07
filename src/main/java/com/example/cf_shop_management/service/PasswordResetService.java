package com.example.cf_shop_management.service;

import com.example.cf_shop_management.entity.Account;
import com.example.cf_shop_management.entity.PasswordReset;
import com.example.cf_shop_management.exception.AppException;
import com.example.cf_shop_management.exception.ErrorCode;
import com.example.cf_shop_management.repository.AccountRepository;
import com.example.cf_shop_management.repository.PasswordResetRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PasswordResetService {

    PasswordResetRepository passwordResetRepository;
    AccountRepository accountRepository;
    PasswordEncoder passwordEncoder;

    @Transactional
    public String createResetToken(String email) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_EXISTED));
        String token = UUID.randomUUID().toString();
        PasswordReset passwordReset = PasswordReset.builder()
                .token(token)
                .account(account)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .isUsed(false)
                .build();
        passwordResetRepository.save(passwordReset);
        return token;
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordReset passwordReset = passwordResetRepository.findByToken(token)
                .orElseThrow(() -> new AppException(ErrorCode.TOKEN_INVALID));
        if (Boolean.TRUE.equals(passwordReset.getIsUsed())) {
            throw new AppException(ErrorCode.TOKEN_ALREADY_USED);
        }
        if (passwordReset.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        }
        Account account = passwordReset.getAccount();
        account.setPasswordHash(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
        passwordReset.setIsUsed(true);
        passwordResetRepository.save(passwordReset);
    }
}