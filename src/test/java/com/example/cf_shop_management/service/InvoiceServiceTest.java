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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private TaxProfileRepository taxProfileRepository;

    @Mock
    private InvoiceMapper invoiceMapper;

    @InjectMocks
    private InvoiceService invoiceService;

    private Order order;
    private TaxProfile taxProfile;
    private Invoice invoice;
    private InvoiceResponse invoiceResponse;

    @BeforeEach
    void setUp() {
        order = Order.builder()
                .id(10L)
                .orderCode("ORD-20261116-XYZ123")
                .status("COMPLETED")
                .finalAmount(new BigDecimal("120000.00"))
                .build();

        taxProfile = TaxProfile.builder()
                .id(5L)
                .companyName("Công ty TNHH Công Nghệ ABC")
                .taxCode("0401234567")
                .companyAddress("Đà Nẵng, Việt Nam")
                .build();

        invoice = Invoice.builder()
                .id(1L)
                .invoiceNumber("INV-20261116-A1B2C3")
                .issuedAt(LocalDateTime.of(2026, 11, 16, 14, 30))
                .order(order)
                .taxProfile(taxProfile)
                .build();

        invoiceResponse = InvoiceResponse.builder()
                .id(1L)
                .invoiceNumber("INV-20261116-A1B2C3")
                .orderId(10L)
                .orderCode("ORD-20261116-XYZ123")
                .taxProfileId(5L)
                .companyName("Công ty TNHH Công Nghệ ABC")
                .taxCode("0401234567")
                .build();
    }

    @Test
    void createInvoice_WithTaxProfile_Success() {
        InvoiceCreateRequest request = InvoiceCreateRequest.builder()
                .orderId(10L)
                .taxProfileId(5L)
                .build();

        Invoice mappedInvoice = new Invoice();

        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(invoiceRepository.existsByOrderId(10L)).thenReturn(false);
        when(taxProfileRepository.findById(5L)).thenReturn(Optional.of(taxProfile));
        when(invoiceMapper.toInvoice(request)).thenReturn(mappedInvoice);
        when(invoiceRepository.existsByInvoiceNumberIgnoreCase(anyString())).thenReturn(false);
        when(invoiceRepository.save(mappedInvoice)).thenReturn(invoice);
        when(invoiceMapper.toInvoiceResponse(invoice)).thenReturn(invoiceResponse);

        InvoiceResponse result = invoiceService.createInvoice(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(order, mappedInvoice.getOrder());
        assertEquals(taxProfile, mappedInvoice.getTaxProfile());
        assertEquals(invoice, order.getInvoice());
        assertNotNull(mappedInvoice.getInvoiceNumber());
        assertTrue(mappedInvoice.getInvoiceNumber().startsWith("INV-"));
        assertNotNull(mappedInvoice.getIssuedAt());
        verify(invoiceRepository).save(mappedInvoice);
    }

    @Test
    void createInvoice_WithoutTaxProfile_Success() {
        InvoiceCreateRequest request = InvoiceCreateRequest.builder()
                .orderId(10L)
                .taxProfileId(null)
                .build();

        Invoice mappedInvoice = new Invoice();

        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(invoiceRepository.existsByOrderId(10L)).thenReturn(false);
        when(invoiceMapper.toInvoice(request)).thenReturn(mappedInvoice);
        when(invoiceRepository.existsByInvoiceNumberIgnoreCase(anyString())).thenReturn(false);
        when(invoiceRepository.save(mappedInvoice)).thenReturn(invoice);
        when(invoiceMapper.toInvoiceResponse(invoice)).thenReturn(invoiceResponse);

        InvoiceResponse result = invoiceService.createInvoice(request);

        assertNotNull(result);
        assertNull(mappedInvoice.getTaxProfile());
        verify(taxProfileRepository, never()).findById(any());
    }

    @Test
    void createInvoice_OrderNotFound_ThrowsException() {
        InvoiceCreateRequest request = InvoiceCreateRequest.builder().orderId(99L).build();
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> invoiceService.createInvoice(request));

        assertEquals(ErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void createInvoice_OrderCancelled_ThrowsException() {
        Order cancelledOrder = Order.builder()
                .id(10L)
                .status("CANCELLED")
                .build();

        InvoiceCreateRequest request = InvoiceCreateRequest.builder().orderId(10L).build();
        when(orderRepository.findById(10L)).thenReturn(Optional.of(cancelledOrder));

        AppException exception = assertThrows(AppException.class,
                () -> invoiceService.createInvoice(request));

        assertEquals(ErrorCode.ORDER_CANCELLED_CANNOT_INVOICE, exception.getErrorCode());
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void createInvoice_AlreadyExistedForOrder_ThrowsException() {
        InvoiceCreateRequest request = InvoiceCreateRequest.builder().orderId(10L).build();

        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(invoiceRepository.existsByOrderId(10L)).thenReturn(true);

        AppException exception = assertThrows(AppException.class,
                () -> invoiceService.createInvoice(request));

        assertEquals(ErrorCode.INVOICE_ALREADY_EXISTED_FOR_ORDER, exception.getErrorCode());
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void createInvoice_TaxProfileNotFound_ThrowsException() {
        InvoiceCreateRequest request = InvoiceCreateRequest.builder()
                .orderId(10L)
                .taxProfileId(99L)
                .build();

        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(invoiceRepository.existsByOrderId(10L)).thenReturn(false);
        when(taxProfileRepository.findById(99L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> invoiceService.createInvoice(request));

        assertEquals(ErrorCode.TAX_PROFILE_NOT_FOUND, exception.getErrorCode());
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void getAllInvoices_Paged_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Invoice> page = new PageImpl<>(List.of(invoice), pageable, 1);

        when(invoiceRepository.searchInvoices(eq(10L), eq(5L), any(), any(), eq("INV-2026"), eq(pageable)))
                .thenReturn(page);
        when(invoiceMapper.toInvoiceResponse(invoice)).thenReturn(invoiceResponse);

        Page<InvoiceResponse> result = invoiceService.getAllInvoices(
                10L, 5L, LocalDateTime.now().minusDays(1), LocalDateTime.now(), "  INV-2026  ", pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("INV-20261116-A1B2C3", result.getContent().get(0).getInvoiceNumber());
    }

    @Test
    void getInvoiceById_Success() {
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(invoiceMapper.toInvoiceResponse(invoice)).thenReturn(invoiceResponse);

        InvoiceResponse result = invoiceService.getInvoiceById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void getInvoiceById_NotFound_ThrowsException() {
        when(invoiceRepository.findById(99L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> invoiceService.getInvoiceById(99L));

        assertEquals(ErrorCode.INVOICE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void getInvoiceByOrderId_Success() {
        when(invoiceRepository.findByOrderId(10L)).thenReturn(Optional.of(invoice));
        when(invoiceMapper.toInvoiceResponse(invoice)).thenReturn(invoiceResponse);

        InvoiceResponse result = invoiceService.getInvoiceByOrderId(10L);

        assertEquals(10L, result.getOrderId());
    }

    @Test
    void getInvoiceByOrderId_NotFound_ThrowsException() {
        when(invoiceRepository.findByOrderId(99L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> invoiceService.getInvoiceByOrderId(99L));

        assertEquals(ErrorCode.INVOICE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void getInvoiceByNumber_Success() {
        when(invoiceRepository.findByInvoiceNumberIgnoreCase("INV-20261116-A1B2C3"))
                .thenReturn(Optional.of(invoice));
        when(invoiceMapper.toInvoiceResponse(invoice)).thenReturn(invoiceResponse);

        InvoiceResponse result = invoiceService.getInvoiceByNumber("  INV-20261116-A1B2C3  ");

        assertEquals("INV-20261116-A1B2C3", result.getInvoiceNumber());
    }

    @Test
    void updateInvoice_Success() {
        TaxProfile newTaxProfile = TaxProfile.builder()
                .id(8L)
                .companyName("Công ty Cổ phần Mới")
                .taxCode("0409876543")
                .build();

        InvoiceUpdateRequest request = InvoiceUpdateRequest.builder()
                .taxProfileId(8L)
                .build();

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(taxProfileRepository.findById(8L)).thenReturn(Optional.of(newTaxProfile));
        when(invoiceRepository.save(invoice)).thenReturn(invoice);
        when(invoiceMapper.toInvoiceResponse(invoice)).thenReturn(invoiceResponse);

        InvoiceResponse result = invoiceService.updateInvoice(1L, request);

        assertNotNull(result);
        assertEquals(newTaxProfile, invoice.getTaxProfile());
        verify(invoiceMapper).updateInvoice(request, invoice);
        verify(invoiceRepository).save(invoice);
    }

    @Test
    void deleteInvoice_Success_DetachesFromOrder() {
        order.setInvoice(invoice);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        doNothing().when(invoiceRepository).delete(invoice);

        invoiceService.deleteInvoice(1L);

        assertNull(order.getInvoice());
        verify(invoiceRepository).delete(invoice);
    }

    @Test
    void deleteInvoice_NotFound_ThrowsException() {
        when(invoiceRepository.findById(99L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> invoiceService.deleteInvoice(99L));

        assertEquals(ErrorCode.INVOICE_NOT_FOUND, exception.getErrorCode());
    }
}