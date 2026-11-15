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
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/{id}")
    public ApiResponse<InvoiceResponse> getInvoiceById(@PathVariable("id") Long id) {
        return ApiResponse.<InvoiceResponse>builder()
                .result(invoiceService.getInvoiceById(id))
                .build();
    }

    @GetMapping("/number/{invoiceNumber}")
    public ApiResponse<InvoiceResponse> getInvoiceByNumber(@PathVariable("invoiceNumber") String invoiceNumber) {
        return ApiResponse.<InvoiceResponse>builder()
                .result(invoiceService.getInvoiceByNumber(invoiceNumber))
                .build();
    }

    @GetMapping("/order/{orderId}")
    public ApiResponse<InvoiceResponse> getInvoiceByOrderId(@PathVariable("orderId") Long orderId) {
        return ApiResponse.<InvoiceResponse>builder()
                .result(invoiceService.getInvoiceByOrderId(orderId))
                .build();
    }

    @GetMapping
    public ApiResponse<List<InvoiceResponse>> getAllInvoices() {
        return ApiResponse.<List<InvoiceResponse>>builder()
                .result(invoiceService.getAllInvoices())
                .build();
    }

    @GetMapping("/tax-profile/{taxProfileId}")
    public ApiResponse<List<InvoiceResponse>> getInvoicesByTaxProfile(
            @PathVariable("taxProfileId") Long taxProfileId) {
        return ApiResponse.<List<InvoiceResponse>>builder()
                .result(invoiceService.getInvoicesByTaxProfile(taxProfileId))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<InvoiceResponse> updateInvoice(
            @PathVariable("id") Long id,
            @Valid @RequestBody InvoiceUpdateRequest request) {
        return ApiResponse.<InvoiceResponse>builder()
                .result(invoiceService.updateInvoice(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteInvoice(@PathVariable("id") Long id) {
        invoiceService.deleteInvoice(id);
        return ApiResponse.<String>builder()
                .result("Xóa hóa đơn thành công")
                .build();
    }
}
