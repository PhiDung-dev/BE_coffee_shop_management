package com.example.cf_shop_management.mapper;

import com.example.cf_shop_management.dto.request.TablesCreateRequest;
import com.example.cf_shop_management.dto.request.TablesUpdateRequest;
import com.example.cf_shop_management.dto.response.TablesResponse;
import com.example.cf_shop_management.entity.Tables;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TablesMapper {

    Tables toTables(TablesCreateRequest request);

    @Mapping(source = "area.id", target = "areaId")
    @Mapping(source = "area.name", target = "areaName")
    TablesResponse toTablesResponse(Tables tables);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateTables(TablesUpdateRequest request, @MappingTarget Tables tables);
}
