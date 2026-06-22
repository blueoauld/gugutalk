package com.blueoauld.server.activity.application

import com.blueoauld.server.activity.application.response.ActivityRowResponse
import com.blueoauld.server.activity.application.response.RankRowResponse
import com.blueoauld.server.activity.entity.Like
import com.blueoauld.server.activity.repository.LikeRepository
import com.blueoauld.server.common.dto.response.CursorResponse
import com.blueoauld.server.common.dto.response.CursorScoreResponse
import com.blueoauld.server.common.exception.CustomException
import com.blueoauld.server.common.exception.type.ErrorCode.ACTIVITY_01
import com.blueoauld.server.common.exception.type.ErrorCode.ACTIVITY_02
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class LikeService(

    private val likeRepository: LikeRepository,
) {

    @Transactional
    fun create(memberId: Long, targetId: Long) {
        if (likeRepository.existsByFromIdAndToId(memberId, targetId)) {
            throw CustomException(ACTIVITY_01)
        }

        val like = Like(
            fromId = memberId,
            toId = targetId
        )
        likeRepository.save(like)
    }

    @Transactional
    fun delete(memberId: Long, targetId: Long) {
        val count = likeRepository.deleteByFromIdAndToId(memberId, targetId)

        if (count == 0) {
            throw CustomException(ACTIVITY_02)
        }
    }

    @Transactional(readOnly = true)
    fun gets(
        memberId: Long,
        cursorId: Long?,
        cursorDateAt: Instant?,
        size: Int
    ): CursorResponse<ActivityRowResponse> {
        val rows = likeRepository.findAllByCursor(
            memberId = memberId,
            cursorId = cursorId,
            cursorDateAt = cursorDateAt,
            size = size + 1
        ).map {
            ActivityRowResponse.from(it)
        }

        return CursorResponse.of(rows, size, { it.activityId }, { it.createdAt })
    }

    @Transactional(readOnly = true)
    fun getsByRank(
        gender: String,
        cursorId: Long?,
        cursorScore: Long?,
        size: Int
    ): CursorScoreResponse<RankRowResponse> {
        val rows = likeRepository.findAllByRank(
            gender = gender,
            cursorId = cursorId,
            cursorScore = cursorScore,
            size = size + 1
        ).map {
            RankRowResponse.from(it)
        }

        return CursorScoreResponse.of(rows, size, { it.memberId }, { it.likes })
    }
}