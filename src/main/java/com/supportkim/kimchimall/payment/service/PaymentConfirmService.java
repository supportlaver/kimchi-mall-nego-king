package com.supportkim.kimchimall.payment.service;

import com.supportkim.kimchimall.common.exception.BaseException;
import com.supportkim.kimchimall.common.exception.ErrorCode;
import com.supportkim.kimchimall.common.exception.PaymentAlreadyProcessedException;
import com.supportkim.kimchimall.common.exception.PaymentValidationException;
import com.supportkim.kimchimall.common.util.Pair;
import com.supportkim.kimchimall.ledger.infrasturcture.*;
import com.supportkim.kimchimall.member.controller.port.MemberService;
import com.supportkim.kimchimall.member.domain.Member;
import com.supportkim.kimchimall.member.infrastructure.MemberEntity;
import com.supportkim.kimchimall.member.infrastructure.MemberJpaRepository;
import com.supportkim.kimchimall.payment.controller.response.PaymentConfirmationResult;
import com.supportkim.kimchimall.payment.infrasturcture.*;
import com.supportkim.kimchimall.payment.service.dto.*;
import com.supportkim.kimchimall.wallet.infrasturcture.Wallet;
import com.supportkim.kimchimall.wallet.infrasturcture.WalletJpaRepository;
import com.supportkim.kimchimall.wallet.infrasturcture.WalletTransaction;
import com.supportkim.kimchimall.wallet.infrasturcture.WalletTransactionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
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
                // ledger(구매자/판매자) x order(주문) 조합을 flatMap
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

                    // CREDIT/DEBIT 두 개의 엔티티를 Stream으로 반환
                    return Stream.of(creditEntry, debitEntry);
                }))
                .collect(Collectors.toList());
    }

    private MemberEntity findById() {
        return memberRepository.findById(1L).get();
    }


    private void getUpdatedWallets(Map<Long, List<PaymentOrder>> paymentOrdersBySellerId) {
        Set<Long> sellerIds = paymentOrdersBySellerId.keySet();

        // 지갑 가져오기
        List<Wallet> wallets = walletRepository.findByUserIds(sellerIds);
        System.out.println("wallets = " + wallets.size());

        // 지갑 업데이트 후 WalletTransaction 저장
        wallets.forEach(wallet -> {
            // calculateBalanceWith 호출 후 반환된 WalletTransaction 저장
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
