package com.blueoauld.server.activity.application

import com.blueoauld.server.activity.application.response.ActivityStatusResponse
import com.blueoauld.server.activity.entity.Block
import com.blueoauld.server.activity.repository.BlockRepository
import com.blueoauld.server.common.exception.CustomException
import com.blueoauld.server.common.exception.type.ErrorCode.ACTIVITY_05
import com.blueoauld.server.common.exception.type.ErrorCode.ACTIVITY_06
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BlockService(

    private val blockRepository: BlockRepository,
) {

    @Transactional(readOnly = true)
    fun get(memberId: Long, targetId: Long): ActivityStatusResponse {
        if (blockRepository.existsByFromIdAndToId(memberId, targetId)) {
            return ActivityStatusResponse(status = false)
        }

        return ActivityStatusResponse(status = true)
    }

    @Transactional
    fun create(memberId: Long, targetId: Long) {
        if (blockRepository.existsByFromIdAndToId(memberId, targetId)) {
            throw CustomException(ACTIVITY_05)
        }

        val block = Block(
            fromId = memberId,
            toId = targetId
        )
        blockRepository.save(block)
    }

    @Transactional
    fun delete(memberId: Long, targetId: Long) {
        val count = blockRepository.deleteByFromIdAndToId(memberId, targetId)

        if (count == 0) {
            throw CustomException(ACTIVITY_06)
        }
    }
}