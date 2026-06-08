package com.blueoauld.server.admin.application

import com.blueoauld.server.admin.application.event.AdminMemberResetImageEvent
import com.blueoauld.server.admin.application.response.AdminGetMemberResponse
import com.blueoauld.server.admin.application.type.ResetTarget
import com.blueoauld.server.admin.application.type.ResetTarget.*
import com.blueoauld.server.common.exception.CustomException
import com.blueoauld.server.common.exception.type.ErrorCode.MEMBER_01
import com.blueoauld.server.member.entity.type.MemberImageType.PRIVATE
import com.blueoauld.server.member.entity.type.MemberImageType.PUBLIC
import com.blueoauld.server.member.repository.MemberImageRepository
import com.blueoauld.server.member.repository.MemberRepository
import com.blueoauld.server.r2.application.R2Provider
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class AdminService(

    private val memberRepository: MemberRepository,
    private val memberImageRepository: MemberImageRepository,
    private val r2Provider: R2Provider,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {

    @Transactional(readOnly = true)
    fun getMember(nickname: String): AdminGetMemberResponse {
        val member = memberRepository.findByNickname(nickname) ?: throw CustomException(MEMBER_01)
        val memberImages = memberImageRepository.findAllByMemberId(member.id)

        return AdminGetMemberResponse.from(
            member = member,
            publicImages = memberImages.filter { it.type == PUBLIC }.map { it.url },
            privateImages = memberImages.filter { it.type == PRIVATE }.map { r2Provider.createDownloadUrl(it.key) }
        )
    }

    @Transactional
    fun resetMember(nickname: String, target: ResetTarget) {
        val member = memberRepository.findByNickname(nickname) ?: throw CustomException(MEMBER_01)

        when (target) {
            NICKNAME -> member.nickname = "닉네임_${UUID.randomUUID().toString().replace("-", "").take(6)}"
            COMMENT -> member.comment = "안녕하세요."
            BIO -> member.bio = ""
            PUBLIC_IMAGES -> {
                val memberImages = memberImageRepository.findAllByMemberIdAndType(member.id, PUBLIC)
                val keys = memberImages.map { it.key }

                memberImageRepository.deleteAll(memberImages)
                member.profileUrl = null

                // 이벤트
                applicationEventPublisher.publishEvent(AdminMemberResetImageEvent(keys))
            }

            PRIVATE_IMAGES -> {
                val memberImages = memberImageRepository.findAllByMemberIdAndType(member.id, PRIVATE)
                val keys = memberImages.map { it.key }

                memberImageRepository.deleteAll(memberImages)

                // 이벤트
                applicationEventPublisher.publishEvent(AdminMemberResetImageEvent(keys))
            }
        }
    }
}