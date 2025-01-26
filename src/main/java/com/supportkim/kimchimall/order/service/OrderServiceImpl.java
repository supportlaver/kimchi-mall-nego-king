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

        List<CartKimchi> cartKimchis = cart.getCartKimchis();

        List<OrderKimchi> orderKimchis = cartKimchis.stream().map(OrderKimchi::from).toList();
        int orderTotalPrice = orderKimchis.stream()
                .mapToInt(OrderKimchi::getPrice)
                .sum();
        Order order = Order.of(orderKimchis, member , orderTotalPrice);

        Order createOrder = orderRepository.save(order);

        return OrderResponse.of(createOrder,member);
    }

    @Override
    @Transactional
    public OrderResponse orderImmediately(OrderRequestImmediately requestDto,HttpServletRequest request) throws InterruptedException {

        Member member = ClassUtils.getSafeCastInstance(request.getAttribute("member"), Member.class);

        Optional<Kimchi> cacheKimchi = kimchiCacheRepository.getKimchi(requestDto.getKimchiName());

        if (cacheKimchi.isEmpty()) {
            log.error("CACHE 비어있음");
            return OrderResponse.builder().build();
        }

        Order createOrder = savedOrder(requestDto, member, cacheKimchi);

        if (!successPayment(createOrder)) {
            throw new BaseException(ErrorCode.PAYMENT_ERROR);
        }

        Delivery delivery = Delivery.from(member);
        deliveryService.save(delivery);

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

    private Order savedOrder(OrderRequestImmediately requestDto, Member member, Optional<Kimchi> cacheKimchi) {
        int totalPrice = cacheKimchi.get().getPrice() * requestDto.getCount();
        OrderKimchi orderKimchi = OrderKimchi.builder()
                .price(totalPrice)
                .kimchi(cacheKimchi.get())
                .quantity(requestDto.getCount())
                .build();
        Order order = Order.of(List.of(orderKimchi), member, totalPrice);
        return orderRepository.save(order);
    }


}
