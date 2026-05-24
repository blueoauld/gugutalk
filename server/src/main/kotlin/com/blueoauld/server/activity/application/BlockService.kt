package com.blueoauld.server.activity.application

import com.blueoauld.server.activity.application.response.ActivityRowResponse
import com.blueoauld.server.activity.entity.Block
import com.blueoauld.server.activity.repository.BlockRepository
import com.blueoauld.server.common.dto.response.CursorResponse
import com.blueoauld.server.common.exception.CustomException
import com.blueoauld.server.common.exception.type.ErrorCode.ACTIVITY_05
import com.blueoauld.server.common.exception.type.ErrorCode.ACTIVITY_06
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class BlockService(

    private val blockRepository: BlockRepository,
) {

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

    @Transactional(readOnly = true)
    fun gets(
        memberId: Long,
        cursorId: Long?,
        cursorDateAt: Instant?,
        size: Int
    ): CursorResponse<ActivityRowResponse> {
        val result = blockRepository.findAllByCursor(
            memberId = memberId,
            cursorId = cursorId,
            cursorDateAt = cursorDateAt,
            size = size + 1
        ).map {
            ActivityRowResponse.from(it)
        }

        val hasNext = result.size > size
        val items = if (hasNext) result.dropLast(1) else result
        val last = items.lastOrNull()

        return CursorResponse(
            payload = items,
            nextId = last?.activityId,
            nextDateAt = last?.createdAt,
            hasNext = hasNext
        )
    }
}