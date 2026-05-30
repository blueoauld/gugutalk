package com.blueoauld.server.chat.repository.impl

import com.blueoauld.server.chat.entity.ChatMessage
import com.blueoauld.server.chat.repository.ChatMessageCustomRepository
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
        val query = jpql {
            selectNew<ChatMessageResult>(
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

        val rendered = jpqlRenderer.render(query, jpqlRenderContext)
        val jpaQuery = entityManager.createQuery(rendered.query, ChatMessageResult::class.java).apply {
            rendered.params.forEach { (name, value) ->
                setParameter(name, value)
            }
        }
        return jpaQuery.setMaxResults(size).resultList
    }
}