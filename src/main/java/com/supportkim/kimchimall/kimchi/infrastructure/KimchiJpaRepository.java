package com.supportkim.kimchimall.kimchi.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

public interface KimchiJpaRepository extends JpaRepository<KimchiEntity , Long> {
    KimchiEntity findByName(String name);
}
