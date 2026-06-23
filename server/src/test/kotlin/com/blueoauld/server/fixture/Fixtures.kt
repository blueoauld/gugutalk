package com.blueoauld.server.fixture

import com.blueoauld.server.activity.entity.Like
import com.blueoauld.server.activity.entity.Review
import com.blueoauld.server.activity.repository.result.ActivityResult
import com.blueoauld.server.activity.repository.result.ReviewResult
import com.blueoauld.server.chat.entity.ChatRoom
import com.blueoauld.server.chat.entity.type.MessageType
import com.blueoauld.server.chat.repository.result.ChatMessageResult
import com.blueoauld.server.chat.repository.result.ChatRoomResult
import com.blueoauld.server.chat.repository.result.ChatRoomSearchResult
import com.blueoauld.server.member.entity.Member
import com.blueoauld.server.member.entity.MemberImage
import com.blueoauld.server.member.entity.type.Gender
import com.blueoauld.server.member.entity.type.MemberImageType
import com.blueoauld.server.member.entity.type.Region
import com.blueoauld.server.member.repository.result.MemberResult
import com.blueoauld.server.member.repository.result.MemberSearchResult
import com.blueoauld.server.point.entity.Point
import com.blueoauld.server.point.entity.type.PointSource
import com.blueoauld.server.point.repository.result.PointHistoryResult
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
    updatedAt: Instant = Instant.parse("2026-01-01T00:00:00Z"),
): Member = Member(
    id = id,
    phone = phone,
    password = password,
    deviceId = deviceId,
    nickname = nickname,
    gender = gender,
    region = region,
    birthYear = birthYear,
    updatedAt = updatedAt,
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

fun memberImageFixture(
    id: Long = 1L,
    memberId: Long = 1L,
    type: MemberImageType = MemberImageType.PUBLIC,
    key: String = "member/public/1/a.jpg",
    url: String = "https://cdn.example.com/member/public/1/a.jpg",
    sortOrder: Int = 0,
): MemberImage = MemberImage(
    id = id,
    memberId = memberId,
    type = type,
    key = key,
    url = url,
    sortOrder = sortOrder,
)

fun memberResultFixture(
    memberId: Long = 2L,
    nickname: String = "상대방",
    profileUrl: String? = null,
    gender: Gender = Gender.FEMALE,
    birthYear: Int = 2000,
    region: Region = Region.SEOUL,
    comment: String = "안녕하세요.",
    updatedAt: Instant = Instant.parse("2026-01-01T00:00:00Z"),
    likes: Long = 0,
    unlikes: Long = 0,
    reviews: Long = 0,
): MemberResult = MemberResult(
    memberId = memberId,
    nickname = nickname,
    profileUrl = profileUrl,
    gender = gender,
    birthYear = birthYear,
    region = region,
    comment = comment,
    updatedAt = updatedAt,
    likes = likes,
    unlikes = unlikes,
    reviews = reviews,
)

fun memberSearchResultFixture(
    memberId: Long = 2L,
    nickname: String = "상대방",
    profileUrl: String? = null,
    gender: Gender = Gender.FEMALE,
    birthYear: Int = 2000,
    region: Region = Region.SEOUL,
    updatedAt: Instant = Instant.parse("2026-01-01T00:00:00Z"),
): MemberSearchResult = MemberSearchResult(
    memberId = memberId,
    nickname = nickname,
    profileUrl = profileUrl,
    gender = gender,
    birthYear = birthYear,
    region = region,
    updatedAt = updatedAt,
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

fun chatRoomResultFixture(
    chatRoomId: Long = 1L,
    memberId: Long = 2L,
    nickname: String = "상대방",
    profileUrl: String? = null,
    unreadCount: Long = 0,
    lastMessagePreview: String = "안녕하세요",
    lastMessageAt: Instant = Instant.parse("2026-01-01T00:00:00Z"),
): ChatRoomResult = ChatRoomResult(
    chatRoomId = chatRoomId,
    memberId = memberId,
    nickname = nickname,
    profileUrl = profileUrl,
    unreadCount = unreadCount,
    lastMessagePreview = lastMessagePreview,
    lastMessageAt = lastMessageAt,
)

fun chatRoomSearchResultFixture(
    chatRoomId: Long = 1L,
    memberId: Long = 2L,
    nickname: String = "상대방",
    profileUrl: String? = null,
    lastMessagePreview: String = "안녕하세요",
    lastMessageAt: Instant = Instant.parse("2026-01-01T00:00:00Z"),
): ChatRoomSearchResult = ChatRoomSearchResult(
    chatRoomId = chatRoomId,
    memberId = memberId,
    nickname = nickname,
    profileUrl = profileUrl,
    lastMessagePreview = lastMessagePreview,
    lastMessageAt = lastMessageAt,
)

fun chatMessageResultFixture(
    chatMessageId: Long = 1L,
    senderId: Long = 1L,
    content: String = "메세지",
    type: MessageType = MessageType.TEXT,
    createdAt: Instant = Instant.parse("2026-01-01T00:00:00Z"),
): ChatMessageResult = ChatMessageResult(
    chatMessageId = chatMessageId,
    senderId = senderId,
    content = content,
    type = type,
    createdAt = createdAt,
    reactions = emptyList(),
)

fun pointHistoryResultFixture(
    pointHistoryId: Long = 1L,
    pointSource: PointSource = PointSource.ATTENDANCE,
    createdAt: Instant = Instant.parse("2026-01-01T00:00:00Z"),
): PointHistoryResult = PointHistoryResult(
    pointHistoryId = pointHistoryId,
    pointSource = pointSource,
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
