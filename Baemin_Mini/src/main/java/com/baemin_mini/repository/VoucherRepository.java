package com.baemin_mini.repository;

import com.baemin_mini.domain.entity.Voucher;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    
    Optional<Voucher> findByCodeAndIsActiveTrue(String code);

    @Query("SELECT v FROM Voucher v WHERE v.isActive = true AND v.startDate <= :now AND v.endDate >= :now")
    List<Voucher> findValidVouchers(@Param("now") LocalDateTime now);
}
