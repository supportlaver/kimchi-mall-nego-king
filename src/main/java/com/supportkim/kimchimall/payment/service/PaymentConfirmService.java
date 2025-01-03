package com.supportkim.kimchimall.payment.service;

import com.supportkim.kimchimall.common.exception.BaseException;
import com.supportkim.kimchimall.common.exception.ErrorCode;
import com.supportkim.kimchimall.common.exception.PaymentAlreadyProcessedException;
import com.supportkim.kimchimall.common.exception.PaymentValidationException;
import com.supportkim.kimchimall.common.util.Pair;
import com.supportkim.kimchimall.payment.controller.response.PaymentConfirmationResult;
import com.supportkim.kimchimall.payment.infrasturcture.*;
import com.supportkim.kimchimall.payment.service.dto.PaymentConfirmCommand;
import com.supportkim.kimchimall.payment.service.dto.PaymentExecutionResult;
import com.supportkim.kimchimall.payment.service.dto.PaymentStatusUpdateCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentConfirmService {

    private final PaymentEventJpaRepository paymentEventRepository;
    private final PaymentOrderJpaRepository paymentOrderRepository;
    private final PaymentOrderHistoryJpaRepository paymentOrderHistoryRepository;
    private final TossPaymentExecutor tossPaymentExecutor;


    @Transactional
    public PaymentConfirmationResult confirm(PaymentConfirmCommand command) {
        // 1. 결제 상태를 EXECUTING 업데이트
        PaymentEvent paymentEvent = paymentEventRepository.findByOrderId(command.getOrderId())
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND_PAYMENT_EVENT));

        List<Pair<Long, String>> result = checkPreviousPaymentOrderStatus(paymentEvent.getOrderId());

        // 엔터티 리스트 생성
        List<PaymentOrderHistory> paymentHistories = result.stream()
                .map(pair -> PaymentOrderHistory.builder()
                        .paymentOrderId(pair.getKey()) // Order ID
                        .previousStatus(PaymentStatus.get(pair.getValue())) // 이전 상태
                        .newStatus(PaymentStatus.EXECUTING) // 새로운 상태
                        .reason("PAYMENT_CONFIRMATION_START") // 변경 이유
                        .createdAt(LocalDateTime.now())
                        .build())
                .collect(Collectors.toList());

        // 데이터 저장
        paymentOrderHistoryRepository.saveAll(paymentHistories);

        List<PaymentOrder> paymentOrders = paymentOrderRepository.findByOrderId(command.getOrderId());

        paymentOrders.forEach(paymentOrder -> paymentOrder.updateOrderStatus(PaymentStatus.EXECUTING));

        // PaymentEvent 에서 PaymentKey 업데이트 (TossAPI 에서 발급해준 Key)
        paymentEvent.updatePaymentKey(command.getPaymentKey());


        // 2. 결제 유효성 검증 (결제 금액을 비교하여 검증)
        Integer amount = paymentOrderRepository.findTotalAmountByOrderId(command.getOrderId());
        // 만약 문제가 있다면 예외 발생
        isValid(amount, command.getAmount(), command.getOrderId());

        // 3. 결제 실행
        PaymentExecutionResult paymentExecutionResult = tossPaymentExecutor.execute(command);


        // 4. 결제 상태 업데이트
        PaymentStatusUpdateCommand paymentStatusUpdateCommand = PaymentStatusUpdateCommand.from(paymentExecutionResult);
        updatePaymentStatus(paymentStatusUpdateCommand);

        // 5. 결과 반환
        return new PaymentConfirmationResult(paymentExecutionResult.paymentStatus(), paymentExecutionResult.getFailure());
    }

    private boolean updatePaymentStatus(PaymentStatusUpdateCommand command) {
        return switch (command.getStatus()) {
            case SUCCESS -> updatePaymentStatusToSuccess(command);
            case FAILURE -> updatePaymentStatusToFailure(command);
            case UNKNOWN -> updatePaymentStatusToUnknown(command);
            default -> throw new IllegalArgumentException(
                    "결제 상태 (status: " + command.getStatus() + ") 는 올바르지 않은 결제 상태입니다."
            );
        };
    }

    public boolean updatePaymentStatusToUnknown(PaymentStatusUpdateCommand command) {
        // Step 1: PaymentOrder 상태 가져오기
        List<Object[]> paymentOrderStatusList = paymentOrderRepository.findPaymentOrderStatusByOrderId(command.getOrderId());

        // Step 2: PaymentOrder 상태를 PaymentHistory로 저장
        insertPaymentHistory(paymentOrderStatusList, command.getStatus(), command.getFailure().toString());

        // Step 3: PaymentOrder 상태 업데이트
        updatePaymentOrderStatus(command.getOrderId(), command.getStatus());

        // Step 4: PaymentOrder 실패 카운트 증가
        // incrementPaymentOrderFailedCount(command);
        return true;
    }

    public boolean updatePaymentStatusToFailure(PaymentStatusUpdateCommand command) {
        // Step 1: PaymentOrder 상태 가져오기
        List<Object[]> paymentOrderStatusList = paymentOrderRepository.findPaymentOrderStatusByOrderId(command.getOrderId());

        // Step 2: PaymentOrder 상태를 PaymentHistory로 저장
        insertPaymentHistory(paymentOrderStatusList, command.getStatus(), command.getFailure().toString());

        // Step 3: PaymentOrder 상태 업데이트
        updatePaymentOrderStatus(command.getOrderId(), command.getStatus());

        return true;
    }

    public boolean updatePaymentStatusToSuccess(PaymentStatusUpdateCommand command) {
        // Step 1: PaymentOrder 상태 가져오기
        List<Object[]> paymentOrderStatusList = paymentOrderRepository.findPaymentOrderStatusByOrderId(command.getOrderId());

        // Step 2: PaymentOrder 상태를 PaymentHistory로 저장
        insertPaymentHistory(paymentOrderStatusList, command.getStatus(), "PAYMENT_CONFIRMATION_DONE");

        // Step 3: PaymentOrder 상태 업데이트
        updatePaymentOrderStatus(command.getOrderId(), command.getStatus());

        // Step 4: PaymentEvent 추가 정보 업데이트
        updatePaymentEventExtraDetails(command);

        return true;
    }

    private void insertPaymentHistory(List<Object[]> paymentOrderStatusList, PaymentStatus status, String reason) {
        List<PaymentOrderHistory> historyList = paymentOrderStatusList.stream()
                .map(record -> PaymentOrderHistory.of(
                        (Long) record[0], // paymentOrderId
                        ((PaymentStatus) record[1]), // 이전 상태를 String으로 변환
                        status, // 새 상태를 String으로 변환
                        reason // 변경 이유
                ))
                .collect(Collectors.toList());
        paymentOrderHistoryRepository.saveAll(historyList);
    }

    private void updatePaymentOrderStatus(String orderId, PaymentStatus status) {
        int updatedCount = paymentOrderRepository.updateStatusByOrderId(orderId, status);
        if (updatedCount == 0) {
            throw new IllegalStateException("No PaymentOrder records updated for orderId: " + orderId);
        }
    }

    private void updatePaymentEventExtraDetails(PaymentStatusUpdateCommand command) {
        PaymentEvent paymentEvent = paymentEventRepository.findByOrderId(command.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("PaymentEvent not found for orderId: " + command.getOrderId()));

        paymentEvent.updateExtraDetails(command.getExtraDetails());
        paymentEventRepository.save(paymentEvent);
    }

    private List<Pair<Long, String>> checkPreviousPaymentOrderStatus(String orderId) {
        // 데이터베이스에서 상태 조회
        List<Object[]> result = paymentOrderRepository.findPaymentOrderStatusByOrderId(orderId);

        for (Object[] objects : result) {
            System.out.println("JIWON " + objects[0]);
            System.out.println(objects[1].getClass());
        }


        // Object[]를 Pair<Long, String>으로 매핑
        List<Pair<Long, String>> pairs = result.stream()
                .map(row -> new Pair<>((Long) row[0], ((PaymentStatus) row[1]).name())) // PaymentStatus → String 변환
                .toList();

        // 상태 검증 및 필터링
        pairs.forEach(pair -> {
            String status = pair.getValue(); // getRight() 대신 getValue() 사용
            if (PaymentStatus.SUCCESS.name().equals(status)) {
                throw new PaymentAlreadyProcessedException(
                        "이미 처리 성공한 결제 입니다.",
                        PaymentStatus.SUCCESS
                );
            } else if (PaymentStatus.FAILURE.name().equals(status)) {
                throw new PaymentAlreadyProcessedException(
                        "이미 처리 실패한 결제 입니다.",
                        PaymentStatus.FAILURE
                );
            }
        });

        // 처리 가능한 상태만 반환
        return pairs.stream()
                .filter(pair -> PaymentStatus.NOT_STARTED.name().equals(pair.getValue()) ||
                        PaymentStatus.UNKNOWN.name().equals(pair.getValue()) ||
                        PaymentStatus.EXECUTING.name().equals(pair.getValue()))
                .collect(Collectors.toList());
    }

    private void isValid(int findAmount, int requestAmount, String orderId) {
        if (findAmount != requestAmount) {
            throw new PaymentValidationException(String.format("결제 (orderId: %s) 에서 금액 (amount: %d) 이 올바르지 않습니다.", orderId, findAmount));
        }
    }

}
