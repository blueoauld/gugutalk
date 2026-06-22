package com.blueoauld.server.point.application

import com.blueoauld.server.common.exception.CustomException
import com.blueoauld.server.common.exception.type.ErrorCode
import com.blueoauld.server.fixture.memberFixture
import com.blueoauld.server.fixture.pointFixture
import com.blueoauld.server.fixture.pointHistoryResultFixture
import com.blueoauld.server.member.repository.MemberRepository
import com.blueoauld.server.point.application.port.AttendanceStore
import com.blueoauld.server.point.entity.type.PointSource
import com.blueoauld.server.point.repository.PointHistoryRepository
import com.blueoauld.server.point.repository.PointRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

class PointServiceTest {

    private val pointRepository = mockk<PointRepository>(relaxUnitFun = true)
    private val pointHistoryRepository = mockk<PointHistoryRepository>(relaxUnitFun = true)
    private val memberRepository = mockk<MemberRepository>(relaxUnitFun = true)
    private val attendanceStore = mockk<AttendanceStore>(relaxUnitFun = true)
    private val pointService = PointService(pointRepository, pointHistoryRepository, memberRepository, attendanceStore)

    @Nested
    inner class GetBalance {

        @Test
        fun `포인트 계정이 있으면 잔액을 반환한다`() {
            every { pointRepository.findByMemberId(1L) } returns pointFixture(balance = 500L)

            val response = pointService.getBalance(memberId = 1L)

            assertThat(response.balance).isEqualTo(500L)
        }

        @Test
        fun `포인트 계정이 없으면 POINT_01 예외가 발생한다`() {
            every { pointRepository.findByMemberId(1L) } returns null

            assertThatThrownBy { pointService.getBalance(memberId = 1L) }
                .isInstanceOfSatisfying(CustomException::class.java) {
                    assertThat(it.errorCode).isEqualTo(ErrorCode.POINT_01)
                }
        }
    }

    @Nested
    inner class RewardAttendance {

        @Test
        fun `존재하지 않는 회원이면 MEMBER_01 예외가 발생한다`() {
            every { memberRepository.findById(1L) } returns Optional.empty()

            assertThatThrownBy { pointService.rewardAttendance(memberId = 1L, deviceId = "device-1") }
                .isInstanceOfSatisfying(CustomException::class.java) {
                    assertThat(it.errorCode).isEqualTo(ErrorCode.MEMBER_01)
                }
        }

        @Test
        fun `요청 디바이스가 회원의 디바이스와 다르면 MEMBER_06 예외가 발생한다`() {
            every { memberRepository.findById(1L) } returns Optional.of(memberFixture(deviceId = "device-1"))

            assertThatThrownBy { pointService.rewardAttendance(memberId = 1L, deviceId = "other-device") }
                .isInstanceOfSatisfying(CustomException::class.java) {
                    assertThat(it.errorCode).isEqualTo(ErrorCode.MEMBER_06)
                }
        }

        @Test
        fun `포인트 계정이 없으면 POINT_01 예외가 발생한다`() {
            every { memberRepository.findById(1L) } returns Optional.of(memberFixture(deviceId = "device-1"))
            every { pointRepository.findByMemberId(1L) } returns null

            assertThatThrownBy { pointService.rewardAttendance(memberId = 1L, deviceId = "device-1") }
                .isInstanceOfSatisfying(CustomException::class.java) {
                    assertThat(it.errorCode).isEqualTo(ErrorCode.POINT_01)
                }
        }

        @Test
        fun `이미 출석했으면 POINT_02 예외가 발생하고 적립하지 않는다`() {
            every { memberRepository.findById(1L) } returns Optional.of(memberFixture(deviceId = "device-1"))
            every { pointRepository.findByMemberId(1L) } returns pointFixture(balance = 100L)
            every { attendanceStore.claim(1L, "device-1") } returns false

            assertThatThrownBy { pointService.rewardAttendance(memberId = 1L, deviceId = "device-1") }
                .isInstanceOfSatisfying(CustomException::class.java) {
                    assertThat(it.errorCode).isEqualTo(ErrorCode.POINT_02)
                }

            verify(exactly = 0) { pointHistoryRepository.save(any()) }
        }

        @Test
        fun `정상 출석이면 이력을 저장하고 출석 포인트를 적립한다`() {
            val point = pointFixture(balance = 100L)
            every { memberRepository.findById(1L) } returns Optional.of(memberFixture(deviceId = "device-1"))
            every { pointRepository.findByMemberId(1L) } returns point
            every { attendanceStore.claim(1L, "device-1") } returns true
            every { pointHistoryRepository.save(any()) } answers { firstArg() }

            pointService.rewardAttendance(memberId = 1L, deviceId = "device-1")

            assertThat(point.balance).isEqualTo(100L + PointSource.ATTENDANCE.point)
            verify(exactly = 1) { pointHistoryRepository.save(any()) }
        }
    }

    @Nested
    inner class RewardAdvertisement {

        @Test
        fun `포인트 계정이 없으면 POINT_01 예외가 발생한다`() {
            every { pointRepository.findByMemberId(1L) } returns null

            assertThatThrownBy { pointService.rewardAdvertisement(memberId = 1L) }
                .isInstanceOfSatisfying(CustomException::class.java) {
                    assertThat(it.errorCode).isEqualTo(ErrorCode.POINT_01)
                }
        }

        @Test
        fun `정상 보상이면 이력을 저장하고 광고 포인트를 적립한다`() {
            val point = pointFixture(balance = 100L)
            every { pointRepository.findByMemberId(1L) } returns point
            every { pointHistoryRepository.save(any()) } answers { firstArg() }

            pointService.rewardAdvertisement(memberId = 1L)

            assertThat(point.balance).isEqualTo(100L + PointSource.ADVERTISEMENT.point)
            verify(exactly = 1) { pointHistoryRepository.save(any()) }
        }
    }

    @Nested
    inner class Gets {

        @Test
        fun `요청 size 보다 한 건 더 조회되면 hasNext 가 true 이고 마지막 한 건은 잘라낸다`() {
            val results = listOf(
                pointHistoryResultFixture(pointHistoryId = 3L, createdAt = Instant.parse("2026-01-03T00:00:00Z")),
                pointHistoryResultFixture(pointHistoryId = 2L, createdAt = Instant.parse("2026-01-02T00:00:00Z")),
                pointHistoryResultFixture(pointHistoryId = 1L, createdAt = Instant.parse("2026-01-01T00:00:00Z")),
            )
            every {
                pointHistoryRepository.findAllByCursor(memberId = 1L, cursorId = null, cursorDateAt = null, size = 3)
            } returns results

            val response = pointService.gets(memberId = 1L, cursorId = null, cursorDateAt = null, size = 2)

            assertThat(response.hasNext).isTrue()
            assertThat(response.payload).hasSize(2)
            assertThat(response.nextId).isEqualTo(2L)
            assertThat(response.nextDateAt).isEqualTo(Instant.parse("2026-01-02T00:00:00Z"))
        }

        @Test
        fun `조회 결과가 size 이하면 hasNext 가 false 이고 전부 반환한다`() {
            val results = listOf(
                pointHistoryResultFixture(pointHistoryId = 2L, createdAt = Instant.parse("2026-01-02T00:00:00Z")),
                pointHistoryResultFixture(pointHistoryId = 1L, createdAt = Instant.parse("2026-01-01T00:00:00Z")),
            )
            every {
                pointHistoryRepository.findAllByCursor(memberId = 1L, cursorId = null, cursorDateAt = null, size = 3)
            } returns results

            val response = pointService.gets(memberId = 1L, cursorId = null, cursorDateAt = null, size = 2)

            assertThat(response.hasNext).isFalse()
            assertThat(response.payload).hasSize(2)
        }
    }
}
