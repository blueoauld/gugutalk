package com.blueoauld.server.common.initialization

import com.blueoauld.server.activity.entity.Review
import com.blueoauld.server.activity.repository.ReviewRepository
import com.blueoauld.server.chat.entity.ChatMessage
import com.blueoauld.server.chat.entity.ChatRoom
import com.blueoauld.server.chat.entity.type.MessageType
import com.blueoauld.server.chat.repository.ChatMessageRepository
import com.blueoauld.server.chat.repository.ChatRoomRepository
import com.blueoauld.server.common.util.RandomNicknameGenerator
import com.blueoauld.server.member.entity.Member
import com.blueoauld.server.member.entity.type.Gender
import com.blueoauld.server.member.entity.type.Region
import com.blueoauld.server.member.repository.MemberRepository
import com.blueoauld.server.point.entity.Point
import com.blueoauld.server.point.repository.PointRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.random.Random

@Component
class DummyData(

    private val passwordEncoder: PasswordEncoder
) {

    companion object {
        private val COMMENTS = listOf(
            "주말엔 등산이나 캠핑 다녀요",
            "맛있는 거 먹으러 다니는 거 좋아해요",
            "넷플릭스 정주행이 취미예요",
            "운동 같이 하실 분 환영이에요",
            "조용한 카페에서 수다 떠는 거 좋아해요",
            "여행 다니는 거 진짜 좋아합니다",
            "강아지 키우고 있어요 🐶",
            "음악 듣고 공연 보러 다녀요",
            "요리하는 거 좋아해서 자주 해먹어요",
            "그림 그리고 전시회 보러 다녀요",
            "주말 아침 러닝이 일상이에요",
            "사진 찍는 거 좋아해요 📷",
        )

        private val BIOS = listOf(
            "안녕하세요! 새로운 사람 만나는 걸 좋아하는 평범한 직장인입니다. 편하게 연락 주세요 :)",
            "처음이라 조금 어색하지만 좋은 인연 만나고 싶어요. 대화 잘 통하는 분이면 좋겠어요.",
            "운동과 여행을 좋아합니다. 주말마다 새로운 곳 탐방하는 게 낙이에요. 함께할 사람 찾아요!",
            "차분한 성격이고 깊은 대화 나누는 걸 좋아해요. 천천히 알아가면 좋겠습니다.",
            "맛집 찾아다니는 게 취미예요. 같이 맛있는 거 먹으러 다닐 분 환영합니다 ㅎㅎ",
            "긍정적이고 잘 웃는 편이에요. 서로 편하게 대해줄 수 있는 분이면 좋겠어요.",
            "퇴근하고 운동하고 주말엔 집에서 쉬는 걸 좋아해요. 비슷한 분이면 잘 맞을 것 같아요.",
            "음악이랑 영화 좋아하고 취향 공유하는 거 좋아해요. 좋은 인연이면 좋겠네요 :)",
        )

        private val REVIEW_CONTENTS = listOf(
            "시간 약속 잘 지키시고 매너가 좋으셨어요",
            "대화가 잘 통해서 시간 가는 줄 몰랐어요",
            "사진보다 실물이 훨씬 좋으세요 ㅎㅎ",
            "친절하고 배려심이 많은 분이에요",
            "편하게 대해주셔서 즐거운 시간이었어요",
            "유머 감각이 좋아서 계속 웃었네요",
            "약속 장소도 센스 있게 잘 골라주셨어요",
            "다음에 또 만나고 싶은 좋은 분이었어요",
            "처음인데도 어색하지 않게 잘 리드해주셨어요",
            "이야기를 잘 들어주셔서 편안했어요",
        )

        private val CHAT_PREVIEWS = listOf(
            "넵 그때 봬요!",
            "오늘 즐거웠어요 ㅎㅎ",
            "내일 시간 괜찮으세요?",
            "ㅋㅋㅋㅋ 진짜요?",
            "조심히 들어가세요~",
            "사진 잘 봤어요!",
            "어디서 만날까요?",
            "좋은 밤 보내세요 :)",
            "연락 기다릴게요!",
            "그럼 주말에 봬요~",
        )

        private val CONVERSATION = listOf(
            "안녕하세요! 매칭돼서 연락드려요 :)",
            "안녕하세요~ 반가워요 ㅎㅎ",
            "프로필 보니까 여행 자주 다니시는 것 같던데 맞아요?",
            "네 맞아요 ㅋㅋ 여행 진짜 좋아해요. 최근엔 제주도 다녀왔어요",
            "오 제주도 좋죠! 저도 작년에 갔었는데 또 가고 싶더라고요",
            "맞아요 가도 가도 또 가고 싶은 곳이에요 ㅎㅎ 어디 가보셨어요?",
            "성산일출봉이랑 우도 갔어요. 우도 진짜 예쁘더라고요",
            "우도 저도 너무 좋아해요! 자전거 타고 한 바퀴 돌면 딱이죠",
            "ㅋㅋㅋ 맞아요 저도 자전거 빌려서 돌았어요",
            "여행 취향 비슷하신 것 같아서 좋네요 :)",
            "그러게요 ㅎㅎ 혹시 이번 주말에 시간 어떠세요?",
            "주말 괜찮아요! 무슨 일 있으세요?",
            "날씨도 좋고 해서 커피라도 한잔 어떨까 해서요",
            "좋아요! 어디서 볼까요?",
            "혹시 강남 쪽 괜찮으세요? 분위기 좋은 카페 알아요",
            "네 강남 좋아요. 토요일 오후쯤이면 될까요?",
            "토요일 오후 좋습니다! 2시쯤 어떠세요?",
            "2시 좋아요 ㅎㅎ 그럼 그때 봬요!",
            "넵! 카페 위치는 다시 보내드릴게요",
            "감사해요~ 기대되네요 :)",
            "혹시 커피 말고 못 드시는 거 있으세요?",
            "딱히 없어요! 다 잘 먹어요 ㅎㅎ",
            "오 좋네요 그럼 근처에 디저트 맛있는 곳도 같이 가요",
            "좋아요!! 점점 더 기대되는데요? ㅋㅋ",
            "ㅋㅋㅋ 너무 부담 갖진 마시고 편하게 와요",
            "넵 ㅎㅎ 편하게 갈게요",
            "그럼 토요일에 봬요! 조심히 주무세요",
            "네 좋은 밤 보내세요~ 토요일에 봬요 :)",
        )
    }

    private val log = KotlinLogging.logger {}

    @ConditionalOnProperty(name = ["dummy-data.enabled"], havingValue = "true")
    @Bean
    fun setup(
        memberRepository: MemberRepository,
        reviewRepository: ReviewRepository,
        chatRoomRepository: ChatRoomRepository,
        chatMessageRepository: ChatMessageRepository,
        pointRepository: PointRepository,
    ): CommandLineRunner {
        return CommandLineRunner {
            if (memberRepository.count() == 0L) {
                val members = (0 until 100).map {
                    Member(
                        phone = "010%04d%04d".format(Random.nextInt(1000, 10000), it),
                        password = passwordEncoder.encode("1234")!!,
                        deviceId = UUID.randomUUID().toString(),
                        profileUrl = "https://picsum.photos/id/${Random.nextInt(100, 200)}/200/200",
                        nickname = RandomNicknameGenerator.generate(),
                        region = Region.entries.random(),
                        gender = Gender.entries.random(),
                        comment = COMMENTS.random(),
                        bio = BIOS.random(),
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
                        content = REVIEW_CONTENTS.random(),
                    )
                }

                reviewRepository.saveAll(reviews)
            }
            if (chatRoomRepository.count() == 0L) {
                val chatRooms = (3 until 101).map {
                    ChatRoom(
                        member1Id = 2,
                        member2Id = it.toLong(),
                        member1LastReadMessageId = 0,
                        member2LastReadMessageId = 0,
                        lastMessagePreview = CHAT_PREVIEWS.random(),
                        lastMessageAt = Instant.now().minus(it.toLong(), ChronoUnit.HOURS),
                    )
                }

                chatRoomRepository.saveAll(chatRooms)
            }
            if (chatMessageRepository.count() == 0L) {
                val baseTime = Instant.now().minus(2, ChronoUnit.DAYS)
                val chatMessages = CONVERSATION.mapIndexed { index, content ->
                    ChatMessage(
                        chatRoomId = 1,
                        senderId = if (index % 2 == 0) 2 else 3,
                        type = MessageType.TEXT,
                        content = content,
                        createdAt = baseTime.plus((index * 7).toLong(), ChronoUnit.MINUTES),
                    )
                }

                chatMessageRepository.saveAll(chatMessages)
            }
            if (pointRepository.count() == 0L) {
                val points = (1 until 101).map {
                    Point(
                        memberId = it.toLong(),
                        balance = ((0..50).random() * 100).toLong(),
                    )
                }

                pointRepository.saveAll(points)
            }

            log.info { "회원 더미 데이터 ${memberRepository.count()}개 생성" }
            log.info { "리뷰 더미 데이터 ${reviewRepository.count()}개 생성" }
            log.info { "채팅방 더미 데이터 ${chatRoomRepository.count()}개 생성" }
            log.info { "메세지 더미 데이터 ${chatMessageRepository.count()}개 생성" }
            log.info { "포인트 더미 데이터 ${pointRepository.count()}개 생성" }
        }
    }
}