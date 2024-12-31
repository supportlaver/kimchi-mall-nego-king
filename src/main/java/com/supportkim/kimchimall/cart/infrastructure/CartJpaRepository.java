package com.supportkim.kimchimall.cart.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CartJpaRepository extends JpaRepository<CartEntity, Long> {
}

