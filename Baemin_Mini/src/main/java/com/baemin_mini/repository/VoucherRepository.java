package com.baemin_mini.repository;

import com.baemin_mini.domain.entity.Voucher;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    Optional<Voucher> findByCodeIgnoreCase(String code);

    Optional<Voucher> findByCodeIgnoreCaseAndIsActiveTrue(String code);

    boolean existsByCodeIgnoreCase(String code);

    List<Voucher> findByOrderByCreatedAtDesc();

    List<Voucher> findByIsActiveTrueAndIsPublicTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByEndDateAsc(
            LocalDateTime startDate,
            LocalDateTime endDate);
}