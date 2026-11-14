package com.example.cf_shop_management.repository;

import com.example.cf_shop_management.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByAccountIdOrderByReservationTimeDesc(Long accountId);

    List<Reservation> findByStatusOrderByReservationTimeAsc(String status);

    List<Reservation> findByReservationTimeBetween(LocalDateTime start, LocalDateTime end);
}
