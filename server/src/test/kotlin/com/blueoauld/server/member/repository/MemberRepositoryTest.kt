package com.blueoauld.server.member.repository

import com.blueoauld.server.fixture.memberFixture
import com.blueoauld.server.member.repository.impl.MemberCustomRepositoryImpl
import com.blueoauld.server.support.PersistenceTestSupport
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.data.repository.findByIdOrNull

@Import(MemberCustomRepositoryImpl::class)
class MemberRepositoryTest : PersistenceTestSupport() {

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Test
    fun `저장한 회원은 전화번호로 조회된다`() {
        val saved = memberRepository.save(
            memberFixture(id = 0L, phone = "01011112222", nickname = "보영")
        )

        val found = memberRepository.findByPhone("01011112222")

        assertThat(found).isNotNull
        assertThat(found!!.id).isEqualTo(saved.id)
    }

    @Test
    fun `소프트 삭제된 회원은 전화번호_ID 조회에서 누출되지 않는다`() {
        val saved = memberRepository.save(
            memberFixture(id = 0L, phone = "01033334444", nickname = "수민")
        )

        memberRepository.delete(saved) // @SoftDelete: deleted_at 갱신
        memberRepository.flush()

        assertThat(memberRepository.findByPhone("01033334444")).isNull()
        assertThat(memberRepository.findByIdOrNull(saved.id)).isNull()
        assertThat(memberRepository.existsByPhone("01033334444")).isFalse()
    }
}
