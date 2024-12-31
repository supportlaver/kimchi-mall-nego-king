package com.supportkim.kimchimall.notification.infrastructure;

import com.supportkim.kimchimall.common.global.BaseEntity;
import com.supportkim.kimchimall.member.infrastructure.MemberEntity;
import jakarta.persistence.*;
import lombok.*;

import static jakarta.persistence.FetchType.*;

@Entity
@Table(name = "notifications")
@Getter
@Builder
@AllArgsConstructor(access= AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationEntity extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "notification_id")
    private Long id;

    private String message;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private MemberEntity member;
}
