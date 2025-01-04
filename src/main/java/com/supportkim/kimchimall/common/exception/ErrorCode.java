package com.supportkim.kimchimall.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    SUCCESS(HttpStatus.OK , "success" , "요청에 성공했습니다."),

    /**
     * Member Error Code
     */
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND , "M000" , "회원을 찾을 수 없습니다."),
    MEMBER_PK_ID_NOT_FOUND(HttpStatus.NOT_FOUND , "M001" , "요청한 ID 에 해당하는 회원이 존재하지 않습니다."),
    MEMBER_LOGIN_ID_NOT_FOUND(HttpStatus.NOT_FOUND , "M002" , "요청한 로그인 아이디에 해당하는 회원이 존재하지 않습니다."),
    MEMBER_PW_NOT_FOUND(HttpStatus.NOT_FOUND , "M003" , "요청한 비밀번호를 다시 확인해주세요."),
    MEMBER_EMAIL_NOT_FOUND(HttpStatus.NOT_FOUND , "M002" , "요청한 이메일를 다시 확인해주세요"),
    MEMBER_ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND , "M002" , "장부 정보를 다시 확인해주세요"),


    /**
     * Kimchi Error Code
     */
    KIMCHI_NOT_FOUND(HttpStatus.NOT_FOUND , "K001" , "해당 김치 상품을 찾을 수 없습니다."),
    KIMCHI_OUT_OF_STOCK_EXCEPTION(HttpStatus.CONFLICT , "K002" , "해당 김치의 수량이 부족합니다."),

    /**
     * Authentication Error Code
     */
    NOT_SUPPORT_METHOD(HttpStatus.METHOD_NOT_ALLOWED , "Auth001" , "요청 방식이 올바르지 않습니다."),
    EMPTY_LOGIN_INFO(HttpStatus.NOT_FOUND , "Auth002" , "아이디 또는 비밀번호가 입력되지 않았습니다."),
    TOKEN_NOT_VALID(HttpStatus.UNAUTHORIZED , "Auth003" , "토큰이 유효하지 않습니다."),
    NOT_EXIST_TOKEN(HttpStatus.NOT_FOUND , "Auth004" , "요청 헤더에 토큰이 존재하지 않습니다."),

    /**
     * Payment Error Code
     */
    PAYMENT_ERROR(HttpStatus.BAD_REQUEST , "Payment001" , "결제에 실패했습니다."),
    NOT_FOUND_PAYMENT_EVENT(HttpStatus.NOT_FOUND , "Payment002" , "찾을 수 없는 결제 입니다."),
    ALREADY_PAYMENT_WALLET_PROCESS(HttpStatus.BAD_REQUEST , "Payment003" , "이미 정산 처리된 결제 입니다."),
    ALREADY_PAYMENT_LEDGER_PROCESS(HttpStatus.BAD_REQUEST , "Payment004" , "이미 장부 기입이 처리된 결제 입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    public static ErrorCode findByMessage(String message) {
        for (ErrorCode response : ErrorCode.values()) {
            if (message.equals(response.message)) {
                return response;
            }
        }
        return null;
    }
}
