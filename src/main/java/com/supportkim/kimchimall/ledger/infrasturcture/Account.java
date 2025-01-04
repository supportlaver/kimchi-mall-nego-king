package com.supportkim.kimchimall.ledger.infrasturcture;

import jakarta.persistence.*;
import lombok.Getter;

@Entity @Getter
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String name;
}
