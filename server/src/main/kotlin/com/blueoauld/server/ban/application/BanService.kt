package com.blueoauld.server.ban.application

import com.blueoauld.server.ban.application.request.BanCreateRequest
import com.blueoauld.server.ban.application.response.BanCreateResponse
import com.blueoauld.server.ban.entity.Ban
import com.blueoauld.server.ban.repository.BanRepository
import com.blueoauld.server.common.exception.CustomException
import com.blueoauld.server.common.exception.type.ErrorCode.BAN_01
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant

@Service
class BanService(

    private val banRepository: BanRepository,
) {

    @Transactional
    fun create(request: BanCreateRequest): BanCreateResponse {
        if (banRepository.existsByTypeAndTarget(request.type, request.target)) {
            throw CustomException(BAN_01)
        }

        val ban = Ban(
            type = request.type,
            target = request.target,
            reason = request.reason,
            expiredAt = Instant.now().plus(Duration.ofDays(request.days.toLong())),
        )
        banRepository.save(ban)

        return BanCreateResponse(
            banId = ban.id,
            type = ban.type,
            target = ban.target,
            reason = ban.reason,
            createdAt = ban.createdAt,
            expiredAt = ban.expiredAt
        )
    }
}