package com.example.cf_shop_management.service;

import com.example.cf_shop_management.dto.request.LoginRequest;
import com.example.cf_shop_management.dto.response.AuthResponse;
import com.example.cf_shop_management.entity.Account;
import com.example.cf_shop_management.exception.AppException;
import com.example.cf_shop_management.exception.ErrorCode;
import com.example.cf_shop_management.repository.AccountRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthService {

    AccountRepository accountRepository;
    PasswordEncoder passwordEncoder;
    JwtEncoder jwtEncoder;

    @NonFinal
    @Value("${jwt.expiration}")
    long jwtExpiration;

    public AuthResponse login(LoginRequest request) {
        Account account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email hoặc mật khẩu không chính xác"));

        if (!"ACTIVE".equalsIgnoreCase(account.getStatus())) {
            throw new AppException(ErrorCode.ACCOUNT_NOT_EXISTED);
        }

        if (!passwordEncoder.matches(request.getPassword(), account.getPasswordHash())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String token = buildToken(account);

        return AuthResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    private String buildToken(Account account) {
        Instant now = Instant.now();
        String roleName = account.getRole() != null ? account.getRole().getName() : "CUSTOMER";

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("cf-shop-management")
                .issuedAt(now)
                .expiresAt(now.plus(jwtExpiration, ChronoUnit.SECONDS))
                .subject(account.getEmail())
                .claim("accountId", account.getId())
                .claim("scope", "ROLE_" + roleName)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}