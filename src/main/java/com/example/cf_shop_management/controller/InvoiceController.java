package com.example.cf_shop_management.controller;

import com.example.cf_shop_management.dto.ApiResponse;
import com.example.cf_shop_management.dto.request.InvoiceCreateRequest;
import com.example.cf_shop_management.dto.request.InvoiceUpdateRequest;
import com.example.cf_shop_management.dto.response.InvoiceResponse;
import com.example.cf_shop_management.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InvoiceController {

    InvoiceService invoiceService;

    @PostMapping
    public ApiResponse<InvoiceResponse> createInvoice(@Valid @RequestBody InvoiceCreateRequest request) {
        return ApiResponse.<InvoiceResponse>builder()
                .result(invoiceService.createInvoice(request))
                .build();
    }

    @GetMapping
    public ApiResponse<Page<InvoiceResponse>> getAllInvoices(
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) Long taxProfileId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "issuedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ApiResponse.<Page<InvoiceResponse>>builder()
                .result(invoiceService.getAllInvoices(orderId, taxProfileId, fromDate, toDate, keyword, pageable))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<InvoiceResponse> getInvoiceById(@PathVariable Long id) {
        return ApiResponse.<InvoiceResponse>builder()
                .result(invoiceService.getInvoiceById(id))
                .build();
    }

    @GetMapping("/order/{orderId}")
    public ApiResponse<InvoiceResponse> getInvoiceByOrderId(@PathVariable Long orderId) {
        return ApiResponse.<InvoiceResponse>builder()
                .result(invoiceService.getInvoiceByOrderId(orderId))
                .build();
    }

    @GetMapping("/number/{invoiceNumber}")
    public ApiResponse<InvoiceResponse> getInvoiceByNumber(@PathVariable String invoiceNumber) {
        return ApiResponse.<InvoiceResponse>builder()
                .result(invoiceService.getInvoiceByNumber(invoiceNumber))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<InvoiceResponse> updateInvoice(
            @PathVariable Long id,
            @Valid @RequestBody InvoiceUpdateRequest request
    ) {
        return ApiResponse.<InvoiceResponse>builder()
                .result(invoiceService.updateInvoice(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteInvoice(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
        return ApiResponse.<String>builder()
                .result("Xóa hóa đơn thành công")
                .build();
    }
}