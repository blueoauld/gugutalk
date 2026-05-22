package com.blueoauld.server.member.application

import com.blueoauld.server.activity.repository.*
import com.blueoauld.server.common.exception.CustomException
import com.blueoauld.server.common.exception.type.ErrorCode.MEMBER_01
import com.blueoauld.server.member.application.request.UpdateCommentRequest
import com.blueoauld.server.member.application.response.MemberGetResponse
import com.blueoauld.server.member.repository.MemberRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Year

@Service
class MemberService(

    private val memberRepository: MemberRepository,
    private val likeRepository: LikeRepository,
    private val unlikeRepository: UnlikeRepository,
    private val reviewRepository: ReviewRepository,
    private val privateImageGrantRepository: PrivateImageGrantRepository,
    private val blockRepository: BlockRepository,
) {

    @Transactional
    fun updateComment(memberId: Long, request: UpdateCommentRequest) {
        val member = memberRepository.findByIdOrNull(memberId) ?: throw CustomException(MEMBER_01)

        member.updateComment(request.content)
    }

    @Transactional
    fun bump(memberId: Long) {
        val member = memberRepository.findByIdOrNull(memberId) ?: throw CustomException(MEMBER_01)

        member.bump()
    }

    @Transactional(readOnly = true)
    fun get(memberId: Long, targetId: Long): MemberGetResponse {
        val target = memberRepository.findByIdOrNull(targetId) ?: throw CustomException(MEMBER_01)

        return MemberGetResponse(
            memberId = target.id,
            nickname = target.nickname,
            gender = target.gender,
            age = Year.now().value - target.birthYear,
            region = target.region,
            bio = target.bio,
            isChat = target.isChat,
            updatedAt = target.updatedAt,
            likes = likeRepository.countByToId(targetId),
            unlikes = unlikeRepository.countByToId(targetId),
            reviews = reviewRepository.countByToId(targetId),
            isLike = likeRepository.existsByFromIdAndToId(memberId, targetId),
            isUnlike = unlikeRepository.existsByFromIdAndToId(memberId, targetId),
            isPrivateImageGrant = privateImageGrantRepository.existsByFromIdAndToId(memberId, targetId),
            hasPrivateImageGrant = privateImageGrantRepository.existsByFromIdAndToId(targetId, memberId),
            isBlock = blockRepository.existsByFromIdAndToId(memberId, targetId),
        )
    }
}