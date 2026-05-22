package com.blueoauld.server.member.repository.impl

import com.blueoauld.server.activity.entity.Block
import com.blueoauld.server.activity.entity.Like
import com.blueoauld.server.activity.entity.Review
import com.blueoauld.server.activity.entity.Unlike
import com.blueoauld.server.member.entity.Member
import com.blueoauld.server.member.entity.type.Gender
import com.blueoauld.server.member.repository.MemberCustomRepository
import com.blueoauld.server.member.repository.result.MemberResult
import com.linecorp.kotlinjdsl.dsl.jpql.jpql
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderContext
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderer
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class MemberCustomRepositoryImpl(

    private val entityManager: EntityManager,
    private val jpqlRenderContext: JpqlRenderContext,
) : MemberCustomRepository {

    private val jpqlRenderer = JpqlRenderer()

    override fun findAllByCursor(
        memberId: Long,
        gender: String,
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

    private fun genderFilter(gender: String): Gender? = when (gender.uppercase()) {
        "MALE" -> Gender.MALE
        "FEMALE" -> Gender.FEMALE
        else -> null
    }
}