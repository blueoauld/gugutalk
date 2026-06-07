package com.blueoauld.server.member.application

import com.blueoauld.server.activity.repository.*
import com.blueoauld.server.member.repository.MemberImageRepository
import com.blueoauld.server.member.repository.MemberRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MemberCleanupService(

    private val memberRepository: MemberRepository,
    private val memberImageRepository: MemberImageRepository,
    private val likeRepository: LikeRepository,
    private val unlikeRepository: UnlikeRepository,
    private val privateImageGrantRepository: PrivateImageGrantRepository,
    private val blockRepository: BlockRepository,
    private val reviewRepository: ReviewRepository,
) {

    @Transactional
    fun deleteBatch(ids: List<Long>): List<String> {
        if (ids.isEmpty()) {
            return emptyList()
        }

        val imageKeys = memberImageRepository.findKeysByMemberIds(ids)

        likeRepository.deleteByMemberIds(ids)
        unlikeRepository.deleteByMemberIds(ids)
        privateImageGrantRepository.deleteByMemberIds(ids)
        blockRepository.deleteByMemberIds(ids)
        reviewRepository.deleteByMemberIds(ids)
        memberImageRepository.deleteByMemberIds(ids)
        memberRepository.hardDeleteByIds(ids)

        return imageKeys
    }
}