package com.supportkim.kimchimall.member.domain;


import com.supportkim.kimchimall.cart.domain.Cart;
import com.supportkim.kimchimall.common.security.jwt.TokenMapping;
import com.supportkim.kimchimall.member.infrastructure.Address;
import com.supportkim.kimchimall.order.domain.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Member {
    private Long memberId;
    private String loginId;
    private String password;
    private Address address;
    private String email;
    private String phoneNumber;
    private String name;
    private Cart cart;
    private List<Order> orders = new ArrayList<>();
    private TokenMapping tokenMapping;
    private String refreshToken;
    private String role;

    // Refresh 토큰 설정
    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    // 장바구니 연관관계 편의 메서드
    public void settingCart(Cart cart) {
        this.cart = cart;
    }

    @Override
    public String toString() {
        return "Member{" +
                "id=" + memberId +
                ", loginId='" + loginId + '\'' +
                ", password='" + password + '\'' +
                ", address=" + address +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", name='" + name + '\'' +
                ", cart=" + cart +
                ", orders=" + orders +
                ", refreshToken='" + refreshToken + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
