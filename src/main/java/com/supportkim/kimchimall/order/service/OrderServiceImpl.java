package com.supportkim.kimchimall.order.service;

import com.supportkim.kimchimall.cart.domain.Cart;
import com.supportkim.kimchimall.cart.service.port.CartRepository;
import com.supportkim.kimchimall.cartkimchi.domain.CartKimchi;
import com.supportkim.kimchimall.common.exception.BaseException;
import com.supportkim.kimchimall.common.exception.ErrorCode;
import com.supportkim.kimchimall.common.security.jwt.JwtService;
import com.supportkim.kimchimall.common.util.ClassUtils;
import com.supportkim.kimchimall.delivery.controller.port.DeliveryService;
import com.supportkim.kimchimall.delivery.domain.Delivery;
import com.supportkim.kimchimall.kimchi.domain.Kimchi;
import com.supportkim.kimchimall.kimchi.infrastructure.KimchiCacheRepository;
import com.supportkim.kimchimall.kimchi.service.port.KimchiRepository;
import com.supportkim.kimchimall.member.domain.Member;
import com.supportkim.kimchimall.order.controller.port.OrderService;
import com.supportkim.kimchimall.order.domain.Order;
import com.supportkim.kimchimall.order.service.port.OrderRepository;
import com.supportkim.kimchimall.orderkimchi.domain.OrderKimchi;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.core.env.Environment;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

import static com.supportkim.kimchimall.cart.controller.request.CartRequestDto.*;
import static com.supportkim.kimchimall.order.controller.request.OrderRequestDto.*;
import static com.supportkim.kimchimall.order.controller.response.OrderResponseDto.*;

@Service @Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final JwtService jwtService;
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final KimchiRepository kimchiRepository;
    private final StreamBridge streamBridge;
    private final KimchiCacheRepository kimchiCacheRepository;
    private final Environment environment;
    private final MockService mockService;
    private final DeliveryService deliveryService;

    @Override
    public Mono<String> order(CartOrderEventMessageDto message) {
        // message 에 있는 값들을 가지고 Order 를 만든다.
        return Mono.just("OK");
    }

    @Override
    @Transactional
    public OrderResponse onlyOrder(HttpServletRequest request) {
        Member member = ClassUtils.getSafeCastInstance(request.getAttribute("member"), Member.class);

        Cart cart = member.getCart();
        // cartKimchis 안에 있는 CartKimchi 를 OrderKimchi 들로 만들고 Order 객체 만들기

        // 1. cartKimchis -> OrderKimchi 로 만들기 (Cart 와 CartKimchi 는 모두 없애기)
        List<CartKimchi> cartKimchis = cart.getCartKimchis();

        List<OrderKimchi> orderKimchis = cartKimchis.stream().map(OrderKimchi::from).toList();
        int orderTotalPrice = orderKimchis.stream()
                .mapToInt(OrderKimchi::getPrice)
                .sum();

        // 장바구니 비우기
        // cart.clear();

        log.info("orderTotalPrice = {} " , orderTotalPrice);

        // 실제 Entity 에 적용 해서 데이터 동기화
        // cartRepository.save(cart);


        // 2. OrderKimchi -> Order 로 만들고 DB 저장
        Order order = Order.of(orderKimchis, member , orderTotalPrice);
        log.info("order ={} " , order.getMember());

        Order createOrder = orderRepository.save(order);

        return OrderResponse.of(createOrder,member);
    }

    @Override
    @Transactional
    public OrderResponse orderImmediately(OrderRequestImmediately requestDto,HttpServletRequest request) throws InterruptedException {

        Member member = ClassUtils.getSafeCastInstance(request.getAttribute("member"), Member.class);

        Optional<Kimchi> cacheKimchi = kimchiCacheRepository.getKimchi(requestDto.getKimchiName());

        // Cache 에서 우선 확인하고 없다면 그때 DB 에서 조회
        if (cacheKimchi.isEmpty()) {
            log.error("CACHE 비어있음");
            return OrderResponse.builder().build();
        }

        // 1. 주문 저장
        Order createOrder = savedOrder(requestDto, member, cacheKimchi);

        // 2. 결제 시작
        if (!successPayment(createOrder)) {
            throw new BaseException(ErrorCode.PAYMENT_ERROR);
        }

        // 3. 배송 정보 저장
        Delivery delivery = Delivery.from(member);
        deliveryService.save(delivery);

        // 4. 상품 재고 감소 (상품 재고가 있는지 확인)
        Kimchi kimchi = cacheKimchi.get();

        Kimchi findKimchi = kimchiRepository.findById(kimchi.getId());

        if (findKimchi.getQuantity() - requestDto.getCount() < 0) {
            throw new BaseException(ErrorCode.KIMCHI_OUT_OF_STOCK_EXCEPTION);
        }

        findKimchi.decreaseQuantity(requestDto.getCount());
        kimchiRepository.save(kimchi);

        return OrderResponse.of(createOrder , member);
    }

    private boolean successPayment(Order createOrder) {
        return mockService.payment(createOrder);
    }

    /*private OrderResponse savedOrderWithKafka(Member member) {
        streamBridge.send("orders" , OrderEventMessage
                .of(member.getMemberId(),List.of(orderKimchi), totalPrice++ , UUID.randomUUID().toString()));
        return OrderResponse.ordering(totalPrice, member);
    }*/

    private Order savedOrder(OrderRequestImmediately requestDto, Member member, Optional<Kimchi> cacheKimchi) {
        int totalPrice = cacheKimchi.get().getPrice() * requestDto.getCount();
        OrderKimchi orderKimchi = OrderKimchi.builder()
                .price(totalPrice)
                .kimchi(cacheKimchi.get())
                .quantity(requestDto.getCount())
                .build();

        // Order 저장하는 부분은 Kafka 에게 전달
        Order order = Order.of(List.of(orderKimchi), member, totalPrice);
        return orderRepository.save(order);
    }


}
