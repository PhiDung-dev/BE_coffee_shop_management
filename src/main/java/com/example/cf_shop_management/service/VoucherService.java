package com.example.cf_shop_management.service;

import com.example.cf_shop_management.dto.request.VoucherCreateRequest;
import com.example.cf_shop_management.dto.request.VoucherUpdateRequest;
import com.example.cf_shop_management.dto.response.VoucherResponse;
import com.example.cf_shop_management.entity.Voucher;
import com.example.cf_shop_management.exception.AppException;
import com.example.cf_shop_management.exception.ErrorCode;
import com.example.cf_shop_management.mapper.VoucherMapper;
import com.example.cf_shop_management.repository.VoucherRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VoucherService {

    VoucherRepository voucherRepository;
    VoucherMapper voucherMapper;

    @Transactional
    public VoucherResponse createVoucher(VoucherCreateRequest request) {
        if (voucherRepository.existsByCode(request.getCode())) {
            throw new AppException(ErrorCode.VOUCHER_CODE_EXISTED);
        }

        Voucher voucher = voucherMapper.toVoucher(request);
        voucher.setUsedCount(0);

        if (voucher.getIsActive() == null) {
            voucher.setIsActive(true);
        }

        return voucherMapper.toVoucherResponse(voucherRepository.save(voucher));
    }

    @Transactional
    public VoucherResponse updateVoucher(Long id, VoucherUpdateRequest request) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_EXISTED));

        if (request.getCode() != null && !request.getCode().equals(voucher.getCode())) {
            if (voucherRepository.existsByCode(request.getCode())) {
                throw new AppException(ErrorCode.VOUCHER_CODE_EXISTED);
            }
        }

        voucherMapper.updateVoucher(request, voucher);

        return voucherMapper.toVoucherResponse(voucherRepository.save(voucher));
    }

    @Transactional(readOnly = true)
    public VoucherResponse getVoucherById(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_EXISTED));
        return voucherMapper.toVoucherResponse(voucher);
    }

    @Transactional(readOnly = true)
    public VoucherResponse getVoucherByCode(String code) {
        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_EXISTED));
        return voucherMapper.toVoucherResponse(voucher);
    }

    @Transactional(readOnly = true)
    public List<VoucherResponse> getAllVouchers() {
        return voucherRepository.findAll()
                .stream()
                .map(voucherMapper::toVoucherResponse)
                .toList();
    }

    @Transactional
    public void deleteVoucher(Long id) {
        if (!voucherRepository.existsById(id)) {
            throw new AppException(ErrorCode.VOUCHER_NOT_EXISTED);
        }
        voucherRepository.deleteById(id);
    }
}