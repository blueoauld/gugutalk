package com.blueoauld.server.point.application

import com.blueoauld.server.common.dto.response.CursorResponse
import com.blueoauld.server.common.exception.CustomException
import com.blueoauld.server.common.exception.type.ErrorCode.POINT_01
import com.blueoauld.server.point.application.response.PointGetBalanceResponse
import com.blueoauld.server.point.application.response.PointHistoryRowResponse
import com.blueoauld.server.point.entity.PointHistory
import com.blueoauld.server.point.entity.type.PointSource.ADVERTISEMENT
import com.blueoauld.server.point.entity.type.PointSource.ATTENDANCE
import com.blueoauld.server.point.repository.PointHistoryRepository
import com.blueoauld.server.point.repository.PointRepository
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

private const val POINT_ATTENDANCE_KEY = "point:attendance:"

@Service
class PointService(

    private val pointRepository: PointRepository,
    private val pointHistoryRepository: PointHistoryRepository,
    private val stringRedisTemplate: StringRedisTemplate,
) {

    @Transactional(readOnly = true)
    fun getBalance(memberId: Long): PointGetBalanceResponse {
        val point = pointRepository.findByMemberId(memberId) ?: throw CustomException(POINT_01)

        return PointGetBalanceResponse(point.balance)
    }

    @Transactional
    fun rewardAttendance(memberId: Long) {
        val attendanceKey = POINT_ATTENDANCE_KEY + memberId

        if (stringRedisTemplate.hasKey(attendanceKey)) {
            throw CustomException(POINT_01)
        }

        val point = pointRepository.findByMemberId(memberId) ?: throw CustomException(POINT_01)

        val pointHistory = PointHistory(
            pointId = point.id,
            source = ATTENDANCE,
            balanceSnapshot = point.balance
        )
        pointHistoryRepository.save(pointHistory)

        point.earn(ATTENDANCE.point)

        // 캐싱
        val zone = ZoneId.of("Asia/Seoul")
        val now = ZonedDateTime.now(zone)
        val midnight = now.toLocalDate().plusDays(1).atStartOfDay(zone)
        val timeout = Duration.between(now, midnight)

        stringRedisTemplate.opsForValue().set(attendanceKey, "1", timeout)
    }

    @Transactional
    fun rewardAdvertisement(memberId: Long) {
        val point = pointRepository.findByMemberId(memberId) ?: throw CustomException(POINT_01)

        val pointHistory = PointHistory(
            pointId = point.id,
            source = ADVERTISEMENT,
            balanceSnapshot = point.balance
        )
        pointHistoryRepository.save(pointHistory)

        point.earn(ADVERTISEMENT.point)
    }

    @Transactional(readOnly = true)
    fun gets(
        memberId: Long,
        cursorId: Long?,
        cursorDateAt: Instant?,
        size: Int
    ): CursorResponse<PointHistoryRowResponse> {
        val result = pointHistoryRepository.findAllByCursor(
            memberId = memberId,
            cursorId = cursorId,
            cursorDateAt = cursorDateAt,
            size = size + 1
        ).map {
            PointHistoryRowResponse.from(it)
        }

        val hasNext = result.size > size
        val items = if (hasNext) result.dropLast(1) else result
        val last = items.lastOrNull()

        return CursorResponse(
            payload = items,
            nextId = last?.pointHistoryId,
            nextDateAt = last?.createdAt,
            hasNext = hasNext
        )
    }
}