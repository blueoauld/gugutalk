package com.blueoauld.server.activity.application

import com.blueoauld.server.activity.application.response.ActivityStatusResponse
import com.blueoauld.server.activity.entity.Like
import com.blueoauld.server.activity.repository.LikeRepository
import com.blueoauld.server.common.exception.CustomException
import com.blueoauld.server.common.exception.type.ErrorCode
import com.blueoauld.server.common.exception.type.ErrorCode.ACTIVITY_01
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LikeService(

    private val likeRepository: LikeRepository,
) {

    @Transactional(readOnly = true)
    fun get(memberId: Long, targetId: Long): ActivityStatusResponse {
        if (likeRepository.existsByFromIdAndToId(memberId, targetId)) {
            return ActivityStatusResponse(status = false)
        }

        return ActivityStatusResponse(status = true)
    }

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
            throw CustomException(ErrorCode.ACTIVITY_02)
        }
    }
}