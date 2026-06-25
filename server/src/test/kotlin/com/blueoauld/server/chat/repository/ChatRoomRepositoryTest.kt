package com.blueoauld.server.chat.repository

import com.blueoauld.server.chat.repository.impl.ChatRoomCustomRepositoryImpl
import com.blueoauld.server.fixture.chatRoomFixture
import com.blueoauld.server.fixture.memberFixture
import com.blueoauld.server.member.repository.MemberRepository
import com.blueoauld.server.support.PersistenceTestSupport
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.data.repository.findByIdOrNull
import java.time.Instant

@Import(ChatRoomCustomRepositoryImpl::class)
class ChatRoomRepositoryTest : PersistenceTestSupport() {

    @Autowired
    private lateinit var chatRoomRepository: ChatRoomRepository

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Test
    fun `참여자 한 명이 탈퇴한 고아 채팅방은 보정 쿼리로 소프트 삭제된다`() {
        val active1 =
            memberRepository.save(memberFixture(id = 0L, phone = "01000000001", deviceId = "d1", nickname = "활성1"))
        val active2 =
            memberRepository.save(memberFixture(id = 0L, phone = "01000000002", deviceId = "d2", nickname = "활성2"))
        val withdrawn =
            memberRepository.save(memberFixture(id = 0L, phone = "01000000003", deviceId = "d3", nickname = "탈퇴1"))

        val healthyRoom =
            chatRoomRepository.save(chatRoomFixture(id = 0L, member1Id = active1.id, member2Id = active2.id))
        val orphanRoom =
            chatRoomRepository.save(chatRoomFixture(id = 0L, member1Id = active1.id, member2Id = withdrawn.id))

        memberRepository.delete(withdrawn) // @SoftDelete: deleted_at 갱신
        memberRepository.flush()

        val count = chatRoomRepository.softDeleteOrphans(Instant.now())

        assertThat(count).isEqualTo(1)
        assertThat(chatRoomRepository.findByIdOrNull(orphanRoom.id)).isNull()
        assertThat(chatRoomRepository.findByIdOrNull(healthyRoom.id)).isNotNull
    }

    @Test
    fun `이미 소프트 삭제된 방은 보정 대상에서 제외된다`() {
        val active =
            memberRepository.save(memberFixture(id = 0L, phone = "01000000004", deviceId = "d4", nickname = "활성3"))
        val withdrawn =
            memberRepository.save(memberFixture(id = 0L, phone = "01000000005", deviceId = "d5", nickname = "탈퇴2"))

        val room = chatRoomRepository.save(chatRoomFixture(id = 0L, member1Id = active.id, member2Id = withdrawn.id))
        chatRoomRepository.delete(room) // 탈퇴 시점에 정상 삭제됨
        chatRoomRepository.flush()

        memberRepository.delete(withdrawn)
        memberRepository.flush()

        assertThat(chatRoomRepository.softDeleteOrphans(Instant.now())).isEqualTo(0)
    }
}
