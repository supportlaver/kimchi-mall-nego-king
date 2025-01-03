package com.supportkim.kimchimall.kimchi.infrastructure;



import com.supportkim.kimchimall.cartkimchi.infrastructure.CartKimchiEntity;
import com.supportkim.kimchimall.common.global.BaseEntity;
import com.supportkim.kimchimall.kimchi.domain.Kimchi;
import com.supportkim.kimchimall.kimchi.domain.KimchiType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.EnumType.*;
import static jakarta.persistence.FetchType.*;

@Entity
@Table(name = "kimchis")
@Getter @Builder
@AllArgsConstructor(access= AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KimchiEntity extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kimchi_id")
    private Long id;

    private String name;
    private int price;

    @Enumerated(value = STRING)
    private KimchiType type;

    @OneToMany(cascade = ALL , mappedBy = "kimchi")
    private List<CartKimchiEntity> cartKimchiEntityList = new ArrayList<>();
    private int quantity;
    private Long sellerId;

/*    @Version
    private int version;*/

    public static KimchiEntity from(Kimchi kimchi) {
        return KimchiEntity.builder()
                .id(kimchi.getId())
                .name(kimchi.getName())
                .type(kimchi.getType())
                .price(kimchi.getPrice())
                .quantity(kimchi.getQuantity())
                .build();
    }

    public Kimchi toModel() {
        return Kimchi.builder()
                .id(id)
                .type(type)
                .name(name)
                .quantity(quantity)
                .price(price)
                .build();
    }
}
