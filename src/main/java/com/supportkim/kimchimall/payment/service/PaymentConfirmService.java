package com.supportkim.kimchimall.payment.service;

import com.supportkim.kimchimall.common.exception.BaseException;
import com.supportkim.kimchimall.common.exception.ErrorCode;
import com.supportkim.kimchimall.common.exception.PaymentAlreadyProcessedException;
import com.supportkim.kimchimall.common.exception.PaymentValidationException;
import com.supportkim.kimchimall.common.util.Pair;
import com.supportkim.kimchimall.ledger.infrasturcture.*;
import com.supportkim.kimchimall.ledger.service.LedgerService;
import com.supportkim.kimchimall.member.infrastructure.MemberEntity;
import com.supportkim.kimchimall.member.infrastructure.MemberJpaRepository;
import com.supportkim.kimchimall.payment.controller.request.TossPaymentConfirmTest;
import com.supportkim.kimchimall.payment.controller.response.PaymentConfirmationResult;
import com.supportkim.kimchimall.payment.infrasturcture.*;
import com.supportkim.kimchimall.payment.service.dto.*;
import com.supportkim.kimchimall.payment.service.event.PaymentEventMessage;
import com.supportkim.kimchimall.wallet.infrasturcture.Wallet;
import com.supportkim.kimchimall.wallet.infrasturcture.WalletJpaRepository;
import com.supportkim.kimchimall.wallet.infrasturcture.WalletTransaction;
import com.supportkim.kimchimall.wallet.infrasturcture.WalletTransactionJpaRepository;
import com.supportkim.kimchimall.wallet.service.WalletService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service @Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentConfirmService {
    private final PaymentEventJpaRepository paymentEventRepository;
    private final PaymentOrderJpaRepository paymentOrderRepository;
    private final PaymentOrderHistoryJpaRepository paymentOrderHistoryRepository;
    private final TossPaymentExecutor tossPaymentExecutor;
    private final WalletTransactionJpaRepository walletTransactionRepository;
    private final WalletJpaRepository walletRepository;
    private final LedgerTransactionJpaRepository ledgerTransactionRepository;
    private final MemberJpaRepository memberRepository;
    private final LedgerEntryJpaRepository ledgerEntryRepository;
    private final AccountJpaRepository accountRepository;
    private final MockTossPaymentExecutor mockTossPaymentExecutor;
    private final ApplicationEventPublisher eventPublisher;
    private final PaymentEventPublisher paymentEventPublisher;
    private final WalletService walletService;
    private final LedgerService ledgerService;
    private final EntityManager em;
    @Transactional
    public PaymentConfirmationResult confirmTransactional(PaymentConfirmCommand command) {
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

        paymentOrderRepository.flush();

        try {
            walletService.walletProcess(command);
            log.info("walletService 끝");

            ledgerService.ledgerProcess(command);
        } catch (BaseException e) {
            log.error("processing failed: {}", e.getMessage());
            // 필요한 추가 처리
        }

        // 모든 업데이트가 끝났다면 paymentEvent 도 Update
        paymentEvent.confirmPaymentDone();

        // 5. 결과 반환
        return new PaymentConfirmationResult(paymentExecutionResult.paymentStatus(), paymentExecutionResult.getFailure());
    }


    @Transactional
    public PaymentConfirmationResult confirmEDA(PaymentConfirmCommand command) {
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

        // 여기까지만 하고 정산 처리 및 장부 기입은 다른 트랜잭션에서 처리 하도록 한다.
        eventPublisher.publishEvent(new PaymentEventMessage(command.getOrderId() , 1L));
        return new PaymentConfirmationResult(paymentExecutionResult.paymentStatus(), paymentExecutionResult.getFailure());
    }

    @Transactional
    public PaymentConfirmationResult confirmKafka(PaymentConfirmCommand command) {
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

        // 여기까지만 하고 정산 처리 및 장부 기입은 다른 트랜잭션에서 처리 하도록 한다.
        paymentEventPublisher.publishToPaymentTopic(new PaymentEventMessage(command.getOrderId() , 1L));

        return new PaymentConfirmationResult(paymentExecutionResult.paymentStatus(), paymentExecutionResult.getFailure());
    }


    @Transactional
    public PaymentConfirmationResult confirm(PaymentConfirmCommand command) {
        // 1. 결제 상태를 EXECUTING 업데이트
        PaymentEvent paymentEvent = paymentEventRepository.findByOrderId(command.getOrderId())
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND_PAYMENT_EVENT));

        List<Pair<Long, String>> result = checkPreviousPaymentOrderStatus(paymentEvent.getOrderId());

        List<PaymentOrderHistory> paymentHistories = result.stream()
                .map(pair -> PaymentOrderHistory.builder()
                        .paymentOrderId(pair.getKey())
                        .previousStatus(PaymentStatus.get(pair.getValue()))
                        .newStatus(PaymentStatus.EXECUTING)
                        .reason("PAYMENT_CONFIRMATION_START")
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

        // 5. Wallet (정산 처리)
        // 5-1) 중복된 정산 처리를 하는지 확인
        if (walletTransactionRepository.existsByOrderId(command.getOrderId())) {
            throw new BaseException(ErrorCode.ALREADY_PAYMENT_WALLET_PROCESS);
        }
        // 5-2) 결제 주문 정보를 가지고 온다.
        // 5-3) 판매자별 결제 주문 정보 그룹화
        // 판매자 ID로 결제 주문 그룹화
        Map<Long, List<PaymentOrder>> paymentOrdersBySellerId = paymentOrders.stream()
                .collect(Collectors.groupingBy(PaymentOrder::getSellerId));
        // 5-4) 지갑 업데이트
        getUpdatedWallets(paymentOrdersBySellerId);

        // 지갑 업데이트가 성공적으로 끝났다면 Update
        paymentOrders.forEach(PaymentOrder::confirmWalletUpdate);

        // 6. Ledger (장부 기입 처리)
        // 6-1) 중복된 장부 기입 처리를 하는지 확인
        if (ledgerTransactionRepository.existsByOrderId(command.getOrderId())) {
            throw new BaseException(ErrorCode.ALREADY_PAYMENT_LEDGER_PROCESS);
        }
        // 6-2) 계정 및 결제 주문 로드
        MemberEntity buyer = findById();
        Account buyerAccount = accountRepository.findByMemberId(buyer.getId()).orElseThrow(()
                -> new BaseException(ErrorCode.MEMBER_ACCOUNT_NOT_FOUND));

        Set<Long> sellerIds = paymentOrdersBySellerId.keySet();
        List<Account> sellerAccounts = accountRepository.findByMemberIds(sellerIds);
        // 6-3) 복식부기 엔트리 생성 (Ledger)
        List<DoubleAccountsForLedger> ledgerList = sellerAccounts.stream()
                .map(sellerAccount -> {
                    DoubleAccountsForLedger ledger = new DoubleAccountsForLedger();
                    ledger.setTo(buyerAccount);    // 구매자 계좌
                    ledger.setFrom(sellerAccount); // 판매자 계좌
                    return ledger;
                })
                .toList();

        List<LedgerEntry> ledgerEntries = createLedgerEntries(ledgerList, paymentOrders);
        // 6-4) 복식 부기 엔트리 저장
        ledgerEntryRepository.saveAll(ledgerEntries);

        // 장부 업데이트가 끝났다면 Update
        paymentOrders.forEach(PaymentOrder::confirmLedgerUpdate);

        // 모든 업데이트가 끝났다면 paymentEvent 도 Update
        paymentEvent.confirmPaymentDone();

        // 5. 결과 반환
        return new PaymentConfirmationResult(paymentExecutionResult.paymentStatus(), paymentExecutionResult.getFailure());
    }

    private List<LedgerEntry> createLedgerEntries(List<DoubleAccountsForLedger> ledgerList, List<PaymentOrder> paymentOrders) {
        return ledgerList.stream()
                .flatMap(ledger -> paymentOrders.stream().flatMap(order -> {
                    LedgerTransaction transaction = LedgerTransaction.builder()
                            .referenceType("PAYMENT_ORDER")
                            .referenceId(1L)
                            .orderId(order.getOrderId())
                            .idempotencyKey(order.getOrderId())
                            .build();

                    LedgerEntry creditEntry = LedgerEntry.builder()
                            .amount(BigDecimal.valueOf(order.getAmount()))
                            .accountId(ledger.getTo().getId())  // buyer account
                            .transaction(transaction)
                            .type(LedgerEntryType.CREDIT)
                            .build();

                    LedgerEntry debitEntry = LedgerEntry.builder()
                            .amount(BigDecimal.valueOf(order.getAmount()))
                            .accountId(ledger.getFrom().getId())  // seller account
                            .transaction(transaction)
                            .type(LedgerEntryType.DEBIT)
                            .build();

                    return Stream.of(creditEntry, debitEntry);
                }))
                .collect(Collectors.toList());
    }

    private MemberEntity findById() {
        return memberRepository.findById(1L).get();
    }


    private void getUpdatedWallets(Map<Long, List<PaymentOrder>> paymentOrdersBySellerId) {
        Set<Long> sellerIds = paymentOrdersBySellerId.keySet();

        List<Wallet> wallets = walletRepository.findByUserIdsWithLock(sellerIds);

        wallets.forEach(wallet -> {
            List<WalletTransaction> transactions =
                    wallet.calculateBalanceWith(paymentOrdersBySellerId.get(wallet.getUserId()));

            // WalletTransaction 저장
            walletTransactionRepository.saveAll(transactions);
        });
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
        List<Object[]> paymentOrderStatusList = paymentOrderRepository.findPaymentOrderStatusByOrderId(command.getOrderId());
        insertPaymentHistory(paymentOrderStatusList, command.getStatus(), command.getFailure().toString());
        updatePaymentOrderStatus(command.getOrderId(), command.getStatus());
        return true;
    }

    public boolean updatePaymentStatusToFailure(PaymentStatusUpdateCommand command) {
        List<Object[]> paymentOrderStatusList = paymentOrderRepository.findPaymentOrderStatusByOrderId(command.getOrderId());
        insertPaymentHistory(paymentOrderStatusList, command.getStatus(), command.getFailure().toString());
        updatePaymentOrderStatus(command.getOrderId(), command.getStatus());

        return true;
    }

    public boolean updatePaymentStatusToSuccess(PaymentStatusUpdateCommand command) {
        List<Object[]> paymentOrderStatusList = paymentOrderRepository.findPaymentOrderStatusByOrderId(command.getOrderId());
        insertPaymentHistory(paymentOrderStatusList, command.getStatus(), "PAYMENT_CONFIRMATION_DONE");
        updatePaymentOrderStatus(command.getOrderId(), command.getStatus());
        updatePaymentEventExtraDetails(command);

        return true;
    }

    private void insertPaymentHistory(List<Object[]> paymentOrderStatusList, PaymentStatus status, String reason) {
        List<PaymentOrderHistory> historyList = paymentOrderStatusList.stream()
                .map(record -> PaymentOrderHistory.of(
                        (Long) record[0],
                        ((PaymentStatus) record[1]),
                        status,
                        reason
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
        List<Object[]> result = paymentOrderRepository.findPaymentOrderStatusByOrderId(orderId);

        List<Pair<Long, String>> pairs = result.stream()
                .map(row -> new Pair<>((Long) row[0], ((PaymentStatus) row[1]).name())) // PaymentStatus → String 변환
                .toList();

        pairs.forEach(pair -> {
            String status = pair.getValue();
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
