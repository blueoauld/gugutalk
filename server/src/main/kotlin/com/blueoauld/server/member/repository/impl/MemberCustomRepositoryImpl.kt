package com.blueoauld.server.member.repository.impl

import com.blueoauld.server.activity.entity.*
import com.blueoauld.server.member.entity.Member
import com.blueoauld.server.member.entity.type.Gender
import com.blueoauld.server.member.entity.type.Region
import com.blueoauld.server.member.repository.MemberCustomRepository
import com.blueoauld.server.member.repository.result.MemberDetailResult
import com.blueoauld.server.member.repository.result.MemberResult
import com.blueoauld.server.member.repository.result.MemberSearchResult
import com.linecorp.kotlinjdsl.dsl.jpql.jpql
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderContext
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderer
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.data.jpa.repository.query.EscapeCharacter
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class MemberCustomRepositoryImpl(

    private val jpqlRenderContext: JpqlRenderContext,
) : MemberCustomRepository {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    private val jpqlRenderer = JpqlRenderer()

    override fun findAllByCursor(
        memberId: Long,
        gender: String,
        region: Region?,
        cursorId: Long?,
        cursorDateAt: Instant?,
        size: Int
    ): List<MemberResult> {
        val query = jpql {
            selectNew<MemberResult>(
                path(Member::id),
                path(Member::nickname),
                path(Member::profileUrl),
                path(Member::gender),
                path(Member::birthYear),
                path(Member::region),
                path(Member::comment),
                path(Member::updatedAt),
                select<Long>(count(Like::id))
                    .from(entity(Like::class))
                    .where(path(Like::toId).eq(path(Member::id)))
                    .asSubquery(),
                select<Long>(count(Unlike::id))
                    .from(entity(Unlike::class))
                    .where(path(Unlike::toId).eq(path(Member::id)))
                    .asSubquery(),
                select<Long>(count(Review::id))
                    .from(entity(Review::class))
                    .where(path(Review::toId).eq(path(Member::id)))
                    .asSubquery(),
            ).from(
                entity(Member::class),
            ).whereAnd(
                path(Member::id).ne(memberId),
                genderFilter(gender)?.let {
                    path(Member::gender).eq(it)
                },
                region?.let { path(Member::region).eq(it) },
                path(Member::id).notIn(
                    select<Long>(path(Block::toId))
                        .from(entity(Block::class))
                        .where(path(Block::fromId).eq(memberId))
                        .asSubquery(),
                ),
                path(Member::id).notIn(
                    select<Long>(path(Block::fromId))
                        .from(entity(Block::class))
                        .where(path(Block::toId).eq(memberId))
                        .asSubquery(),
                ),
                if (cursorId != null && cursorDateAt != null) {
                    or(
                        path(Member::updatedAt).lt(cursorDateAt),
                        and(
                            path(Member::updatedAt).eq(cursorDateAt),
                            path(Member::id).lt(cursorId),
                        ),
                    )
                } else null,
            ).orderBy(
                path(Member::updatedAt).desc(),
                path(Member::id).desc(),
            )
        }

        val rendered = jpqlRenderer.render(query, jpqlRenderContext)
        val jpaQuery = entityManager.createQuery(rendered.query, MemberResult::class.java).apply {
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
    ): List<MemberSearchResult> {
        val query = jpql {
            val escaped = EscapeCharacter.DEFAULT.escape(nickname)

            selectNew<MemberSearchResult>(
                path(Member::id),
                path(Member::nickname),
                path(Member::profileUrl),
                path(Member::gender),
                path(Member::birthYear),
                path(Member::region),
                path(Member::updatedAt),
            ).from(
                entity(Member::class),
            ).whereAnd(
                path(Member::id).ne(memberId),
                path(Member::nickname).like("$escaped%", escape = '\\'),
                path(Member::id).notIn(
                    select<Long>(path(Block::toId))
                        .from(entity(Block::class))
                        .where(path(Block::fromId).eq(memberId))
                        .asSubquery(),
                ),
                path(Member::id).notIn(
                    select<Long>(path(Block::fromId))
                        .from(entity(Block::class))
                        .where(path(Block::toId).eq(memberId))
                        .asSubquery(),
                ),
                if (cursorId != null && cursorDateAt != null) {
                    or(
                        path(Member::updatedAt).lt(cursorDateAt),
                        and(
                            path(Member::updatedAt).eq(cursorDateAt),
                            path(Member::id).lt(cursorId),
                        ),
                    )
                } else null,
            ).orderBy(
                path(Member::updatedAt).desc(),
                path(Member::id).desc(),
            )
        }

        val rendered = jpqlRenderer.render(query, jpqlRenderContext)
        val jpaQuery = entityManager.createQuery(rendered.query, MemberSearchResult::class.java).apply {
            rendered.params.forEach { (name, value) ->
                setParameter(name, value)
            }
        }
        return jpaQuery.setMaxResults(size).resultList
    }

    override fun findDetailById(memberId: Long, targetId: Long): MemberDetailResult? {
        val query = jpql {
            selectNew<MemberDetailResult>(
                path(Member::id),
                path(Member::nickname),
                path(Member::profileUrl),
                path(Member::gender),
                path(Member::birthYear),
                path(Member::region),
                path(Member::bio),
                path(Member::isChat),
                path(Member::updatedAt),
                select<Long>(count(Like::id))
                    .from(entity(Like::class))
                    .where(path(Like::toId).eq(targetId))
                    .asSubquery(),
                select<Long>(count(Unlike::id))
                    .from(entity(Unlike::class))
                    .where(path(Unlike::toId).eq(targetId))
                    .asSubquery(),
                select<Long>(count(Review::id))
                    .from(entity(Review::class))
                    .where(path(Review::toId).eq(targetId))
                    .asSubquery(),
                select<Long>(count(Like::id))
                    .from(entity(Like::class))
                    .where(and(path(Like::fromId).eq(memberId), path(Like::toId).eq(targetId)))
                    .asSubquery(),
                select<Long>(count(Unlike::id))
                    .from(entity(Unlike::class))
                    .where(and(path(Unlike::fromId).eq(memberId), path(Unlike::toId).eq(targetId)))
                    .asSubquery(),
                select<Long>(count(Block::id))
                    .from(entity(Block::class))
                    .where(and(path(Block::fromId).eq(memberId), path(Block::toId).eq(targetId)))
                    .asSubquery(),
                select<Long>(count(PrivateImageGrant::id))
                    .from(entity(PrivateImageGrant::class))
                    .where(
                        and(
                            path(PrivateImageGrant::fromId).eq(memberId),
                            path(PrivateImageGrant::toId).eq(targetId)
                        )
                    )
                    .asSubquery(),
                select<Long>(count(PrivateImageGrant::id))
                    .from(entity(PrivateImageGrant::class))
                    .where(
                        and(
                            path(PrivateImageGrant::fromId).eq(targetId),
                            path(PrivateImageGrant::toId).eq(memberId)
                        )
                    )
                    .asSubquery(),
            ).from(
                entity(Member::class),
            ).where(
                path(Member::id).eq(targetId),
            )
        }

        val rendered = jpqlRenderer.render(query, jpqlRenderContext)
        val jpaQuery = entityManager.createQuery(rendered.query, MemberDetailResult::class.java).apply {
            rendered.params.forEach { (name, value) ->
                setParameter(name, value)
            }
        }
        return jpaQuery.resultList.firstOrNull()
    }

    private fun genderFilter(gender: String): Gender? = Gender.entries.find {
        it.name.equals(gender, ignoreCase = true)
    }
}