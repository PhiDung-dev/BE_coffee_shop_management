package com.example.cf_shop_management.mapper;

import com.example.cf_shop_management.dto.request.InvoiceCreateRequest;
import com.example.cf_shop_management.dto.request.InvoiceUpdateRequest;
import com.example.cf_shop_management.dto.response.InvoiceResponse;
import com.example.cf_shop_management.entity.Invoice;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InvoiceMapper {

    Invoice toInvoice(InvoiceCreateRequest request);

    @Mapping(source = "order.id", target = "orderId")
    @Mapping(source = "order.orderCode", target = "orderCode")
    @Mapping(source = "order.finalAmount", target = "finalAmount")
    @Mapping(source = "taxProfile.id", target = "taxProfileId")
    @Mapping(source = "taxProfile.companyName", target = "companyName")
    @Mapping(source = "taxProfile.taxCode", target = "taxCode")
    @Mapping(source = "taxProfile.companyAddress", target = "companyAddress")
    InvoiceResponse toInvoiceResponse(Invoice invoice);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateInvoice(InvoiceUpdateRequest request, @MappingTarget Invoice invoice);
}
