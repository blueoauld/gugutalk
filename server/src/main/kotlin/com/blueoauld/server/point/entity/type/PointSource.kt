package com.blueoauld.server.point.entity.type

import com.blueoauld.server.point.entity.type.PointType.EARN
import com.blueoauld.server.point.entity.type.PointType.USE

enum class PointSource(

    val point: Long,
    val description: String,
    val type: PointType
) {

    ATTENDANCE(30, "출석 체크", EARN),
    ADVERTISEMENT(15, "광고 보상", EARN),

    MESSAGE_SEND(15, "쪽지 전송", USE),
    REVIEW_WRITE(5, "리뷰 작성", USE),
    REVIEW_DELETE(5, "리뷰 삭제", USE),
}