package com.blueoauld.server.member.application

import com.blueoauld.server.common.exception.CustomException
import com.blueoauld.server.common.exception.type.ErrorCode.FILE_02
import com.blueoauld.server.member.application.request.MemberImageCreateRequest
import com.blueoauld.server.member.application.response.MemberImageMoveTask
import com.blueoauld.server.member.entity.MemberImage
import com.blueoauld.server.member.entity.type.MemberImageType

/**
 * 회원 이미지 동기화의 순수 계산 결과.
 *
 * 요청된 이미지 목록과 기존 이미지 목록을 비교해 삽입/삭제 대상, 객체 스토리지 이동 작업,
 * 대표 이미지 URL 을 산출한다. DB·외부 I/O 가 없는 순수 함수이므로 단독으로 테스트할 수 있다.
 * 유지되는 기존 이미지의 정렬 순서(sortOrder)는 [of] 호출 시 인메모리로 갱신된다.
 */
data class MemberImageSyncPlan(

    val toInsert: List<MemberImage>,
    val toDelete: List<MemberImage>,
    val moveTasks: List<MemberImageMoveTask>,
    val firstImageUrl: String?,
) {

    val deleteKeys: List<String>
        get() = toDelete.map { it.key }

    companion object {

        fun of(
            memberId: Long,
            type: MemberImageType,
            domain: String,
            existingImages: List<MemberImage>,
            requestedImages: List<MemberImageCreateRequest>,
        ): MemberImageSyncPlan {
            val pathPrefix = type.name.lowercase()
            val existingKeys = existingImages.map { it.key }.toSet()

            // 검증: 요청 이미지는 신규 업로드(temporary 경로)이거나 기존 이미지여야 한다
            requestedImages.forEach { image ->
                val isNew = image.key.startsWith("member/$pathPrefix/temporary/$memberId/")
                val isExisting = image.key in existingKeys

                if (!isNew && !isExisting) {
                    throw CustomException(FILE_02)
                }
            }

            // 요청에서 원하는 상태 구성(목적지 키 → 정렬 순서 / 목적지 키 → 원본 키)
            val desiredOrderByKey = requestedImages.mapIndexed { index, image ->
                val fileName = image.key.substringAfterLast("/")
                "member/$pathPrefix/$memberId/$fileName" to index
            }.toMap()

            val sourceByDestination = requestedImages.associate { image ->
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
                        url = "$domain/$key",
                        type = type,
                        sortOrder = sortOrder,
                    )
                }

            // 유지 대상: 정렬 순서 갱신
            existingImages
                .filter { it.key in desiredOrderByKey }
                .forEach { existing ->
                    val newSortOrder = desiredOrderByKey.getValue(existing.key)
                    if (existing.sortOrder != newSortOrder) {
                        existing.updateSortOrder(newSortOrder)
                    }
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
                ?.let { "$domain/$it" }

            return MemberImageSyncPlan(
                toInsert = toInsert,
                toDelete = toDelete,
                moveTasks = moveTasks,
                firstImageUrl = firstImageUrl,
            )
        }
    }
}
