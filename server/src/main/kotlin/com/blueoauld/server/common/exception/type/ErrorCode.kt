package com.blueoauld.server.common.exception.type

import org.springframework.http.HttpStatus

enum class ErrorCode(

    val status: HttpStatus,
    val message: String,
) {

    UNAUTHORIZED_02(HttpStatus.UNAUTHORIZED, "접근할 수 없는 리프레쉬 토큰입니다."),

    VERIFICATION_CODE_01(HttpStatus.TOO_MANY_REQUESTS, "인증번호 요청 횟수를 초과했습니다."),
    VERIFICATION_CODE_02(HttpStatus.BAD_REQUEST, "인증 번호를 다시 한번 확인해주시길 바랍니다."),

    MEMBER_01(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
    MEMBER_02(HttpStatus.BAD_REQUEST, "이미 사용중인 휴대폰 번호입니다."),
    MEMBER_03(HttpStatus.BAD_REQUEST, "이미 사용중인 닉네임입니다."),
    MEMBER_04(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    MEMBER_05(HttpStatus.BAD_REQUEST, "휴대폰 또는 비밀번호가 일치하지 않습니다."),

    ACTIVITY_01(HttpStatus.BAD_REQUEST, "이미 좋아요를 누르셨습니다."),
    ACTIVITY_02(HttpStatus.BAD_REQUEST, "좋아요를 누른 적이 없습니다."),
    ACTIVITY_03(HttpStatus.BAD_REQUEST, "이미 싫어요를 누르셨습니다."),
    ACTIVITY_04(HttpStatus.BAD_REQUEST, "싫어요를 누른 적이 없습니다."),
    ACTIVITY_05(HttpStatus.BAD_REQUEST, "이미 차단한 대상입니다."),
    ACTIVITY_06(HttpStatus.BAD_REQUEST, "차단한 적이 없습니다."),
    ACTIVITY_07(HttpStatus.BAD_REQUEST, "이미 비밀 사진을 공개하셨습니다."),
    ACTIVITY_08(HttpStatus.BAD_REQUEST, "비밀 사진을 공개한 적이 없습니다."),
    ACTIVITY_09(HttpStatus.BAD_REQUEST, "본인에게 리뷰를 작성할 수 없습니다."),
    ACTIVITY_10(HttpStatus.BAD_REQUEST, "존재하지 않는 리뷰입니다."),
    ACTIVITY_11(HttpStatus.BAD_REQUEST, "접근할 수 없는 리뷰입니다."),

    INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청하신 리소스를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),
}
