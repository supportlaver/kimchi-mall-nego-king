package com.supportkim.kimchimall.cart.service;

import com.supportkim.kimchimall.cart.controller.port.CartService;
import com.supportkim.kimchimall.cart.domain.Cart;
import com.supportkim.kimchimall.cart.infrastructure.stream.CartOrderEventMessage;
import com.supportkim.kimchimall.cart.infrastructure.stream.CartPaymentMessagePublisher;
import com.supportkim.kimchimall.cart.service.port.CartRepository;
import com.supportkim.kimchimall.cartkimchi.domain.CartKimchi;
import com.supportkim.kimchimall.cartkimchi.service.port.CartKimchiRepository;
import com.supportkim.kimchimall.common.util.ClassUtils;
import com.supportkim.kimchimall.kimchi.domain.Kimchi;
import com.supportkim.kimchimall.kimchi.service.port.KimchiRepository;
import com.supportkim.kimchimall.member.controller.port.MemberService;
import com.supportkim.kimchimall.member.domain.Member;
import com.supportkim.kimchimall.member.service.port.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.supportkim.kimchimall.cart.controller.request.CartRequestDto.*;
import static com.supportkim.kimchimall.cart.controller.response.CartResponseDto.*;

@Service @Builder @Slf4j
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final KimchiRepository kimchiRepository;
    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private final CartKimchiRepository cartKimchiRepository;
    private final TransactionTemplate transactionTemplate;

    /**
     * Publisher 를 사용하려면 R2DBCRepository 와 적합해보인다.
     */
    private final StreamBridge streamBridge;

    @Override
    @Transactional
    public AddToCartResponse addToCart(int quantity , Long kimchiId, HttpServletRequest request) {
        Kimchi kimchi = kimchiRepository.findById(kimchiId);

        // 로그인 시
        Member member = ClassUtils.getSafeCastInstance(request.getAttribute("member"), Member.class);

        // 장바구니에서 Order (결제하기) 를 누르게 되면 그떄 Kimchi 를 OrderKimchi 로 만들고 결제 로직 시작
        // kimchi 를 CartKimchi 로 만들기 + List<CartKimchiEntity> 에 추가
        CartKimchi cartKimchi = cartKimchiRepository.save(CartKimchi.of(kimchi,quantity),member.getCart());

        // Domain 에도 적용
        member.getCart().getCartKimchis().add(cartKimchi);

        // TODO: 9/9/24 UI 가 나오고 나면 비 로그인시 일때는 로컬 스토리지에 상품들을 보관했다가 로그인하면 그대로 옮길 수 있는 로직이 있어야 합니다.
        return AddToCartResponse.of(kimchi);
    }

    /**
     * Kafka 에 CartOrderEventMessage 를 전달
     * 이 메시지는 Order-Service 에서 처리한다.
     * SAGA Pattern 적용 -> MSA 환경에서 분산 트랜잭션을 관리하는데 자주 사용
     * 만약 로직이 성공적으로 완료되지 않았다면 보상 트랜잭션을 실행
     * Order 생성 -> 결제 시도 -> 결제 실패 or 취소 시 보상 트랜잭션으로 Order 삭제
     */
    @Override
    public String requestPayment(HttpServletRequest request) {
        Member member = ClassUtils.getSafeCastInstance(request.getAttribute("member"), Member.class);
        Cart cart = member.getCart();
        List<CartKimchi> cartKimchis = cart.getCartKimchis();

        Map<Long, Integer> kimchiQuantityMap = cartKimchis.stream()
                .collect(Collectors.toMap(
                        cartKimchi -> cartKimchi.getKimchi().getId(), // key: kimchi.id
                        CartKimchi::getQuantity // value: quantity
                ));

        CartOrderEventMessageDto messageDto = CartOrderEventMessageDto.of(member.getMemberId(), cart.getId(), kimchiQuantityMap);

        CartOrderEventMessage cartOrderEventMessage = createCartOrderEventMessage(messageDto);
        // orderService 에서 Order 생성
        streamBridge.send("cart-order" , cartOrderEventMessage);

        // paymentService 에서 결제 로직 실행
        // 실패시 : PaymentFailedEvent 를 발행하여 생성한 Order 를 취소 처리
        // 성공시 : PaymentSuccessEvent 를 발행하여 생성한 Order 를 성공 처리
        streamBridge.send("orders-out-0", cartOrderEventMessage);
        return "OK";
    }


    private CartOrderEventMessage createCartOrderEventMessage(CartOrderEventMessageDto messageDto) {
        return CartOrderEventMessage
                .builder()
                .cartId(messageDto.getCartId())
                .buyerId(messageDto.getBuyerId())
                .seed(messageDto.getSeed())
                .productIdsToQuantities(messageDto.getProductIdsToQuantities())
                .idempotencyKey(messageDto.getIdempotencyKey())
                .build();
    }

}
