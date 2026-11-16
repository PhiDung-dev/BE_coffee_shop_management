package com.example.cf_shop_management.mapper;

import com.example.cf_shop_management.dto.request.InvoiceCreateRequest;
import com.example.cf_shop_management.dto.request.InvoiceUpdateRequest;
import com.example.cf_shop_management.dto.response.InvoiceResponse;
import com.example.cf_shop_management.entity.Invoice;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface InvoiceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "invoiceNumber", ignore = true)
    @Mapping(target = "issuedAt", ignore = true)
    @Mapping(target = "pdfUrl", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "taxProfile", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Invoice toInvoice(InvoiceCreateRequest request);

    @Mapping(source = "order.id", target = "orderId")
    @Mapping(source = "order.orderCode", target = "orderCode")
    @Mapping(source = "order.finalAmount", target = "orderFinalAmount")
    @Mapping(source = "taxProfile.id", target = "taxProfileId")
    @Mapping(source = "taxProfile.companyName", target = "companyName")
    @Mapping(source = "taxProfile.taxCode", target = "taxCode")
    @Mapping(source = "taxProfile.companyAddress", target = "companyAddress")
    InvoiceResponse toInvoiceResponse(Invoice invoice);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "invoiceNumber", ignore = true)
    @Mapping(target = "issuedAt", ignore = true)
    @Mapping(target = "pdfUrl", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "taxProfile", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateInvoice(InvoiceUpdateRequest request, @MappingTarget Invoice invoice);
}