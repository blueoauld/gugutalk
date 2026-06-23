package com.blueoauld.server.member.repository

import com.blueoauld.server.fixture.memberFixture
import com.blueoauld.server.member.entity.Member
import com.blueoauld.server.member.repository.impl.MemberCustomRepositoryImpl
import com.blueoauld.server.support.PersistenceTestSupport
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import java.time.Instant

/**
 * 닉네임 검색 커서(키셋) JDSL 쿼리를 실제 Postgres 로 검증한다.
 * prefix LIKE 매칭 + (updated_at DESC, id DESC) 커서 + 소프트 삭제 필터.
 */
@Import(MemberCustomRepositoryImpl::class)
class MemberSearchPersistenceTest : PersistenceTestSupport() {

    @Autowired
    private lateinit var memberRepository: MemberRepository

    private val viewerId = 999_999L
    private var seq = 0

    private fun save(nickname: String, updatedAt: Instant): Member {
        seq++
        return memberRepository.saveAndFlush(
            memberFixture(id = 0L, phone = "0100%07d".format(seq), nickname = nickname, updatedAt = updatedAt)
        )
    }

    private val t1 = Instant.parse("2026-01-01T00:00:00Z")
    private val t2 = Instant.parse("2026-01-02T00:00:00Z")
    private val t3 = Instant.parse("2026-01-03T00:00:00Z")

    @Test
    fun `닉네임 prefix 로 매칭되고 updated_at DESC, id DESC 로 정렬된다`() {
        val cheolsu = save("김철수", updatedAt = t1)
        val younghee = save("김영희", updatedAt = t2)
        save("박지성", updatedAt = t3) // prefix 불일치

        val result = memberRepository.findAllByNickname(
            memberId = viewerId, nickname = "김", cursorId = null, cursorDateAt = null, size = 10
        )

        assertThat(result.map { it.memberId }).containsExactly(younghee.id, cheolsu.id)
    }

    @Test
    fun `prefix 가 아니면(중간 일치) 매칭되지 않는다`() {
        save("김영희", updatedAt = t2)

        val result = memberRepository.findAllByNickname(
            memberId = viewerId, nickname = "영희", cursorId = null, cursorDateAt = null, size = 10
        )

        assertThat(result).isEmpty()
    }

    @Test
    fun `커서는 경계 행을 제외하고 다음 페이지를 반환한다`() {
        val a = save("김a", updatedAt = t1)
        val b = save("김b", updatedAt = t2)
        val c = save("김c", updatedAt = t3)

        val page1 = memberRepository.findAllByNickname(
            memberId = viewerId, nickname = "김", cursorId = null, cursorDateAt = null, size = 2
        )
        assertThat(page1.map { it.memberId }).containsExactly(c.id, b.id)

        val boundary = page1.last()
        val page2 = memberRepository.findAllByNickname(
            memberId = viewerId, nickname = "김",
            cursorId = boundary.memberId, cursorDateAt = boundary.updatedAt, size = 2
        )

        assertThat(page2.map { it.memberId }).containsExactly(a.id)
    }

    @Test
    fun `소프트 삭제된 회원은 검색에서 누출되지 않는다`() {
        val a = save("김a", updatedAt = t1)
        val b = save("김b", updatedAt = t2)

        memberRepository.delete(b)
        memberRepository.flush()

        val result = memberRepository.findAllByNickname(
            memberId = viewerId, nickname = "김", cursorId = null, cursorDateAt = null, size = 10
        )

        assertThat(result.map { it.memberId }).containsExactly(a.id)
    }

    @Test
    fun `요청자 본인은 검색 결과에서 제외된다`() {
        val a = save("김a", updatedAt = t1)
        val b = save("김b", updatedAt = t2)

        val result = memberRepository.findAllByNickname(
            memberId = b.id, nickname = "김", cursorId = null, cursorDateAt = null, size = 10
        )

        assertThat(result.map { it.memberId }).containsExactly(a.id)
    }
}
