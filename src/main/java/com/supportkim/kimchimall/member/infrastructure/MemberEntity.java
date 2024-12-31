package com.supportkim.kimchimall.member.infrastructure;

import com.supportkim.kimchimall.cart.domain.Cart;
import com.supportkim.kimchimall.cart.infrastructure.CartEntity;
import com.supportkim.kimchimall.common.global.BaseEntity;
import com.supportkim.kimchimall.coupon.domain.Coupon;
import com.supportkim.kimchimall.coupon.infrastructure.CouponEntity;
import com.supportkim.kimchimall.member.domain.Member;
import com.supportkim.kimchimall.notification.infrastructure.NotificationEntity;
import com.supportkim.kimchimall.order.infrastructure.OrderEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Lazy;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.FetchType.*;
import static java.util.stream.Collectors.*;

@Entity
@Table(name = "members")
@Getter @Builder
@AllArgsConstructor(access= AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class MemberEntity extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    private String loginId;
    private String password;
    private Address address;
    private String email;
    private String name;
    private String phoneNumber;

    @OneToMany(cascade = ALL , mappedBy = "member")
    private List<OrderEntity> orders = new ArrayList<>();

    @OneToOne(fetch = LAZY , cascade = ALL)
    @JoinColumn(name = "cart_id")
    private CartEntity cart;

    // 회원가입 용(Cart X)
    public static MemberEntity fromForJoin(Member member) {
        return MemberEntity.builder()
                .id(member.getMemberId())
                .loginId(member.getLoginId())
                .password(member.getPassword())
                .address(member.getAddress())
                .email(member.getEmail())
                .phoneNumber(member.getPhoneNumber())
                //.orders(OrderEntity.fromList(member.getOrders()))
                .name(member.getName())
                .build();
    }

    // 회원가입 용(Cart X)
    public static MemberEntity fromForLogin(Member member) {
        log.info("memberCartId = {} " , member.getCart());
        return MemberEntity.builder()
                .id(member.getMemberId())
                .loginId(member.getLoginId())
                .password(member.getPassword())
                .address(member.getAddress())
                .email(member.getEmail())
                .phoneNumber(member.getPhoneNumber())
                .cart(CartEntity.from(member.getCart()))
                //.orders(OrderEntity.fromList(member.getOrders()))
                .name(member.getName())
                .build();
    }

    // 로그인 용(Cart O)

    public Member toModel() {
        return Member.builder()
                .memberId(id)
                .name(name)
                .loginId(loginId)
                .password(password)
                .email(email)
                .phoneNumber(phoneNumber)
                .address(address)
                //.cart(cart.toModel())
                // 지연로딩 -> could not initialize proxy - no Session 오류 발생
                //.orders(orders.stream().map(OrderEntity::toModel).collect(toList()))
                .build();
    }
    public Member toModelForLogin() {
        return Member.builder()
                .memberId(id)
                .name(name)
                .loginId(loginId)
                .password(password)
                .email(email)
                .phoneNumber(phoneNumber)
                .address(address)
                .cart(cart.toModel())
                // 지연로딩 -> could not initialize proxy - no Session 오류 발생
                //.orders(orders.stream().map(OrderEntity::toModel).collect(toList()))
                .build();
    }

}
