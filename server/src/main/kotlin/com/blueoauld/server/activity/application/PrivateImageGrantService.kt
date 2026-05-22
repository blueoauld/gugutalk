package com.blueoauld.server.activity.application

import com.blueoauld.server.activity.application.response.ActivityStatusResponse
import com.blueoauld.server.activity.entity.PrivateImageGrant
import com.blueoauld.server.activity.repository.PrivateImageGrantRepository
import com.blueoauld.server.common.exception.CustomException
import com.blueoauld.server.common.exception.type.ErrorCode.ACTIVITY_07
import com.blueoauld.server.common.exception.type.ErrorCode.ACTIVITY_08
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PrivateImageGrantService(

    private val privateImageGrantRepository: PrivateImageGrantRepository
) {

    @Transactional(readOnly = true)
    fun get(memberId: Long, targetId: Long): ActivityStatusResponse {
        if (privateImageGrantRepository.existsByFromIdAndToId(memberId, targetId)) {
            return ActivityStatusResponse(status = false)
        }

        return ActivityStatusResponse(status = true)
    }

    @Transactional
    fun create(memberId: Long, targetId: Long) {
        if (privateImageGrantRepository.existsByFromIdAndToId(memberId, targetId)) {
            throw CustomException(ACTIVITY_07)
        }

        val privateImageGrant = PrivateImageGrant(
            fromId = memberId,
            toId = targetId
        )
        privateImageGrantRepository.save(privateImageGrant)
    }

    @Transactional
    fun delete(memberId: Long, targetId: Long) {
        val count = privateImageGrantRepository.deleteByFromIdAndToId(memberId, targetId)

        if (count == 0) {
            throw CustomException(ACTIVITY_08)
        }
    }
}