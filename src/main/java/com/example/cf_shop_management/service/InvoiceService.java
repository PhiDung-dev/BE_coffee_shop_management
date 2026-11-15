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
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
        if (invoiceRepository.existsByOrderId(request.getOrderId())) {
            throw new AppException(ErrorCode.INVOICE_ALREADY_EXISTED);
        }

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXISTED));

        Invoice invoice = invoiceMapper.toInvoice(request);
        invoice.setInvoiceNumber(generateInvoiceNumber());
        invoice.setOrder(order);

        if (invoice.getIssuedAt() == null) {
            invoice.setIssuedAt(LocalDateTime.now());
        }

        if (request.getTaxProfileId() != null) {
            TaxProfile taxProfile = taxProfileRepository.findById(request.getTaxProfileId())
                    .orElseThrow(() -> new AppException(ErrorCode.TAX_PROFILE_NOT_EXISTED));
            invoice.setTaxProfile(taxProfile);
        }

        return invoiceMapper.toInvoiceResponse(invoiceRepository.save(invoice));
    }

    @Transactional
    public InvoiceResponse getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_EXISTED));
        return invoiceMapper.toInvoiceResponse(invoice);
    }

    @Transactional
    public InvoiceResponse getInvoiceByNumber(String invoiceNumber) {
        Invoice invoice = invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_EXISTED));
        return invoiceMapper.toInvoiceResponse(invoice);
    }

    @Transactional
    public InvoiceResponse getInvoiceByOrderId(Long orderId) {
        Invoice invoice = invoiceRepository.findByOrderId(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_EXISTED));
        return invoiceMapper.toInvoiceResponse(invoice);
    }

    @Transactional
    public List<InvoiceResponse> getAllInvoices() {
        return invoiceRepository.findAll()
                .stream()
                .map(invoiceMapper::toInvoiceResponse)
                .toList();
    }

    @Transactional
    public List<InvoiceResponse> getInvoicesByTaxProfile(Long taxProfileId) {
        return invoiceRepository.findByTaxProfileIdOrderByCreatedAtDesc(taxProfileId)
                .stream()
                .map(invoiceMapper::toInvoiceResponse)
                .toList();
    }

    @Transactional
    public InvoiceResponse updateInvoice(Long id, InvoiceUpdateRequest request) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_EXISTED));

        invoiceMapper.updateInvoice(request, invoice);

        if (request.getTaxProfileId() != null) {
            TaxProfile taxProfile = taxProfileRepository.findById(request.getTaxProfileId())
                    .orElseThrow(() -> new AppException(ErrorCode.TAX_PROFILE_NOT_EXISTED));
            invoice.setTaxProfile(taxProfile);
        }

        return invoiceMapper.toInvoiceResponse(invoiceRepository.save(invoice));
    }

    @Transactional
    public void deleteInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_EXISTED));
        invoiceRepository.delete(invoice);
    }

    private String generateInvoiceNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "INV-" + datePart + "-" + randomPart;
    }
}
