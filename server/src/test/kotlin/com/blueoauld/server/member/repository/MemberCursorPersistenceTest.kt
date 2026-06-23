package com.blueoauld.server.member.repository

import com.blueoauld.server.fixture.memberFixture
import com.blueoauld.server.member.entity.Member
import com.blueoauld.server.member.entity.type.Gender
import com.blueoauld.server.member.repository.impl.MemberCustomRepositoryImpl
import com.blueoauld.server.support.PersistenceTestSupport
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import java.time.Instant

/**
 * 커서(키셋) 페이지네이션 JDSL 쿼리를 실제 Postgres 로 검증한다.
 * CLAUDE.md 가 must-cover 로 지정한 정렬·커서 경계·소프트 삭제 필터를 직접 확인한다.
 */
@Import(MemberCustomRepositoryImpl::class)
class MemberCursorPersistenceTest : PersistenceTestSupport() {

    @Autowired
    private lateinit var memberRepository: MemberRepository

    private val viewerId = 999_999L // 시드하지 않은 id (본인 제외 조건에만 영향)
    private var seq = 0

    private fun save(nickname: String, updatedAt: Instant, gender: Gender = Gender.FEMALE): Member {
        seq++
        return memberRepository.saveAndFlush(
            memberFixture(
                id = 0L,
                phone = "0100%07d".format(seq),
                nickname = nickname,
                gender = gender,
                updatedAt = updatedAt,
            )
        )
    }

    private val t1 = Instant.parse("2026-01-01T00:00:00Z")
    private val t2 = Instant.parse("2026-01-02T00:00:00Z")
    private val t3 = Instant.parse("2026-01-03T00:00:00Z")

    @Test
    fun `updated_at DESC, id DESC 순으로 정렬된다`() {
        val m1 = save("a", updatedAt = t1)
        val m2 = save("b", updatedAt = t2)
        val m3 = save("c", updatedAt = t3)

        val result = memberRepository.findAllByCursor(
            memberId = viewerId, gender = "ALL", region = null, cursorId = null, cursorDateAt = null, size = 10
        )

        assertThat(result.map { it.memberId }).containsExactly(m3.id, m2.id, m1.id)
    }

    @Test
    fun `updated_at 이 같으면 id DESC 로 정렬된다`() {
        val a = save("a", updatedAt = t2)
        val b = save("b", updatedAt = t2) // b.id > a.id

        val result = memberRepository.findAllByCursor(
            memberId = viewerId, gender = "ALL", region = null, cursorId = null, cursorDateAt = null, size = 10
        )

        assertThat(result.map { it.memberId }).containsExactly(b.id, a.id)
    }

    @Test
    fun `커서는 경계 행을 제외하고 다음 페이지를 반환한다`() {
        val m1 = save("a", updatedAt = t1)
        val m2 = save("b", updatedAt = t2)
        val m3 = save("c", updatedAt = t3)

        val page1 = memberRepository.findAllByCursor(
            memberId = viewerId, gender = "ALL", region = null, cursorId = null, cursorDateAt = null, size = 2
        )
        assertThat(page1.map { it.memberId }).containsExactly(m3.id, m2.id)

        val boundary = page1.last() // m2
        val page2 = memberRepository.findAllByCursor(
            memberId = viewerId, gender = "ALL", region = null,
            cursorId = boundary.memberId, cursorDateAt = boundary.updatedAt, size = 2
        )

        // 경계 행(m2)은 제외되고 m1 만 남는다
        assertThat(page2.map { it.memberId }).containsExactly(m1.id)
    }

    @Test
    fun `소프트 삭제된 회원은 결과에서 누출되지 않는다`() {
        val m1 = save("a", updatedAt = t1)
        val m2 = save("b", updatedAt = t2)
        val m3 = save("c", updatedAt = t3)

        memberRepository.delete(m2)
        memberRepository.flush()

        val result = memberRepository.findAllByCursor(
            memberId = viewerId, gender = "ALL", region = null, cursorId = null, cursorDateAt = null, size = 10
        )

        assertThat(result.map { it.memberId }).containsExactly(m3.id, m1.id)
    }

    @Test
    fun `요청자 본인은 결과에서 제외된다`() {
        val m1 = save("a", updatedAt = t1)
        val m2 = save("b", updatedAt = t2)
        val m3 = save("c", updatedAt = t3)

        val result = memberRepository.findAllByCursor(
            memberId = m2.id, gender = "ALL", region = null, cursorId = null, cursorDateAt = null, size = 10
        )

        assertThat(result.map { it.memberId }).containsExactly(m3.id, m1.id)
    }

    @Test
    fun `gender 필터를 적용하면 해당 성별만 조회된다`() {
        val male = save("m", updatedAt = t2, gender = Gender.MALE)
        save("f", updatedAt = t1, gender = Gender.FEMALE)

        val result = memberRepository.findAllByCursor(
            memberId = viewerId, gender = "MALE", region = null, cursorId = null, cursorDateAt = null, size = 10
        )

        assertThat(result.map { it.memberId }).containsExactly(male.id)
    }
}
