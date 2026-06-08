package com.blueoauld.server.ban.application

import com.blueoauld.server.ban.application.request.BanCreateRequest
import com.blueoauld.server.ban.application.response.BanCreateResponse
import com.blueoauld.server.ban.application.response.BanGetResponse
import com.blueoauld.server.ban.entity.Ban
import com.blueoauld.server.ban.entity.BanHistory
import com.blueoauld.server.ban.entity.type.BanType
import com.blueoauld.server.ban.repository.BanHistoryRepository
import com.blueoauld.server.ban.repository.BanRepository
import com.blueoauld.server.common.exception.CustomException
import com.blueoauld.server.common.exception.type.ErrorCode.BAN_01
import com.blueoauld.server.common.exception.type.ErrorCode.BAN_02
import com.blueoauld.server.member.entity.Member
import com.blueoauld.server.member.repository.MemberRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant

@Service
class BanService(

    private val banRepository: BanRepository,
    private val banHistoryRepository: BanHistoryRepository,
    private val memberRepository: MemberRepository,
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

        val members = when (request.type) {
            BanType.PHONE -> listOfNotNull(memberRepository.findByPhone(request.target))
            BanType.DEVICE -> memberRepository.findAllByDeviceId(request.target)
            BanType.ACCOUNT -> {
                val id = request.target.toLongOrNull() ?: throw CustomException(BAN_02)

                listOfNotNull(memberRepository.findByIdOrNull(id))
            }
        }
        members.forEach {
            saveBanHistory(ban, it)
        }

        return BanCreateResponse(
            banId = ban.id,
            uuid = ban.uuid,
            type = ban.type,
            target = ban.target,
            reason = ban.reason,
            createdAt = ban.createdAt,
            expiredAt = ban.expiredAt
        )
    }

    @Transactional
    fun delete(uuid: String) {
        val ban = banRepository.findByUuid(uuid) ?: throw CustomException(BAN_02)

        banRepository.delete(ban)
    }

    @Transactional(readOnly = true)
    fun get(uuid: String): BanGetResponse {
        val ban = banRepository.findByUuid(uuid) ?: throw CustomException(BAN_02)

        return BanGetResponse(
            banId = ban.id,
            uuid = ban.uuid,
            type = ban.type,
            target = ban.target,
            reason = ban.reason,
            days = Duration.between(ban.createdAt, ban.expiredAt).toDays().toInt() + 1,
            createdAt = ban.createdAt,
            expiredAt = ban.expiredAt
        )
    }

    private fun saveBanHistory(ban: Ban, member: Member) {
        banHistoryRepository.save(
            BanHistory(
                banId = ban.id,
                type = ban.type,
                target = ban.target,
                phone = member.phone,
                deviceId = member.deviceId,
                nickname = member.nickname,
                reason = ban.reason,
                expiredAt = ban.expiredAt,
            )
        )
    }
}