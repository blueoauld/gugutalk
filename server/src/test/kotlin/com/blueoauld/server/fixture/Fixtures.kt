package com.blueoauld.server.fixture

import com.blueoauld.server.activity.entity.Like
import com.blueoauld.server.activity.entity.Review
import com.blueoauld.server.activity.repository.result.ActivityResult
import com.blueoauld.server.activity.repository.result.ReviewResult
import com.blueoauld.server.chat.entity.ChatRoom
import com.blueoauld.server.member.entity.Member
import com.blueoauld.server.point.entity.Point
import com.blueoauld.server.member.entity.type.Gender
import com.blueoauld.server.member.entity.type.Region
import java.time.Instant

/**
 * 테스트용 엔티티/결과 빌더.
 *
 * 단위 테스트에서는 인라인으로 객체를 만들지 말고 여기 함수들을 사용한다.
 * 필요한 필드만 named argument 로 덮어쓰고 나머지는 합리적인 기본값을 쓴다.
 */

fun memberFixture(
    id: Long = 1L,
    phone: String = "01000000000",
    password: String = "encoded-password",
    deviceId: String = "device-1",
    nickname: String = "테스터",
    gender: Gender = Gender.MALE,
    region: Region = Region.SEOUL,
    birthYear: Int = 2000,
): Member = Member(
    id = id,
    phone = phone,
    password = password,
    deviceId = deviceId,
    nickname = nickname,
    gender = gender,
    region = region,
    birthYear = birthYear,
)

fun likeFixture(
    id: Long = 1L,
    fromId: Long = 1L,
    toId: Long = 2L,
    createdAt: Instant = Instant.parse("2026-01-01T00:00:00Z"),
): Like = Like(
    id = id,
    fromId = fromId,
    toId = toId,
    createdAt = createdAt,
)

fun reviewFixture(
    id: Long = 1L,
    fromId: Long = 2L,
    toId: Long = 1L,
    nickname: String = "익명",
    content: String = "리뷰 내용",
    createdAt: Instant = Instant.parse("2026-01-01T00:00:00Z"),
): Review = Review(
    id = id,
    fromId = fromId,
    toId = toId,
    nickname = nickname,
    content = content,
    createdAt = createdAt,
)

fun pointFixture(
    id: Long = 1L,
    memberId: Long = 1L,
    balance: Long = 1000L,
): Point = Point(
    id = id,
    memberId = memberId,
    balance = balance,
)

fun reviewResultFixture(
    reviewId: Long = 1L,
    fromId: Long = 1L,
    toId: Long = 2L,
    nickname: String = "익명",
    content: String = "리뷰 내용",
    createdAt: Instant = Instant.parse("2026-01-01T00:00:00Z"),
): ReviewResult = ReviewResult(
    reviewId = reviewId,
    fromId = fromId,
    toId = toId,
    nickname = nickname,
    content = content,
    createdAt = createdAt,
)

fun chatRoomFixture(
    id: Long = 10L,
    member1Id: Long = 1L,
    member2Id: Long = 2L,
    lastMessageAt: Instant = Instant.parse("2026-01-01T00:00:00Z"),
): ChatRoom = ChatRoom(
    id = id,
    member1Id = minOf(member1Id, member2Id),
    member2Id = maxOf(member1Id, member2Id),
    member1LastReadMessageId = 0L,
    member2LastReadMessageId = 0L,
    lastMessagePreview = "",
    lastMessageAt = lastMessageAt,
)

fun activityResultFixture(
    activityId: Long = 1L,
    toId: Long = 2L,
    profileUrl: String? = null,
    nickname: String = "상대방",
    gender: Gender = Gender.FEMALE,
    birthYear: Int = 2000,
    region: Region = Region.SEOUL,
    createdAt: Instant = Instant.parse("2026-01-01T00:00:00Z"),
): ActivityResult = ActivityResult(
    activityId = activityId,
    toId = toId,
    profileUrl = profileUrl,
    nickname = nickname,
    gender = gender,
    birthYear = birthYear,
    region = region,
    createdAt = createdAt,
)
