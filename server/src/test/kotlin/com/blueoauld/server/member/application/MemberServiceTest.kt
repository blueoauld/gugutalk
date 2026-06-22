package com.blueoauld.server.member.application

import com.blueoauld.server.activity.repository.PrivateImageGrantRepository
import com.blueoauld.server.common.exception.CustomException
import com.blueoauld.server.common.exception.type.ErrorCode
import com.blueoauld.server.common.properties.R2Properties
import com.blueoauld.server.fixture.memberFixture
import com.blueoauld.server.fixture.memberResultFixture
import com.blueoauld.server.fixture.memberSearchResultFixture
import com.blueoauld.server.member.application.request.MemberUpdateCommentRequest
import com.blueoauld.server.member.application.request.MemberUpdateProfileRequest
import com.blueoauld.server.member.entity.type.MemberImageType
import com.blueoauld.server.member.entity.type.Region
import com.blueoauld.server.member.repository.MemberImageRepository
import com.blueoauld.server.member.repository.MemberRepository
import com.blueoauld.server.r2.application.R2Provider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.context.ApplicationEventPublisher
import java.time.Instant
import java.util.Optional

class MemberServiceTest {

    private val memberRepository = mockk<MemberRepository>(relaxUnitFun = true)
    private val memberImageRepository = mockk<MemberImageRepository>(relaxUnitFun = true)
    private val privateImageGrantRepository = mockk<PrivateImageGrantRepository>(relaxUnitFun = true)
    private val r2Provider = mockk<R2Provider>(relaxUnitFun = true)
    private val applicationEventPublisher = mockk<ApplicationEventPublisher>(relaxUnitFun = true)
    private val r2Properties = mockk<R2Properties>(relaxUnitFun = true)
    private val memberService = MemberService(
        memberRepository,
        memberImageRepository,
        privateImageGrantRepository,
        r2Provider,
        applicationEventPublisher,
        r2Properties,
    )

    @Nested
    inner class UpdateComment {

        @Test
        fun `회원이 존재하면 코멘트를 수정한다`() {
            val member = memberFixture()
            every { memberRepository.findById(1L) } returns Optional.of(member)

            memberService.updateComment(memberId = 1L, request = MemberUpdateCommentRequest(content = "새 코멘트"))

            assertThat(member.comment).isEqualTo("새 코멘트")
        }

        @Test
        fun `존재하지 않는 회원이면 MEMBER_01 예외가 발생한다`() {
            every { memberRepository.findById(1L) } returns Optional.empty()

            assertThatThrownBy {
                memberService.updateComment(memberId = 1L, request = MemberUpdateCommentRequest(content = "x"))
            }.isInstanceOfSatisfying(CustomException::class.java) {
                assertThat(it.errorCode).isEqualTo(ErrorCode.MEMBER_01)
            }
        }
    }

    @Nested
    inner class ToggleChat {

        @Test
        fun `채팅 허용 여부를 반전시킨다`() {
            val member = memberFixture() // isChat 기본값 true
            every { memberRepository.findById(1L) } returns Optional.of(member)

            memberService.toggleChat(memberId = 1L)

            assertThat(member.isChat).isFalse()
        }
    }

    @Nested
    inner class IsChat {

        @Test
        fun `회원의 채팅 허용 여부를 반환한다`() {
            every { memberRepository.findById(1L) } returns Optional.of(memberFixture())

            val response = memberService.isChat(memberId = 1L)

            assertThat(response.isChat).isTrue()
        }

        @Test
        fun `존재하지 않는 회원이면 MEMBER_01 예외가 발생한다`() {
            every { memberRepository.findById(1L) } returns Optional.empty()

            assertThatThrownBy { memberService.isChat(memberId = 1L) }
                .isInstanceOfSatisfying(CustomException::class.java) {
                    assertThat(it.errorCode).isEqualTo(ErrorCode.MEMBER_01)
                }
        }
    }

    @Nested
    inner class Gets {

        @Test
        fun `지역 조건 없이(region=null) 커서 조회하고 페이징한다`() {
            val results = listOf(
                memberResultFixture(memberId = 3L, updatedAt = Instant.parse("2026-01-03T00:00:00Z")),
                memberResultFixture(memberId = 2L, updatedAt = Instant.parse("2026-01-02T00:00:00Z")),
                memberResultFixture(memberId = 1L, updatedAt = Instant.parse("2026-01-01T00:00:00Z")),
            )
            every {
                memberRepository.findAllByCursor(
                    memberId = 1L, gender = "ALL", region = null, cursorId = null, cursorDateAt = null, size = 3
                )
            } returns results

            val response = memberService.gets(memberId = 1L, gender = "ALL", cursorId = null, cursorDateAt = null, size = 2)

            assertThat(response.hasNext).isTrue()
            assertThat(response.payload).hasSize(2)
            assertThat(response.nextId).isEqualTo(2L)
            assertThat(response.nextDateAt).isEqualTo(Instant.parse("2026-01-02T00:00:00Z"))
            verify { memberRepository.findAllByCursor(1L, "ALL", null, null, null, 3) }
        }
    }

    @Nested
    inner class GetsByRegion {

        @Test
        fun `요청 회원의 지역을 조건으로 커서 조회한다`() {
            val member = memberFixture(region = Region.BUSAN)
            every { memberRepository.findById(1L) } returns Optional.of(member)
            every {
                memberRepository.findAllByCursor(
                    memberId = 1L, gender = "ALL", region = Region.BUSAN, cursorId = null, cursorDateAt = null, size = 3
                )
            } returns listOf(memberResultFixture(memberId = 2L))

            val response = memberService.getsByRegion(memberId = 1L, gender = "ALL", cursorId = null, cursorDateAt = null, size = 2)

            assertThat(response.hasNext).isFalse()
            assertThat(response.payload).hasSize(1)
            verify { memberRepository.findAllByCursor(1L, "ALL", Region.BUSAN, null, null, 3) }
        }

        @Test
        fun `존재하지 않는 회원이면 MEMBER_01 예외가 발생한다`() {
            every { memberRepository.findById(1L) } returns Optional.empty()

            assertThatThrownBy {
                memberService.getsByRegion(memberId = 1L, gender = "ALL", cursorId = null, cursorDateAt = null, size = 2)
            }.isInstanceOfSatisfying(CustomException::class.java) {
                assertThat(it.errorCode).isEqualTo(ErrorCode.MEMBER_01)
            }
        }
    }

    @Nested
    inner class Search {

        @Test
        fun `닉네임이 2자 미만이면 SEARCH_01 예외가 발생한다`() {
            assertThatThrownBy {
                memberService.search(memberId = 1L, nickname = "a", cursorId = null, cursorDateAt = null, size = 2)
            }.isInstanceOfSatisfying(CustomException::class.java) {
                assertThat(it.errorCode).isEqualTo(ErrorCode.SEARCH_01)
            }

            verify(exactly = 0) { memberRepository.findAllByNickname(any(), any(), any(), any(), any()) }
        }

        @Test
        fun `닉네임으로 커서 조회하고 페이징한다`() {
            val results = listOf(
                memberSearchResultFixture(memberId = 3L, updatedAt = Instant.parse("2026-01-03T00:00:00Z")),
                memberSearchResultFixture(memberId = 2L, updatedAt = Instant.parse("2026-01-02T00:00:00Z")),
                memberSearchResultFixture(memberId = 1L, updatedAt = Instant.parse("2026-01-01T00:00:00Z")),
            )
            every {
                memberRepository.findAllByNickname(
                    memberId = 1L, nickname = "철수", cursorId = null, cursorDateAt = null, size = 3
                )
            } returns results

            val response = memberService.search(memberId = 1L, nickname = "철수", cursorId = null, cursorDateAt = null, size = 2)

            assertThat(response.hasNext).isTrue()
            assertThat(response.payload).hasSize(2)
            assertThat(response.nextId).isEqualTo(2L)
            assertThat(response.nextDateAt).isEqualTo(Instant.parse("2026-01-02T00:00:00Z"))
        }
    }

    @Nested
    inner class UpdateProfile {

        private fun request(nickname: String) = MemberUpdateProfileRequest(
            publicImages = emptyList(),
            privateImages = emptyList(),
            nickname = nickname,
            birthYear = 1995,
            region = Region.SEOUL,
            bio = "새 자기소개",
        )

        @Test
        fun `존재하지 않는 회원이면 MEMBER_01 예외가 발생한다`() {
            every { memberRepository.findById(1L) } returns Optional.empty()

            assertThatThrownBy { memberService.updateProfile(memberId = 1L, request = request("새닉네임")) }
                .isInstanceOfSatisfying(CustomException::class.java) {
                    assertThat(it.errorCode).isEqualTo(ErrorCode.MEMBER_01)
                }
        }

        @Test
        fun `닉네임을 변경하는데 이미 사용 중이면 MEMBER_03 예외가 발생한다`() {
            every { memberRepository.findById(1L) } returns Optional.of(memberFixture(nickname = "기존닉"))
            every { memberRepository.existsByNickname("중복닉") } returns true

            assertThatThrownBy { memberService.updateProfile(memberId = 1L, request = request("중복닉")) }
                .isInstanceOfSatisfying(CustomException::class.java) {
                    assertThat(it.errorCode).isEqualTo(ErrorCode.MEMBER_03)
                }
        }

        @Test
        fun `이미지 변경이 없으면 프로필만 갱신하고 이벤트를 발행하지 않는다`() {
            val member = memberFixture(nickname = "테스터")
            every { memberRepository.findById(1L) } returns Optional.of(member)
            // 닉네임을 동일하게 유지하므로 existsByNickname 은 호출되지 않는다
            every { memberImageRepository.findAllByMemberIdAndType(1L, any()) } returns emptyList()
            every { r2Properties.domain } returns "https://cdn"

            memberService.updateProfile(memberId = 1L, request = request("테스터"))

            assertThat(member.bio).isEqualTo("새 자기소개")
            assertThat(member.birthYear).isEqualTo(1995)
            verify(exactly = 0) { applicationEventPublisher.publishEvent(any()) }
        }
    }

    @Nested
    inner class GetPrivateImages {

        @Test
        fun `열람 권한이 없으면 ACTIVITY_12 예외가 발생한다`() {
            // 대상(targetId=2)이 요청자(memberId=1)에게 권한을 부여했는지 확인한다
            every { privateImageGrantRepository.existsByFromIdAndToId(2L, 1L) } returns false

            assertThatThrownBy { memberService.getPrivateImages(memberId = 1L, targetId = 2L) }
                .isInstanceOfSatisfying(CustomException::class.java) {
                    assertThat(it.errorCode).isEqualTo(ErrorCode.ACTIVITY_12)
                }

            verify(exactly = 0) { memberImageRepository.findAllByMemberIdAndType(any(), MemberImageType.PRIVATE) }
        }
    }
}
