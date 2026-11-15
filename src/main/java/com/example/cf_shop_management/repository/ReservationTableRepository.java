package com.example.cf_shop_management.repository;

import com.example.cf_shop_management.entity.ReservationTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationTableRepository extends JpaRepository<ReservationTable, Long> {

    List<ReservationTable> findByReservationId(Long reservationId);

    void deleteByReservationId(Long reservationId);
}