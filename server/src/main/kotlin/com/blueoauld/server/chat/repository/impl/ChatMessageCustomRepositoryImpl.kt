package com.blueoauld.server.chat.repository.impl

import com.blueoauld.server.chat.entity.ChatMessage
import com.blueoauld.server.chat.entity.ChatMessageReaction
import com.blueoauld.server.chat.repository.ChatMessageCustomRepository
import com.blueoauld.server.chat.repository.result.ChatMessageProjection
import com.blueoauld.server.chat.repository.result.ChatMessageReactionResult
import com.blueoauld.server.chat.repository.result.ChatMessageResult
import com.linecorp.kotlinjdsl.dsl.jpql.jpql
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderContext
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderer
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class ChatMessageCustomRepositoryImpl(

    private val entityManager: EntityManager,
    private val jpqlRenderContext: JpqlRenderContext,
) : ChatMessageCustomRepository {

    private val jpqlRenderer = JpqlRenderer()

    override fun findAllByCursor(
        memberId: Long,
        chatRoomId: Long,
        cursorId: Long?,
        cursorDateAt: Instant?,
        size: Int
    ): List<ChatMessageResult> {
        // 채팅 메세지
        val messageQuery = jpql {
            selectNew<ChatMessageProjection>(
                path(ChatMessage::id),
                path(ChatMessage::senderId),
                path(ChatMessage::content),
                path(ChatMessage::type),
                path(ChatMessage::createdAt),
            ).from(
                entity(ChatMessage::class),
            ).whereAnd(
                path(ChatMessage::chatRoomId).eq(chatRoomId),
                if (cursorId != null && cursorDateAt != null) {
                    or(
                        path(ChatMessage::createdAt).lt(cursorDateAt),
                        and(
                            path(ChatMessage::createdAt).eq(cursorDateAt),
                            path(ChatMessage::id).lt(cursorId),
                        ),
                    )
                } else null,
            ).orderBy(
                path(ChatMessage::createdAt).desc(),
                path(ChatMessage::id).desc(),
            )
        }

        val renderedMessage = jpqlRenderer.render(messageQuery, jpqlRenderContext)
        val messages = entityManager
            .createQuery(renderedMessage.query, ChatMessageProjection::class.java)
            .apply {
                renderedMessage.params.forEach { (name, value) -> setParameter(name, value) }
            }
            .setMaxResults(size)
            .resultList

        if (messages.isEmpty()) {
            return emptyList()
        }

        // 채팅 메세지 반응
        val messageIds = messages.map { it.chatMessageId }

        val reactionQuery = jpql {
            selectNew<ChatMessageReactionResult>(
                path(ChatMessageReaction::chatMessageId),
                path(ChatMessageReaction::memberId),
                path(ChatMessageReaction::type),
            ).from(
                entity(ChatMessageReaction::class),
            ).where(
                path(ChatMessageReaction::chatMessageId).`in`(messageIds),
            )
        }

        val renderedReaction = jpqlRenderer.render(reactionQuery, jpqlRenderContext)
        val reactions = entityManager
            .createQuery(renderedReaction.query, ChatMessageReactionResult::class.java)
            .apply {
                renderedReaction.params.forEach { (name, value) -> setParameter(name, value) }
            }
            .resultList

        val reactionsByMessageId: Map<Long, List<ChatMessageReactionResult>> = reactions.groupBy { it.chatMessageId }

        return messages.map { message ->
            ChatMessageResult(
                chatMessageId = message.chatMessageId,
                senderId = message.senderId,
                content = message.content,
                type = message.type,
                createdAt = message.createdAt,
                reactions = reactionsByMessageId[message.chatMessageId] ?: emptyList(),
            )
        }
    }
}