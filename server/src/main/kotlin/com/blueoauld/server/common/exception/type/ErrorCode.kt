package com.blueoauld.server.common.exception.type

import org.springframework.http.HttpStatus

enum class ErrorCode(

    val status: HttpStatus,
    val message: String,
) {

    UNAUTHORIZED_02(HttpStatus.UNAUTHORIZED, "접근할 수 없는 리프레쉬 토큰입니다."),

    VERIFICATION_CODE_01(HttpStatus.TOO_MANY_REQUESTS, "인증 번호 요청 횟수를 초과했습니다."),
    VERIFICATION_CODE_02(HttpStatus.BAD_REQUEST, "인증 번호를 다시 한번 확인해주시길 바랍니다."),
    VERIFICATION_CODE_03(HttpStatus.BAD_REQUEST, "인증 번호 전송에 실패했습니다."),

    MEMBER_01(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
    MEMBER_02(HttpStatus.CONFLICT, "이미 사용중인 휴대폰 번호입니다."),
    MEMBER_03(HttpStatus.CONFLICT, "이미 사용중인 닉네임입니다."),
    MEMBER_04(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    MEMBER_05(HttpStatus.BAD_REQUEST, "휴대폰 또는 비밀번호가 일치하지 않습니다."),
    MEMBER_06(HttpStatus.BAD_REQUEST, "로그인을 다시 해주시길 바랍니다."),

    ACTIVITY_01(HttpStatus.CONFLICT, "이미 좋아요를 누르셨습니다."),
    ACTIVITY_02(HttpStatus.BAD_REQUEST, "좋아요를 누른 적이 없습니다."),
    ACTIVITY_03(HttpStatus.CONFLICT, "이미 싫어요를 누르셨습니다."),
    ACTIVITY_04(HttpStatus.BAD_REQUEST, "싫어요를 누른 적이 없습니다."),
    ACTIVITY_05(HttpStatus.CONFLICT, "이미 차단한 대상입니다."),
    ACTIVITY_06(HttpStatus.BAD_REQUEST, "차단한 적이 없습니다."),
    ACTIVITY_07(HttpStatus.CONFLICT, "이미 비밀 사진을 공개하셨습니다."),
    ACTIVITY_08(HttpStatus.BAD_REQUEST, "비밀 사진을 공개한 적이 없습니다."),
    ACTIVITY_09(HttpStatus.BAD_REQUEST, "본인에게 리뷰를 작성할 수 없습니다."),
    ACTIVITY_10(HttpStatus.BAD_REQUEST, "존재하지 않는 리뷰입니다."),
    ACTIVITY_11(HttpStatus.BAD_REQUEST, "접근할 수 없는 리뷰입니다."),
    ACTIVITY_12(HttpStatus.BAD_REQUEST, "접근할 수 없는 비밀 사진입니다."),

    SEARCH_01(HttpStatus.BAD_REQUEST, "최소 2자 이상 입력해주시길 바랍니다."),

    FILE_01(HttpStatus.BAD_REQUEST, "잘못된 컨텐츠 타입입니다."),
    FILE_02(HttpStatus.BAD_REQUEST, "접근할 수 없는 파일입니다."),
    FILE_03(HttpStatus.BAD_REQUEST, "허용 크기를 초과했습니다."),

    REPORT_01(HttpStatus.BAD_REQUEST, "본인을 신고할 수 없습니다."),

    CHAT_01(HttpStatus.BAD_REQUEST, "자기 자신과는 채팅방을 생성할 수 없습니다."),
    CHAT_02(HttpStatus.BAD_REQUEST, "접근할 수 없는 채팅방입니다."),
    CHAT_03(HttpStatus.BAD_REQUEST, "존재하지 않는 채팅방입니다."),

    CHAT_MESSAGE_01(HttpStatus.BAD_REQUEST, "존재하지 않는 채팅 메세지입니다."),
    CHAT_MESSAGE_02(HttpStatus.BAD_REQUEST, "재생할 수 없는 채팅 메세지입니다."),

    POINT_01(HttpStatus.BAD_REQUEST, "존재하지 않는 포인트 정보입니다."),
    POINT_02(HttpStatus.CONFLICT, "오늘 출석 체크를 완료하셨습니다."),
    POINT_03(HttpStatus.BAD_REQUEST, "포인트가 부족합니다."),

    REWARD_01(HttpStatus.BAD_REQUEST, "회원 ID를 찾을 수 없습니다."),

    BAN_01(HttpStatus.CONFLICT, "이미 정지된 상태입니다."),
    BAN_02(HttpStatus.BAD_REQUEST, "존재하지 않는 정지 기록입니다."),

    INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청하신 리소스를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),
}
