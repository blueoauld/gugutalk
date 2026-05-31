package com.blueoauld.server.chat.repository.impl

import com.blueoauld.server.chat.entity.ChatMessage
import com.blueoauld.server.chat.entity.ChatRoom
import com.blueoauld.server.chat.repository.ChatRoomCustomRepository
import com.blueoauld.server.chat.repository.result.ChatRoomResult
import com.blueoauld.server.chat.repository.result.ChatRoomSearchResult
import com.blueoauld.server.member.entity.Member
import com.linecorp.kotlinjdsl.dsl.jpql.jpql
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderContext
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderer
import jakarta.persistence.EntityManager
import org.springframework.data.jpa.repository.query.EscapeCharacter
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class ChatRoomCustomRepositoryImpl(

    private val entityManager: EntityManager,
    private val jpqlRenderContext: JpqlRenderContext,
) : ChatRoomCustomRepository {

    private val jpqlRenderer = JpqlRenderer()

    override fun findAllByCursor(
        memberId: Long,
        status: String,
        cursorId: Long?,
        cursorDateAt: Instant?,
        size: Int
    ): List<ChatRoomResult> {
        val query = jpql {
            val myLastReadMessageId = caseWhen(path(ChatRoom::member1Id).eq(memberId))
                .then(path(ChatRoom::member1LastReadMessageId))
                .`else`(path(ChatRoom::member2LastReadMessageId))

            val otherMemberId = caseWhen(path(ChatRoom::member1Id).eq(memberId))
                .then(path(ChatRoom::member2Id))
                .`else`(path(ChatRoom::member1Id))

            val unreadCount = select(count(path(ChatMessage::id)))
                .from(entity(ChatMessage::class))
                .whereAnd(
                    path(ChatMessage::chatRoomId).eq(path(ChatRoom::id)),
                    path(ChatMessage::senderId).ne(memberId),
                    path(ChatMessage::id).gt(myLastReadMessageId),
                )
                .asSubquery()

            selectNew<ChatRoomResult>(
                path(ChatRoom::id),
                path(Member::id),
                path(Member::nickname),
                path(Member::profileUrl),
                unreadCount,
                path(ChatRoom::lastMessagePreview),
                path(ChatRoom::lastMessageAt),
            ).from(
                entity(ChatRoom::class),
                join(Member::class).on(path(Member::id).eq(otherMemberId)),
            ).whereAnd(
                or(
                    path(ChatRoom::member1Id).eq(memberId),
                    path(ChatRoom::member2Id).eq(memberId),
                ),
                if (status == "UNREAD") {
                    unreadCount.gt(0L)
                } else null,

                if (cursorId != null && cursorDateAt != null) {
                    or(
                        path(ChatRoom::lastMessageAt).lt(cursorDateAt),
                        and(
                            path(ChatRoom::lastMessageAt).eq(cursorDateAt),
                            path(ChatRoom::id).lt(cursorId),
                        ),
                    )
                } else null,
            ).orderBy(
                path(ChatRoom::lastMessageAt).desc(),
                path(ChatRoom::id).desc(),
            )
        }

        val rendered = jpqlRenderer.render(query, jpqlRenderContext)
        val jpaQuery = entityManager.createQuery(rendered.query, ChatRoomResult::class.java).apply {
            rendered.params.forEach { (name, value) ->
                setParameter(name, value)
            }
        }
        return jpaQuery.setMaxResults(size).resultList
    }

    override fun findAllByNickname(
        memberId: Long,
        nickname: String,
        cursorId: Long?,
        cursorDateAt: Instant?,
        size: Int
    ): List<ChatRoomSearchResult> {
        val query = jpql {
            caseWhen(path(ChatRoom::member1Id).eq(memberId))
                .then(path(ChatRoom::member1LastReadMessageId))
                .`else`(path(ChatRoom::member2LastReadMessageId))

            val otherMemberId = caseWhen(path(ChatRoom::member1Id).eq(memberId))
                .then(path(ChatRoom::member2Id))
                .`else`(path(ChatRoom::member1Id))

            val escaped = EscapeCharacter.DEFAULT.escape(nickname)

            selectNew<ChatRoomSearchResult>(
                path(ChatRoom::id),
                path(Member::id),
                path(Member::nickname),
                path(Member::profileUrl),
                path(ChatRoom::lastMessagePreview),
                path(ChatRoom::lastMessageAt),
            ).from(
                entity(ChatRoom::class),
                join(Member::class).on(path(Member::id).eq(otherMemberId)),
            ).whereAnd(
                or(
                    path(ChatRoom::member1Id).eq(memberId),
                    path(ChatRoom::member2Id).eq(memberId),
                ),
                path(Member::nickname).like("$escaped%", escape = '\\'),

                if (cursorId != null && cursorDateAt != null) {
                    or(
                        path(ChatRoom::lastMessageAt).lt(cursorDateAt),
                        and(
                            path(ChatRoom::lastMessageAt).eq(cursorDateAt),
                            path(ChatRoom::id).lt(cursorId),
                        ),
                    )
                } else null,
            ).orderBy(
                path(ChatRoom::lastMessageAt).desc(),
                path(ChatRoom::id).desc(),
            )
        }

        val rendered = jpqlRenderer.render(query, jpqlRenderContext)
        val jpaQuery = entityManager.createQuery(rendered.query, ChatRoomSearchResult::class.java).apply {
            rendered.params.forEach { (name, value) ->
                setParameter(name, value)
            }
        }
        return jpaQuery.setMaxResults(size).resultList
    }
}