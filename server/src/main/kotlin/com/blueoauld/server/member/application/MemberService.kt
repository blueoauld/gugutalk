package com.blueoauld.server.member.application

import com.blueoauld.server.activity.repository.PrivateImageGrantRepository
import com.blueoauld.server.common.dto.response.CursorResponse
import com.blueoauld.server.common.exception.CustomException
import com.blueoauld.server.common.exception.type.ErrorCode.*
import com.blueoauld.server.common.properties.R2Properties
import com.blueoauld.server.member.application.event.MemberUpdateProfileEvent
import com.blueoauld.server.member.application.request.MemberImageCreateRequest
import com.blueoauld.server.member.application.request.MemberUpdateCommentRequest
import com.blueoauld.server.member.application.request.MemberUpdateProfileRequest
import com.blueoauld.server.member.application.response.*
import com.blueoauld.server.member.entity.MemberImage
import com.blueoauld.server.member.entity.type.MemberImageType
import com.blueoauld.server.member.entity.type.MemberImageType.PRIVATE
import com.blueoauld.server.member.entity.type.MemberImageType.PUBLIC
import com.blueoauld.server.member.repository.MemberImageRepository
import com.blueoauld.server.member.repository.MemberRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class MemberService(

    private val memberRepository: MemberRepository,
    private val memberImageRepository: MemberImageRepository,
    private val privateImageGrantRepository: PrivateImageGrantRepository,
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

    @Transactional
    fun toggleChat(memberId: Long) {
        val member = memberRepository.findByIdOrNull(memberId) ?: throw CustomException(MEMBER_01)

        member.toggleChat()
    }

    @Transactional(readOnly = true)
    fun get(memberId: Long, targetId: Long): MemberGetResponse {
        val result = memberRepository.findDetailById(memberId, targetId) ?: throw CustomException(MEMBER_01)
        val memberImages = memberImageRepository.findAllByMemberId(targetId).map { MemberImageResponse.from(it) }

        return MemberGetResponse.from(
            result = result,
            images = memberImages.filter { it.type == PUBLIC },
            privateImages = memberImages.count { it.type == PRIVATE },
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

    @Transactional(readOnly = true)
    fun getMe(memberId: Long): MemberGetMeResponse {
        val member = memberRepository.findByIdOrNull(memberId) ?: throw CustomException(MEMBER_01)
        val memberImages = memberImageRepository.findAllByMemberId(member.id).map { MemberImageResponse.from(it) }

        return MemberGetMeResponse(
            memberId = member.id,
            images = memberImages,
            nickname = member.nickname,
            birthYear = member.birthYear,
            region = member.region,
            bio = member.bio
        )
    }

    @Transactional
    fun updateProfile(memberId: Long, request: MemberUpdateProfileRequest) {
        val member = memberRepository.findByIdOrNull(memberId) ?: throw CustomException(MEMBER_01)

        if (member.nickname != request.nickname && memberRepository.existsByNickname(request.nickname)) {
            throw CustomException(MEMBER_03)
        }

        val publicImageResult = syncImages(member.id, request.publicImages, PUBLIC)
        val privateImageResult = syncImages(member.id, request.privateImages, PRIVATE)

        val moveTasks = publicImageResult.moveTasks + privateImageResult.moveTasks
        val deleteKeys = publicImageResult.deleteKeys + privateImageResult.deleteKeys

        member.updateProfile(
            profileUrl = publicImageResult.firstImageUrl,
            nickname = request.nickname,
            birthYear = request.birthYear,
            region = request.region,
            bio = request.bio,
        )

        // 이벤트
        if (moveTasks.isNotEmpty() || deleteKeys.isNotEmpty()) {
            applicationEventPublisher.publishEvent(
                MemberUpdateProfileEvent(
                    memberId = member.id,
                    moveTasks = moveTasks,
                    deleteKeys = deleteKeys,
                )
            )
        }
    }

    @Transactional(readOnly = true)
    fun getPrivateImages(memberId: Long, targetId: Long): MemberGetPrivateImagesResponse {
        if (!privateImageGrantRepository.existsByFromIdAndToId(targetId, memberId)) {
            throw CustomException(ACTIVITY_12)
        }

        val member = memberRepository.findByIdOrNull(memberId) ?: throw CustomException(MEMBER_01)
        val memberImages = memberImageRepository.findAllByMemberIdAndType(targetId, PRIVATE).map {
            MemberImageResponse.from(it)
        }

        return MemberGetPrivateImagesResponse(member.phone, memberImages)
    }

    @Transactional(readOnly = true)
    fun isChat(memberId: Long): MemberIsChatResponse {
        val member = memberRepository.findByIdOrNull(memberId) ?: throw CustomException(MEMBER_01)
        return MemberIsChatResponse(member.isChat)
    }

    private fun syncImages(
        memberId: Long,
        images: List<MemberImageCreateRequest>,
        type: MemberImageType,
    ): MemberImageSyncResult {
        val pathPrefix = type.name.lowercase()

        // 기존 이미지 조회
        val existingImages = memberImageRepository.findAllByMemberIdAndType(memberId, type)
        val existingKeys = existingImages.map { it.key }.toSet()

        // 검증
        images.forEach { image ->
            val isNew = image.key.startsWith("member/$pathPrefix/temporary/$memberId/")
            val isExisting = image.key in existingKeys

            if (!isNew && !isExisting) {
                throw CustomException(FILE_02)
            }
        }

        // 요청에서 원하는 상태 구성
        val desiredOrderByKey = images.mapIndexed { index, image ->
            val fileName = image.key.substringAfterLast("/")
            "member/$pathPrefix/$memberId/$fileName" to index
        }.toMap()

        val sourceByDestination = images.associate { image ->
            val fileName = image.key.substringAfterLast("/")
            "member/$pathPrefix/$memberId/$fileName" to image.key
        }

        // 삭제 대상
        val toDelete = existingImages.filter { it.key !in desiredOrderByKey }

        // 추가 대상
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

        // 유지 대상
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

        val moveTasks = desiredOrderByKey.keys
            .filter { it !in existingKeys }
            .map { destinationKey ->
                MemberImageMoveTask(
                    sourceKey = sourceByDestination.getValue(destinationKey),
                    destinationKey = destinationKey,
                )
            }

        val firstImageUrl = desiredOrderByKey.entries
            .firstOrNull { (_, sortOrder) -> sortOrder == 0 }
            ?.key
            ?.let { "${r2Properties.domain}/$it" }

        return MemberImageSyncResult(
            moveTasks = moveTasks,
            deleteKeys = toDelete.map { it.key },
            firstImageUrl = firstImageUrl,
        )
    }
}