package com.blueoauld.server.activity.application

import com.blueoauld.server.activity.entity.Unlike
import com.blueoauld.server.activity.repository.UnlikeRepository
import com.blueoauld.server.common.exception.CustomException
import com.blueoauld.server.common.exception.type.ErrorCode.ACTIVITY_03
import com.blueoauld.server.common.exception.type.ErrorCode.ACTIVITY_04
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UnlikeService(

    private val unlikeRepository: UnlikeRepository
) {

    @Transactional
    fun create(memberId: Long, targetId: Long) {
        if (unlikeRepository.existsByFromIdAndToId(memberId, targetId)) {
            throw CustomException(ACTIVITY_03)
        }

        val unlike = Unlike(
            fromId = memberId,
            toId = targetId
        )
        unlikeRepository.save(unlike)
    }

    @Transactional
    fun delete(memberId: Long, targetId: Long) {
        val count = unlikeRepository.deleteByFromIdAndToId(memberId, targetId)

        if (count == 0) {
            throw CustomException(ACTIVITY_04)
        }
    }
}