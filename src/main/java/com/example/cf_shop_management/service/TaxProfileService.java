package com.example.cf_shop_management.service;

import com.example.cf_shop_management.dto.request.TaxProfileCreateRequest;
import com.example.cf_shop_management.dto.request.TaxProfileUpdateRequest;
import com.example.cf_shop_management.dto.response.TaxProfileResponse;
import com.example.cf_shop_management.entity.Account;
import com.example.cf_shop_management.entity.TaxProfile;
import com.example.cf_shop_management.exception.AppException;
import com.example.cf_shop_management.exception.ErrorCode;
import com.example.cf_shop_management.mapper.TaxProfileMapper;
import com.example.cf_shop_management.repository.AccountRepository;
import com.example.cf_shop_management.repository.TaxProfileRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TaxProfileService {

    TaxProfileRepository taxProfileRepository;
    AccountRepository accountRepository;
    TaxProfileMapper taxProfileMapper;

    @Transactional
    public TaxProfileResponse createTaxProfile(TaxProfileCreateRequest request) {
        String trimmedTaxCode = request.getTaxCode().trim();
        Account account = null;

        if (request.getAccountId() != null) {
            account = accountRepository.findById(request.getAccountId())
                    .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

            if (taxProfileRepository.existsByAccountIdAndTaxCodeIgnoreCase(account.getId(), trimmedTaxCode)) {
                throw new AppException(ErrorCode.TAX_PROFILE_EXISTED);
            }
        }

        TaxProfile taxProfile = taxProfileMapper.toTaxProfile(request);
        taxProfile.setCompanyName(request.getCompanyName().trim());
        taxProfile.setTaxCode(trimmedTaxCode);
        taxProfile.setCompanyAddress(request.getCompanyAddress().trim());
        taxProfile.setAccount(account);

        return taxProfileMapper.toTaxProfileResponse(taxProfileRepository.save(taxProfile));
    }

    @Transactional(readOnly = true)
    public Page<TaxProfileResponse> getAllTaxProfiles(Long accountId, String keyword, Pageable pageable) {
        String normalizedKeyword = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return taxProfileRepository.searchTaxProfiles(accountId, normalizedKeyword, pageable)
                .map(taxProfileMapper::toTaxProfileResponse);
    }

    @Transactional(readOnly = true)
    public TaxProfileResponse getTaxProfileById(Long id) {
        TaxProfile taxProfile = taxProfileRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TAX_PROFILE_NOT_FOUND));
        return taxProfileMapper.toTaxProfileResponse(taxProfile);
    }

    @Transactional(readOnly = true)
    public List<TaxProfileResponse> getTaxProfilesByAccountId(Long accountId) {
        if (!accountRepository.existsById(accountId)) {
            throw new AppException(ErrorCode.ACCOUNT_NOT_FOUND);
        }
        return taxProfileRepository.findByAccountIdOrderByCreatedAtDesc(accountId)
                .stream()
                .map(taxProfileMapper::toTaxProfileResponse)
                .toList();
    }

    @Transactional
    public TaxProfileResponse updateTaxProfile(Long id, TaxProfileUpdateRequest request) {
        TaxProfile taxProfile = taxProfileRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TAX_PROFILE_NOT_FOUND));

        if (request.getAccountId() != null) {
            Account account = accountRepository.findById(request.getAccountId())
                    .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
            taxProfile.setAccount(account);
        }

        if (request.getCompanyName() != null && !request.getCompanyName().isBlank()) {
            request.setCompanyName(request.getCompanyName().trim());
        }
        if (request.getCompanyAddress() != null && !request.getCompanyAddress().isBlank()) {
            request.setCompanyAddress(request.getCompanyAddress().trim());
        }
        if (request.getTaxCode() != null && !request.getTaxCode().isBlank()) {
            request.setTaxCode(request.getTaxCode().trim());
        }

        Long targetAccountId = taxProfile.getAccount() != null ? taxProfile.getAccount().getId() : null;
        String targetTaxCode = request.getTaxCode() != null ? request.getTaxCode() : taxProfile.getTaxCode();

        if (targetAccountId != null
                && taxProfileRepository.existsByAccountIdAndTaxCodeIgnoreCaseAndIdNot(targetAccountId, targetTaxCode, id)) {
            throw new AppException(ErrorCode.TAX_PROFILE_EXISTED);
        }

        taxProfileMapper.updateTaxProfile(taxProfile, request);

        return taxProfileMapper.toTaxProfileResponse(taxProfileRepository.save(taxProfile));
    }

    @Transactional
    public void deleteTaxProfile(Long id) {
        TaxProfile taxProfile = taxProfileRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TAX_PROFILE_NOT_FOUND));

        if (taxProfile.getInvoices() != null && !taxProfile.getInvoices().isEmpty()) {
            throw new AppException(ErrorCode.TAX_PROFILE_IN_USE);
        }

        taxProfileRepository.delete(taxProfile);
    }
}