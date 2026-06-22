package com.blueoauld.server.member.application

import com.blueoauld.server.common.exception.CustomException
import com.blueoauld.server.common.exception.type.ErrorCode
import com.blueoauld.server.fixture.memberImageFixture
import com.blueoauld.server.member.application.request.MemberImageCreateRequest
import com.blueoauld.server.member.entity.type.MemberImageType
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

/**
 * 이미지 동기화의 순수 계산 로직 테스트. DB/Mock 없이 입력 → 출력만 검증한다.
 */
class MemberImageSyncPlanTest {

    private val domain = "https://cdn"
    private val type = MemberImageType.PUBLIC

    private fun req(key: String) = MemberImageCreateRequest(url = "ignored", key = key)

    private fun plan(
        existing: List<com.blueoauld.server.member.entity.MemberImage>,
        requested: List<MemberImageCreateRequest>,
    ) = MemberImageSyncPlan.of(
        memberId = 1L,
        type = type,
        domain = domain,
        existingImages = existing,
        requestedImages = requested,
    )

    @Test
    fun `요청과 기존이 모두 비어 있으면 빈 계획을 반환한다`() {
        val result = plan(existing = emptyList(), requested = emptyList())

        assertThat(result.toInsert).isEmpty()
        assertThat(result.toDelete).isEmpty()
        assertThat(result.moveTasks).isEmpty()
        assertThat(result.firstImageUrl).isNull()
    }

    @Test
    fun `신규 이미지는 temporary 경로에서 정식 경로로 옮길 작업과 삽입 대상을 만든다`() {
        val result = plan(
            existing = emptyList(),
            requested = listOf(req("member/public/temporary/1/new.jpg")),
        )

        assertThat(result.toInsert).hasSize(1)
        with(result.toInsert.first()) {
            assertThat(key).isEqualTo("member/public/1/new.jpg")
            assertThat(url).isEqualTo("https://cdn/member/public/1/new.jpg")
            assertThat(sortOrder).isEqualTo(0)
        }
        assertThat(result.moveTasks).hasSize(1)
        with(result.moveTasks.first()) {
            assertThat(sourceKey).isEqualTo("member/public/temporary/1/new.jpg")
            assertThat(destinationKey).isEqualTo("member/public/1/new.jpg")
        }
        assertThat(result.toDelete).isEmpty()
        assertThat(result.firstImageUrl).isEqualTo("https://cdn/member/public/1/new.jpg")
    }

    @Test
    fun `신규도 기존도 아닌 키가 들어오면 FILE_02 예외가 발생한다`() {
        assertThatThrownBy {
            plan(
                existing = emptyList(),
                requested = listOf(req("member/public/1/unknown.jpg")),
            )
        }.isInstanceOfSatisfying(CustomException::class.java) {
            assertThat(it.errorCode).isEqualTo(ErrorCode.FILE_02)
        }
    }

    @Test
    fun `기존 이미지 순서만 바뀌면 삽입_삭제 없이 정렬 순서만 갱신한다`() {
        val a = memberImageFixture(id = 1L, key = "member/public/1/a.jpg", sortOrder = 0)
        val b = memberImageFixture(id = 2L, key = "member/public/1/b.jpg", sortOrder = 1)

        // 요청 순서를 b, a 로 뒤집는다
        val result = plan(
            existing = listOf(a, b),
            requested = listOf(req("member/public/1/b.jpg"), req("member/public/1/a.jpg")),
        )

        assertThat(result.toInsert).isEmpty()
        assertThat(result.toDelete).isEmpty()
        assertThat(result.moveTasks).isEmpty()
        assertThat(b.sortOrder).isEqualTo(0)
        assertThat(a.sortOrder).isEqualTo(1)
        assertThat(result.firstImageUrl).isEqualTo("https://cdn/member/public/1/b.jpg")
    }

    @Test
    fun `요청에서 빠진 기존 이미지는 삭제 대상이 된다`() {
        val a = memberImageFixture(id = 1L, key = "member/public/1/a.jpg", sortOrder = 0)
        val b = memberImageFixture(id = 2L, key = "member/public/1/b.jpg", sortOrder = 1)

        val result = plan(
            existing = listOf(a, b),
            requested = listOf(req("member/public/1/a.jpg")),
        )

        assertThat(result.toDelete).containsExactly(b)
        assertThat(result.deleteKeys).containsExactly("member/public/1/b.jpg")
        assertThat(result.toInsert).isEmpty()
        assertThat(a.sortOrder).isEqualTo(0)
    }

    @Test
    fun `신규 추가_기존 유지_기존 삭제가 한 번에 처리된다`() {
        val a = memberImageFixture(id = 1L, key = "member/public/1/a.jpg", sortOrder = 0)
        val b = memberImageFixture(id = 2L, key = "member/public/1/b.jpg", sortOrder = 1)

        // new 를 맨 앞(0)으로, a 를 그 다음(1)으로, b 는 제거
        val result = plan(
            existing = listOf(a, b),
            requested = listOf(req("member/public/temporary/1/new.jpg"), req("member/public/1/a.jpg")),
        )

        assertThat(result.toInsert).hasSize(1)
        assertThat(result.toInsert.first().key).isEqualTo("member/public/1/new.jpg")
        assertThat(result.toInsert.first().sortOrder).isEqualTo(0)
        assertThat(result.toDelete).containsExactly(b)
        assertThat(result.moveTasks.map { it.destinationKey }).containsExactly("member/public/1/new.jpg")
        assertThat(a.sortOrder).isEqualTo(1)
        assertThat(result.firstImageUrl).isEqualTo("https://cdn/member/public/1/new.jpg")
    }
}
