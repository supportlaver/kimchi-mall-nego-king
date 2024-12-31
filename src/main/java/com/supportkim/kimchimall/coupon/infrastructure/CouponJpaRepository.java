package com.supportkim.kimchimall.coupon.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponJpaRepository extends JpaRepository<CouponEntity , Long> {
}
