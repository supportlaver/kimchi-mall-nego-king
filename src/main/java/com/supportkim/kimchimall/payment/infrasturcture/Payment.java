package com.supportkim.kimchimall.payment.infrasturcture;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payment_event")
@Getter
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long buyerId;

    @Column(nullable = false)
    private String orderName;

    @Column(nullable = false, unique = true)
    private String orderId;

    @Column
    private String paymentKey;

    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Column
    private LocalDateTime approvedAt;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "paymentEvent", orphanRemoval = true)
    private List<PaymentOrder> paymentOrders = new ArrayList<>();

    @Column(nullable = false)
    private boolean isPaymentDone = false;
}
