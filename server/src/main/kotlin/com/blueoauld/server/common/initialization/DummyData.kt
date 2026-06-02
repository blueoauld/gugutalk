package com.blueoauld.server.common.initialization

import com.blueoauld.server.activity.entity.*
import com.blueoauld.server.activity.repository.*
import com.blueoauld.server.chat.entity.ChatMessage
import com.blueoauld.server.chat.entity.ChatRoom
import com.blueoauld.server.chat.entity.type.MessageType
import com.blueoauld.server.chat.repository.ChatMessageRepository
import com.blueoauld.server.chat.repository.ChatRoomRepository
import com.blueoauld.server.common.util.RandomNicknameGenerator
import com.blueoauld.server.member.entity.Member
import com.blueoauld.server.member.entity.type.Gender
import com.blueoauld.server.member.repository.MemberRepository
import com.blueoauld.server.point.entity.Point
import com.blueoauld.server.point.entity.PointHistory
import com.blueoauld.server.point.entity.type.PointSource
import com.blueoauld.server.point.repository.PointHistoryRepository
import com.blueoauld.server.point.repository.PointRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@Component
class DummyData(

    private val passwordEncoder: PasswordEncoder
) {

    private val log = KotlinLogging.logger {}

    @Bean
    fun setup(
        memberRepository: MemberRepository,
        reviewRepository: ReviewRepository,
        likeRepository: LikeRepository,
        unlikeRepository: UnlikeRepository,
        privateImageGrantRepository: PrivateImageGrantRepository,
        blockRepository: BlockRepository,
        chatRoomRepository: ChatRoomRepository,
        chatMessageRepository: ChatMessageRepository,
        pointRepository: PointRepository,
        pointHistoryRepository: PointHistoryRepository,
    ): CommandLineRunner {
        return CommandLineRunner {
            if (memberRepository.count() == 0L) {
                val members = (0 until 100).map {
                    Member(
                        phone = "0100000%04d".format(it.toLong()),
                        password = passwordEncoder.encode("1234")!!,
                        deviceId = UUID.randomUUID().toString(),
                        gender = if (it % 2 == 0) Gender.MALE else Gender.FEMALE,
                        comment = "안녕하세요. $it",
                        bio = "반갑습니다. $it"
                    )
                }

                memberRepository.saveAll(members)
            }
            if (reviewRepository.count() == 0L) {
                val reviews = (2 until 101).map {
                    Review(
                        fromId = it.toLong(),
                        toId = 1,
                        nickname = RandomNicknameGenerator.generate(),
                        content = "리뷰입니다. $it"
                    )
                }

                reviewRepository.saveAll(reviews)
            }
            if (likeRepository.count() == 0L) {
                val likes = (2 until 101).map {
                    Like(
                        fromId = 1,
                        toId = it.toLong()
                    )
                }

                likeRepository.saveAll(likes)
            }
            if (unlikeRepository.count() == 0L) {
                val unlikes = (2 until 101).map {
                    Unlike(
                        fromId = 1,
                        toId = it.toLong()
                    )
                }

                unlikeRepository.saveAll(unlikes)
            }
            if (privateImageGrantRepository.count() == 0L) {
                val privateImageGrants = (2 until 101).map {
                    PrivateImageGrant(
                        fromId = 1,
                        toId = it.toLong()
                    )
                }

                privateImageGrantRepository.saveAll(privateImageGrants)
            }
            if (blockRepository.count() == 0L) {
                val blocks = (2 until 101).map {
                    Block(
                        fromId = 1,
                        toId = it.toLong()
                    )
                }

                blockRepository.saveAll(blocks)
            }
            if (chatRoomRepository.count() == 0L) {
                val chatRooms = (3 until 101).map {
                    ChatRoom(
                        member1Id = 2,
                        member2Id = it.toLong(),
                        member1LastReadMessageId = 0,
                        member2LastReadMessageId = 0,
                        lastMessagePreview = "테스트 $it".repeat(5),
                        lastMessageAt = Instant.now().minus(it.toLong(), ChronoUnit.HOURS),
                    )
                }

                chatRoomRepository.saveAll(chatRooms)
            }
            if (chatMessageRepository.count() == 0L) {
                val chatMessages = (0 until 100).map {
                    ChatMessage(
                        chatRoomId = 1,
                        senderId = 3,
                        type = MessageType.TEXT,
                        content = "내용 $it",
                        createdAt = Instant.now().minus(it.toLong(), ChronoUnit.HOURS),
                    )
                }

                chatMessageRepository.saveAll(chatMessages)
            }
            if (pointRepository.count() == 0L) {
                val points = (1 until 101).map {
                    Point(memberId = it.toLong())
                }

                pointRepository.saveAll(points)
            }
            if (pointHistoryRepository.count() == 0L) {
                val pointHistories = (0 until 100).map {
                    PointHistory(
                        pointId = 2,
                        source = PointSource.ADVERTISEMENT,
                        balanceSnapshot = 0
                    )
                }

                pointHistoryRepository.saveAll(pointHistories)
            }

            log.info { "회원 더미 데이터 ${memberRepository.count()}개 생성" }
            log.info { "리뷰 더미 데이터 ${reviewRepository.count()}개 생성" }
            log.info { "좋아요 더미 데이터 ${likeRepository.count()}개 생성" }
            log.info { "싫어요 더미 데이터 ${unlikeRepository.count()}개 생성" }
            log.info { "비밀 사진 부여 더미 데이터 ${privateImageGrantRepository.count()}개 생성" }
            log.info { "차단 더미 데이터 ${blockRepository.count()}개 생성" }
            log.info { "채팅방 더미 데이터 ${chatRoomRepository.count()}개 생성" }
            log.info { "메세지 더미 데이터 ${chatMessageRepository.count()}개 생성" }
            log.info { "포인트 더미 데이터 ${pointRepository.count()}개 생성" }
            log.info { "포인트 기록 더미 데이터 ${pointHistoryRepository.count()}개 생성" }
        }
    }
}