package com.example.cf_shop_management.mapper;

import com.example.cf_shop_management.dto.request.ReservationCreateRequest;
import com.example.cf_shop_management.dto.request.ReservationUpdateRequest;
import com.example.cf_shop_management.dto.response.ReservationResponse;
import com.example.cf_shop_management.entity.Reservation;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReservationMapper {

    Reservation toReservation(ReservationCreateRequest request);

    @Mapping(source = "account.id", target = "accountId")
    @Mapping(source = "account.email", target = "accountEmail")
    @Mapping(source = "account.phone", target = "accountPhone")
    ReservationResponse toReservationResponse(Reservation reservation);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateReservation(ReservationUpdateRequest request, @MappingTarget Reservation reservation);
}
