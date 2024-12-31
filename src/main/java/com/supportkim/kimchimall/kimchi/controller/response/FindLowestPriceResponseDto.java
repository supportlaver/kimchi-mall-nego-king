package com.supportkim.kimchimall.kimchi.controller.response;

import com.supportkim.kimchimall.kimchi.controller.port.FindLowestPriceService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.cache.spi.support.AbstractReadWriteAccess;

import java.io.Serializable;
import java.util.List;

@Getter @Builder
@NoArgsConstructor @AllArgsConstructor
public class FindLowestPriceResponseDto implements Serializable{
    private int start;
    private int display;
    private List<ItemDto> items;

    public static FindLowestPriceResponseDto from(List<ItemDto> items , int display , int start) {
        return FindLowestPriceResponseDto.builder()
                .display(display)
                .start(start)
                .items(items)
                .build();
    }



    @Getter @Builder
    @NoArgsConstructor @AllArgsConstructor
    public static class ItemDto implements Serializable {
        private String title;
        private String link;
        private String image;
        private String lprice;
        private String hprice;
        private String mallName;
        private String productId;
        private String productType;
        private String brand;
        private String maker;
        private String category1;
        private String category2;
        private String category3;
        private String category4;

        @Override
        public String toString() {
            return "ItemDto{" +
                    "title='" + title + '\'' +
                    ", link='" + link + '\'' +
                    ", lprice='" + lprice + '\'' +
                    ", hprice='" + hprice + '\'' +
                    ", mallName='" + mallName + '\'' +
                    ", productId='" + productId + '\'' +
                    ", productType='" + productType + '\'' +
                    ", brand='" + brand + '\'' +
                    ", maker='" + maker + '\'' +
                    '}';
        }
    }
}
