package com.example.cf_shop_management.service;

import com.example.cf_shop_management.dto.request.InvoiceCreateRequest;
import com.example.cf_shop_management.dto.request.InvoiceUpdateRequest;
import com.example.cf_shop_management.dto.response.InvoiceResponse;
import com.example.cf_shop_management.entity.Invoice;
import com.example.cf_shop_management.entity.Order;
import com.example.cf_shop_management.entity.TaxProfile;
import com.example.cf_shop_management.exception.AppException;
import com.example.cf_shop_management.exception.ErrorCode;
import com.example.cf_shop_management.mapper.InvoiceMapper;
import com.example.cf_shop_management.repository.InvoiceRepository;
import com.example.cf_shop_management.repository.OrderRepository;
import com.example.cf_shop_management.repository.TaxProfileRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InvoiceService {

    InvoiceRepository invoiceRepository;
    OrderRepository orderRepository;
    TaxProfileRepository taxProfileRepository;
    InvoiceMapper invoiceMapper;

    @Transactional
    public InvoiceResponse createInvoice(InvoiceCreateRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if ("CANCELLED".equalsIgnoreCase(order.getStatus())) {
            throw new AppException(ErrorCode.ORDER_CANCELLED_CANNOT_INVOICE);
        }

        if (invoiceRepository.existsByOrderId(order.getId())) {
            throw new AppException(ErrorCode.INVOICE_ALREADY_EXISTED_FOR_ORDER);
        }

        TaxProfile taxProfile = null;
        if (request.getTaxProfileId() != null) {
            taxProfile = taxProfileRepository.findById(request.getTaxProfileId())
                    .orElseThrow(() -> new AppException(ErrorCode.TAX_PROFILE_NOT_FOUND));
        }

        Invoice invoice = invoiceMapper.toInvoice(request);
        invoice.setInvoiceNumber(generateUniqueInvoiceNumber());
        invoice.setIssuedAt(LocalDateTime.now());
        invoice.setOrder(order);
        invoice.setTaxProfile(taxProfile);

        Invoice savedInvoice = invoiceRepository.save(invoice);
        order.setInvoice(savedInvoice);

        return invoiceMapper.toInvoiceResponse(savedInvoice);
    }

    @Transactional(readOnly = true)
    public Page<InvoiceResponse> getAllInvoices(
            Long orderId,
            Long taxProfileId,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            String keyword,
            Pageable pageable
    ) {
        String normalizedKeyword = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return invoiceRepository.searchInvoices(
                        orderId,
                        taxProfileId,
                        fromDate,
                        toDate,
                        normalizedKeyword,
                        pageable
                )
                .map(invoiceMapper::toInvoiceResponse);
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));
        return invoiceMapper.toInvoiceResponse(invoice);
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceByOrderId(Long orderId) {
        Invoice invoice = invoiceRepository.findByOrderId(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));
        return invoiceMapper.toInvoiceResponse(invoice);
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceByNumber(String invoiceNumber) {
        Invoice invoice = invoiceRepository.findByInvoiceNumberIgnoreCase(invoiceNumber.trim())
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));
        return invoiceMapper.toInvoiceResponse(invoice);
    }

    @Transactional
    public InvoiceResponse updateInvoice(Long id, InvoiceUpdateRequest request) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));

        if (request.getTaxProfileId() != null) {
            TaxProfile taxProfile = taxProfileRepository.findById(request.getTaxProfileId())
                    .orElseThrow(() -> new AppException(ErrorCode.TAX_PROFILE_NOT_FOUND));
            invoice.setTaxProfile(taxProfile);
        }

        invoiceMapper.updateInvoice(request, invoice);

        return invoiceMapper.toInvoiceResponse(invoiceRepository.save(invoice));
    }

    @Transactional
    public void deleteInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));

        if (invoice.getOrder() != null) {
            invoice.getOrder().setInvoice(null);
        }

        invoiceRepository.delete(invoice);
    }

    private String generateUniqueInvoiceNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String candidate;
        do {
            String randomPart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            candidate = "INV-" + datePart + "-" + randomPart;
        } while (invoiceRepository.existsByInvoiceNumberIgnoreCase(candidate));
        return candidate;
    }
}