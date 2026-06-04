package com.blueoauld.server.admin.application

import com.blueoauld.server.admin.application.response.AdminGetMemberResponse
import com.blueoauld.server.common.exception.CustomException
import com.blueoauld.server.common.exception.type.ErrorCode.MEMBER_01
import com.blueoauld.server.member.entity.type.MemberImageType.PRIVATE
import com.blueoauld.server.member.entity.type.MemberImageType.PUBLIC
import com.blueoauld.server.member.repository.MemberImageRepository
import com.blueoauld.server.member.repository.MemberRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminService(

    private val memberRepository: MemberRepository,
    private val memberImageRepository: MemberImageRepository
) {

    @Transactional(readOnly = true)
    fun getMember(nickname: String): AdminGetMemberResponse {
        val member = memberRepository.findByNickname(nickname) ?: throw CustomException(MEMBER_01)
        val memberImages = memberImageRepository.findAllByMemberId(member.id)

        return AdminGetMemberResponse.from(
            member = member,
            publicImages = memberImages.filter { it.type == PUBLIC }.map { it.url },
            privateImages = memberImages.filter { it.type == PRIVATE }.map { it.url }
        )
    }
}