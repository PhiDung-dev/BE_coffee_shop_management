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
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserVoucherService {

    UserVoucherRepository userVoucherRepository;
    AccountRepository accountRepository;
    VoucherRepository voucherRepository;
    UserVoucherMapper userVoucherMapper;

    @Transactional
    public UserVoucherResponse createUserVoucher(UserVoucherCreateRequest request) {
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        Voucher voucher = voucherRepository.findById(request.getVoucherId())
                .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));

        if (userVoucherRepository.existsByAccountIdAndVoucherId(account.getId(), voucher.getId())) {
            throw new AppException(ErrorCode.USER_VOUCHER_EXISTED);
        }

        validateVoucherClaimable(voucher);

        UserVoucher userVoucher = userVoucherMapper.toUserVoucher(request);
        userVoucher.setAccount(account);
        userVoucher.setVoucher(voucher);

        if (userVoucher.getIsUsed() == null) {
            userVoucher.setIsUsed(false);
        }
        if (Boolean.TRUE.equals(userVoucher.getIsUsed()) && userVoucher.getUsedAt() == null) {
            userVoucher.setUsedAt(LocalDateTime.now());
        }

        return userVoucherMapper.toUserVoucherResponse(userVoucherRepository.save(userVoucher));
    }

    @Transactional(readOnly = true)
    public Page<UserVoucherResponse> getAllUserVouchers(
            Long accountId,
            Long voucherId,
            Boolean isUsed,
            Pageable pageable
    ) {
        return userVoucherRepository.searchUserVouchers(accountId, voucherId, isUsed, pageable)
                .map(userVoucherMapper::toUserVoucherResponse);
    }

    @Transactional(readOnly = true)
    public UserVoucherResponse getUserVoucherById(Long id) {
        UserVoucher userVoucher = userVoucherRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_VOUCHER_NOT_FOUND));
        return userVoucherMapper.toUserVoucherResponse(userVoucher);
    }

    @Transactional(readOnly = true)
    public List<UserVoucherResponse> getUserVouchersByAccountId(Long accountId) {
        if (!accountRepository.existsById(accountId)) {
            throw new AppException(ErrorCode.ACCOUNT_NOT_FOUND);
        }
        return userVoucherRepository.findByAccountIdOrderByCreatedAtDesc(accountId)
                .stream()
                .map(userVoucherMapper::toUserVoucherResponse)
                .toList();
    }

    @Transactional
    public UserVoucherResponse updateUserVoucher(Long id, UserVoucherUpdateRequest request) {
        UserVoucher userVoucher = userVoucherRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_VOUCHER_NOT_FOUND));

        userVoucherMapper.updateUserVoucher(userVoucher, request);

        if (Boolean.TRUE.equals(userVoucher.getIsUsed())) {
            if (userVoucher.getUsedAt() == null) {
                userVoucher.setUsedAt(LocalDateTime.now());
            }
        } else if (Boolean.FALSE.equals(request.getIsUsed())) {
            // Hoàn lại voucher khi hủy đơn hàng
            userVoucher.setUsedAt(null);
        }

        return userVoucherMapper.toUserVoucherResponse(userVoucherRepository.save(userVoucher));
    }

    @Transactional
    public void deleteUserVoucher(Long id) {
        UserVoucher userVoucher = userVoucherRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_VOUCHER_NOT_FOUND));

        if (Boolean.TRUE.equals(userVoucher.getIsUsed())) {
            throw new AppException(ErrorCode.USER_VOUCHER_ALREADY_USED);
        }

        userVoucherRepository.delete(userVoucher);
    }

    private void validateVoucherClaimable(Voucher voucher) {
        if (Boolean.FALSE.equals(voucher.getIsActive())) {
            throw new AppException(ErrorCode.VOUCHER_UNAVAILABLE);
        }
        if (voucher.getEndDate() != null && voucher.getEndDate().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.VOUCHER_UNAVAILABLE);
        }
        if (voucher.getUsageLimit() != null
                && voucher.getUsedCount() != null
                && voucher.getUsedCount() >= voucher.getUsageLimit()) {
            throw new AppException(ErrorCode.VOUCHER_UNAVAILABLE);
        }
    }
}