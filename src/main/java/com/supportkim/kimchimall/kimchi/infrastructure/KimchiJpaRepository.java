package com.supportkim.kimchimall.kimchi.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KimchiJpaRepository extends JpaRepository<KimchiEntity , Long> {
    KimchiEntity findByName(String name);

    List<KimchiEntity> findAllByIdIn(List<Long> ids);
}
