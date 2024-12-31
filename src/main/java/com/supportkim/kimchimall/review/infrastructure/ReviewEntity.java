package com.supportkim.kimchimall.review.infrastructure;

import com.supportkim.kimchimall.common.global.BaseEntity;
import com.supportkim.kimchimall.kimchi.infrastructure.KimchiEntity;
import jakarta.persistence.*;
import lombok.*;

import static jakarta.persistence.FetchType.*;

@Entity
@Table(name = "reviews")
@Getter @Builder
@AllArgsConstructor(access= AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewEntity extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reviews_id")
    private Long id;

    private String comment;
    private int score;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "kimchi_id")
    private KimchiEntity kimchi;
}
