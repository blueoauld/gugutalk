package com.blueoauld.server.chat.application

import com.blueoauld.server.chat.application.event.ChatMessageSendEvent
import com.blueoauld.server.chat.application.event.ChatMessageUploadMediaEvent
import com.blueoauld.server.chat.application.event.ChatRoomUpsertEvent
import com.blueoauld.server.chat.application.request.ChatMessageMediaUploadRequest
import com.blueoauld.server.chat.application.request.ChatMessageSendRequest
import com.blueoauld.server.chat.application.response.ChatMessageGetVideoResponse
import com.blueoauld.server.chat.application.response.ChatMessageRowResponse
import com.blueoauld.server.chat.entity.ChatMessage
import com.blueoauld.server.chat.entity.ChatMessageMedia
import com.blueoauld.server.chat.entity.type.MessageType
import com.blueoauld.server.chat.repository.ChatMessageMediaRepository
import com.blueoauld.server.chat.repository.ChatMessageRepository
import com.blueoauld.server.chat.repository.ChatRoomRepository
import com.blueoauld.server.common.dto.response.CursorResponse
import com.blueoauld.server.common.exception.CustomException
import com.blueoauld.server.common.exception.type.ErrorCode.*
import com.blueoauld.server.common.properties.R2Properties
import com.blueoauld.server.member.repository.MemberRepository
import com.blueoauld.server.r2.application.R2Provider
import com.blueoauld.server.r2.application.request.UploadUrlRequests
import com.blueoauld.server.r2.application.response.UploadUrlResponses
import com.blueoauld.server.r2.type.FileContentType
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant
import java.util.*

@Service
class ChatMessageService(

    private val chatMessageRepository: ChatMessageRepository,
    private val chatRoomRepository: ChatRoomRepository,
    private val chatMessageMediaRepository: ChatMessageMediaRepository,
    private val memberRepository: MemberRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val r2Provider: R2Provider,
    private val r2Properties: R2Properties,
) {

    @Transactional(readOnly = true)
    fun gets(
        memberId: Long,
        chatRoomId: Long,
        cursorId: Long?,
        cursorDateAt: Instant?,
        size: Int
    ): CursorResponse<ChatMessageRowResponse> {
        val chatRoom = chatRoomRepository.findByIdOrNull(chatRoomId) ?: throw CustomException(CHAT_03)

        if (chatRoom.member1Id != memberId && chatRoom.member2Id != memberId) {
            throw CustomException(CHAT_02)
        }

        val result = chatMessageRepository.findAllByCursor(
            memberId = memberId,
            chatRoomId = chatRoomId,
            cursorId = cursorId,
            cursorDateAt = cursorDateAt,
            size = size + 1
        ).map {
            ChatMessageRowResponse.from(it)
        }

        val hasNext = result.size > size
        val items = if (hasNext) result.dropLast(1) else result
        val last = items.lastOrNull()

        return CursorResponse(
            payload = items,
            nextId = last?.chatMessageId,
            nextDateAt = last?.createdAt,
            hasNext = hasNext
        )
    }

    @Transactional
    fun send(memberId: Long, chatRoomId: Long, request: ChatMessageSendRequest) {
        val chatRoom = chatRoomRepository.findByIdOrNull(chatRoomId) ?: throw CustomException(CHAT_03)

        if (chatRoom.member1Id != memberId && chatRoom.member2Id != memberId) {
            throw CustomException(CHAT_02)
        }

        val targetId = chatRoom.getOtherMemberId(memberId)
        val member = memberRepository.findByIdOrNull(memberId) ?: throw CustomException(MEMBER_01)
        val target = memberRepository.findByIdOrNull(targetId) ?: throw CustomException(MEMBER_01)

        val chatMessage = ChatMessage(
            chatRoomId = chatRoomId,
            senderId = memberId,
            type = MessageType.TEXT,
            content = request.content
        )
        chatMessageRepository.save(chatMessage)
        chatRoom.onMessageSent(memberId, chatMessage)

        // 이벤트
        applicationEventPublisher.publishEvent(
            ChatMessageSendEvent(
                chatMessageId = chatMessage.id,
                chatRoomId = chatRoomId,
                senderId = memberId,
                content = request.content,
                type = MessageType.TEXT,
                createdAt = chatMessage.createdAt
            )
        )

        applicationEventPublisher.publishEvent(
            ChatRoomUpsertEvent(
                chatRoomId = chatRoomId,
                targetId = targetId,
                memberId = memberId,
                senderId = memberId,
                nickname = member.nickname,
                profileUrl = member.profileUrl,
                lastMessagePreview = request.content.take(100),
                lastMessageAt = chatMessage.createdAt,
            )
        )

        applicationEventPublisher.publishEvent(
            ChatRoomUpsertEvent(
                chatRoomId = chatRoomId,
                targetId = memberId,
                memberId = targetId,
                senderId = memberId,
                nickname = target.nickname,
                profileUrl = target.profileUrl,
                lastMessagePreview = request.content.take(100),
                lastMessageAt = chatMessage.createdAt,
            )
        )
    }

    @Transactional
    fun uploadMedia(
        memberId: Long,
        chatRoomId: Long,
        request: ChatMessageMediaUploadRequest
    ) {
        val chatRoom = chatRoomRepository.findByIdOrNull(chatRoomId) ?: throw CustomException(CHAT_03)

        if (chatRoom.member1Id != memberId && chatRoom.member2Id != memberId) {
            throw CustomException(CHAT_02)
        }

        val targetId = chatRoom.getOtherMemberId(memberId)
        val member = memberRepository.findByIdOrNull(memberId) ?: throw CustomException(MEMBER_01)
        val target = memberRepository.findByIdOrNull(targetId) ?: throw CustomException(MEMBER_01)

        val chatMessages = request.media.map { media ->
            val url = "${r2Properties.domain}/chat/$chatRoomId/${media.key.substringAfterLast("/")}"
            val thumbnailUrl = media.thumbnailKey?.let { tk ->
                "${r2Properties.domain}/chat/$chatRoomId/${tk.substringAfterLast("/")}"
            }

            ChatMessage(
                chatRoomId = chatRoomId,
                senderId = memberId,
                type = media.type,
                content = thumbnailUrl ?: url,
            )
        }
        val savedMessages: List<ChatMessage> = chatMessageRepository.saveAll(chatMessages)
        chatRoom.onMessageSent(memberId, savedMessages.last())

        val chatMessageMedia = savedMessages.zip(request.media) { message, media ->
            val key = "chat/$chatRoomId/${media.key.substringAfterLast("/")}"
            val thumbnailKey = media.thumbnailKey?.let { "chat/$chatRoomId/${it.substringAfterLast("/")}" }

            ChatMessageMedia(
                chatMessageId = message.id,
                type = message.type,
                url = "${r2Properties.domain}/$key",
                key = key,
                thumbnailUrl = thumbnailKey?.let { "${r2Properties.domain}/$it" },
                thumbnailKey = thumbnailKey,
            )
        }
        chatMessageMediaRepository.saveAll(chatMessageMedia)

        // 이벤트
        applicationEventPublisher.publishEvent(
            ChatMessageUploadMediaEvent(
                chatRoomId = chatRoomId,
                keys = request.media.flatMap { listOfNotNull(it.key, it.thumbnailKey) }
            )
        )

        savedMessages.forEach { message ->
            applicationEventPublisher.publishEvent(
                ChatMessageSendEvent(
                    chatMessageId = message.id,
                    chatRoomId = chatRoomId,
                    senderId = memberId,
                    content = message.content,
                    type = message.type,
                    createdAt = message.createdAt
                )
            )
        }

        val lastMessage = savedMessages.last()
        val preview = when (lastMessage.type) {
            MessageType.IMAGE -> "이미지"
            MessageType.VIDEO -> "동영상"
            else -> lastMessage.content.take(100)
        }

        applicationEventPublisher.publishEvent(
            ChatRoomUpsertEvent(
                chatRoomId = chatRoomId,
                targetId = targetId,
                memberId = memberId,
                senderId = memberId,
                nickname = member.nickname,
                profileUrl = member.profileUrl,
                lastMessagePreview = preview,
                lastMessageAt = lastMessage.createdAt,
            )
        )

        applicationEventPublisher.publishEvent(
            ChatRoomUpsertEvent(
                chatRoomId = chatRoomId,
                targetId = memberId,
                memberId = targetId,
                senderId = memberId,
                nickname = target.nickname,
                profileUrl = target.profileUrl,
                lastMessagePreview = preview,
                lastMessageAt = lastMessage.createdAt,
            )
        )
    }

    @Transactional(readOnly = true)
    fun getVideo(chatMessageId: Long): ChatMessageGetVideoResponse {
        val chatMessageMedia = (chatMessageMediaRepository.findByChatMessageId(chatMessageId)
            ?: throw CustomException(CHAT_MESSAGE_01))

        if (chatMessageMedia.type != MessageType.VIDEO) {
            throw CustomException(CHAT_MESSAGE_02)
        }

        return ChatMessageGetVideoResponse(
            url = chatMessageMedia.url
        )
    }

    fun createUploadUrls(memberId: Long, chatRoomId: Long, requests: UploadUrlRequests): UploadUrlResponses {
        val chatRoom = chatRoomRepository.findByIdOrNull(chatRoomId) ?: throw CustomException(CHAT_03)

        if (chatRoom.member1Id != memberId && chatRoom.member2Id != memberId) {
            throw CustomException(CHAT_02)
        }

        val urls = requests.urls.map {
            val contentType = FileContentType.from(it.contentType)
            val fileName = "${UUID.randomUUID()}.${contentType.extension}"
            val key = "chat/temporary/$chatRoomId/$fileName"

            r2Provider.createUploadUrl(key, it.contentType, Duration.ofMinutes(5))
        }
        return UploadUrlResponses(urls)
    }
}