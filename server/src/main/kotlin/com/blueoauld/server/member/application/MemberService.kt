package com.blueoauld.server.member.application

import com.blueoauld.server.activity.repository.*
import com.blueoauld.server.common.dto.response.CursorResponse
import com.blueoauld.server.common.exception.CustomException
import com.blueoauld.server.common.exception.type.ErrorCode.*
import com.blueoauld.server.common.properties.R2Properties
import com.blueoauld.server.member.application.event.MemberUpdateProfileEvent
import com.blueoauld.server.member.application.request.MemberImageCreateRequest
import com.blueoauld.server.member.application.request.MemberUpdateCommentRequest
import com.blueoauld.server.member.application.request.MemberUpdateProfileRequest
import com.blueoauld.server.member.application.response.MemberGetResponse
import com.blueoauld.server.member.application.response.MemberImageMoveTask
import com.blueoauld.server.member.application.response.MemberRowResponse
import com.blueoauld.server.member.application.response.MemberSearchRowResponse
import com.blueoauld.server.member.entity.MemberImage
import com.blueoauld.server.member.entity.type.MemberImageType
import com.blueoauld.server.member.repository.MemberImageRepository
import com.blueoauld.server.member.repository.MemberRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.Year

@Service
class MemberService(

    private val memberRepository: MemberRepository,
    private val memberImageRepository: MemberImageRepository,
    private val likeRepository: LikeRepository,
    private val unlikeRepository: UnlikeRepository,
    private val reviewRepository: ReviewRepository,
    private val privateImageGrantRepository: PrivateImageGrantRepository,
    private val blockRepository: BlockRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val r2Properties: R2Properties,
) {

    @Transactional
    fun updateComment(memberId: Long, request: MemberUpdateCommentRequest) {
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

    @Transactional(readOnly = true)
    fun gets(
        memberId: Long,
        gender: String,
        cursorId: Long?,
        cursorDateAt: Instant?,
        size: Int
    ): CursorResponse<MemberRowResponse> {
        val result = memberRepository.findAllByCursor(
            memberId = memberId,
            gender = gender,
            region = null,
            cursorId = cursorId,
            cursorDateAt = cursorDateAt,
            size = size + 1
        ).map {
            MemberRowResponse.from(it)
        }

        val hasNext = result.size > size
        val items = if (hasNext) result.dropLast(1) else result
        val last = items.lastOrNull()

        return CursorResponse(
            payload = items,
            nextId = last?.memberId,
            nextDateAt = last?.updatedAt,
            hasNext = hasNext
        )
    }

    @Transactional(readOnly = true)
    fun getsByRegion(
        memberId: Long,
        gender: String,
        cursorId: Long?,
        cursorDateAt: Instant?,
        size: Int
    ): CursorResponse<MemberRowResponse> {
        val member = memberRepository.findByIdOrNull(memberId) ?: throw CustomException(MEMBER_01)

        val result = memberRepository.findAllByCursor(
            memberId = memberId,
            gender = gender,
            region = member.region,
            cursorId = cursorId,
            cursorDateAt = cursorDateAt,
            size = size + 1
        ).map {
            MemberRowResponse.from(it)
        }

        val hasNext = result.size > size
        val items = if (hasNext) result.dropLast(1) else result
        val last = items.lastOrNull()

        return CursorResponse(
            payload = items,
            nextId = last?.memberId,
            nextDateAt = last?.updatedAt,
            hasNext = hasNext
        )
    }

    @Transactional(readOnly = true)
    fun search(
        memberId: Long,
        nickname: String,
        cursorId: Long?,
        cursorDateAt: Instant?,
        size: Int
    ): CursorResponse<MemberSearchRowResponse> {
        if (nickname.length < 2) {
            throw CustomException(SEARCH_01)
        }

        val result = memberRepository.findAllByNickname(
            memberId = memberId,
            nickname = nickname,
            cursorId = cursorId,
            cursorDateAt = cursorDateAt,
            size = size + 1
        ).map {
            MemberSearchRowResponse.from(it)
        }

        val hasNext = result.size > size
        val items = if (hasNext) result.dropLast(1) else result
        val last = items.lastOrNull()

        return CursorResponse(
            payload = items,
            nextId = last?.memberId,
            nextDateAt = last?.updatedAt,
            hasNext = hasNext
        )
    }

    @Transactional
    fun updateProfile(memberId: Long, request: MemberUpdateProfileRequest) {
        val member = memberRepository.findByIdOrNull(memberId) ?: throw CustomException(MEMBER_01)

        if (member.nickname != request.nickname && memberRepository.existsByNickname(request.nickname)) {
            throw CustomException(MEMBER_03)
        }

        val moveTasks = syncImages(member.id, request.publicImages, MemberImageType.PUBLIC) +
                syncImages(member.id, request.privateImages, MemberImageType.PRIVATE)

        member.updateProfile(
            nickname = request.nickname,
            birthYear = request.birthYear,
            region = request.region,
            bio = request.bio,
        )

        // 이벤트
        if (moveTasks.isNotEmpty()) {
            applicationEventPublisher.publishEvent(
                MemberUpdateProfileEvent(memberId = member.id, moveTasks = moveTasks)
            )
        }
    }

    private fun syncImages(
        memberId: Long,
        images: List<MemberImageCreateRequest>,
        type: MemberImageType
    ): List<MemberImageMoveTask> {
        val pathPrefix = type.name.lowercase()

        // 1. 요청에서 원하는 상태 구성
        val desiredOrderByKey = images.mapIndexed { index, image ->
            val fileName = image.key.substringAfterLast("/")
            "member/$pathPrefix/$memberId/$fileName" to index
        }.toMap()

        val sourceByDestination = images.associate { image ->
            val fileName = image.key.substringAfterLast("/")
            "member/$pathPrefix/$memberId/$fileName" to image.key
        }

        // 2. 기존 이미지 조회
        val existingImages = memberImageRepository.findAllByMemberIdAndType(memberId, type)
        val existingKeys = existingImages.map { it.key }.toSet()

        // 3. 삭제 대상 (DB에는 있는데 요청엔 없는 것)
        val toDelete = existingImages.filter { it.key !in desiredOrderByKey }

        // 4. 추가 대상 (요청에는 있는데 DB엔 없는 것)
        val toInsert = desiredOrderByKey
            .filterKeys { it !in existingKeys }
            .map { (key, sortOrder) ->
                MemberImage(
                    memberId = memberId,
                    key = key,
                    url = "${r2Properties.domain}/$key",
                    type = type,
                    sortOrder = sortOrder,
                )
            }

        // 5. 유지 대상 (sortOrder만 바뀌었으면 업데이트)
        existingImages
            .filter { it.key in desiredOrderByKey }
            .forEach { existing ->
                val newSortOrder = desiredOrderByKey.getValue(existing.key)

                if (existing.sortOrder != newSortOrder) {
                    existing.updateSortOrder(newSortOrder)
                }
            }

        if (toDelete.isNotEmpty()) {
            memberImageRepository.deleteAll(toDelete)
        }
        if (toInsert.isNotEmpty()) {
            memberImageRepository.saveAll(toInsert)
        }

        return desiredOrderByKey.keys
            .filter { it !in existingKeys }
            .map { destinationKey ->
                MemberImageMoveTask(
                    sourceKey = sourceByDestination.getValue(destinationKey),
                    destinationKey = destinationKey,
                )
            }
    }
}