package com.example.cf_shop_management.mapper;

import com.example.cf_shop_management.dto.request.AreaCreateRequest;
import com.example.cf_shop_management.dto.request.AreaUpdateRequest;
import com.example.cf_shop_management.dto.response.AreaResponse;
import com.example.cf_shop_management.entity.Area;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface AreaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Area toArea(AreaCreateRequest request);

    AreaResponse toAreaResponse(Area area);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateArea(AreaUpdateRequest request, @MappingTarget Area area);
}