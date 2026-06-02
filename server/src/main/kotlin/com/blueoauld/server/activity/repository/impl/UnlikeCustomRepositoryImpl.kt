package com.blueoauld.server.activity.repository.impl

import com.blueoauld.server.activity.entity.Like
import com.blueoauld.server.activity.entity.Review
import com.blueoauld.server.activity.entity.Unlike
import com.blueoauld.server.activity.repository.UnlikeCustomRepository
import com.blueoauld.server.activity.repository.result.ActivityResult
import com.blueoauld.server.activity.repository.result.RankResult
import com.blueoauld.server.member.entity.Member
import com.blueoauld.server.member.entity.type.Gender
import com.linecorp.kotlinjdsl.dsl.jpql.jpql
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderContext
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderer
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class UnlikeCustomRepositoryImpl(

    private val entityManager: EntityManager,
    private val jpqlRenderContext: JpqlRenderContext,
) : UnlikeCustomRepository {

    private val jpqlRenderer = JpqlRenderer()

    override fun findAllByCursor(
        memberId: Long,
        cursorId: Long?,
        cursorDateAt: Instant?,
        size: Int
    ): List<ActivityResult> {
        val query = jpql {
            selectNew<ActivityResult>(
                path(Unlike::id),
                path(Unlike::toId),
                path(Member::profileUrl),
                path(Member::nickname),
                path(Member::gender),
                path(Member::birthYear),
                path(Member::region),
                path(Unlike::createdAt),
            ).from(
                entity(Unlike::class),
                join(Member::class).on(path(Unlike::toId).eq(path(Member::id))),
            ).whereAnd(
                path(Unlike::fromId).eq(memberId),
                if (cursorId != null && cursorDateAt != null) {
                    or(
                        path(Unlike::createdAt).lt(cursorDateAt),
                        and(
                            path(Unlike::createdAt).eq(cursorDateAt),
                            path(Unlike::id).lt(cursorId),
                        ),
                    )
                } else null,
            ).orderBy(
                path(Unlike::createdAt).desc(),
                path(Unlike::id).desc(),
            )
        }

        val rendered = jpqlRenderer.render(query, jpqlRenderContext)
        val jpaQuery = entityManager.createQuery(rendered.query, ActivityResult::class.java).apply {
            rendered.params.forEach { (name, value) ->
                setParameter(name, value)
            }
        }
        return jpaQuery.setMaxResults(size).resultList
    }

    override fun findAllByRank(
        gender: String,
        cursorId: Long?,
        cursorScore: Long?,
        size: Int
    ): List<RankResult> {
        val query = jpql {
            selectNew<RankResult>(
                path(Member::id),
                path(Member::nickname),
                path(Member::profileUrl),
                path(Member::gender),
                path(Member::birthYear),
                path(Member::region),
                path(Member::comment),
                path(Member::updatedAt),
                countDistinct(path(Like::id)),
                countDistinct(path(Unlike::id)),
                countDistinct(path(Review::id)),
            ).from(
                entity(Member::class),
                leftJoin(Like::class).on(path(Like::toId).eq(path(Member::id))),
                leftJoin(Unlike::class).on(path(Unlike::toId).eq(path(Member::id))),
                leftJoin(Review::class).on(path(Review::toId).eq(path(Member::id))),
            ).whereAnd(
                genderFilter(gender)?.let {
                    path(Member::gender).eq(it)
                },
            ).groupBy(
                path(Member::id),
                path(Member::nickname),
                path(Member::profileUrl),
                path(Member::gender),
                path(Member::birthYear),
                path(Member::region),
                path(Member::comment),
                path(Member::updatedAt),
            ).havingAnd(
                if (cursorId != null && cursorScore != null) {
                    or(
                        countDistinct(path(Unlike::id)).lt(cursorScore),
                        and(
                            countDistinct(path(Unlike::id)).eq(cursorScore),
                            path(Member::id).lt(cursorId),
                        ),
                    )
                } else null,
            ).orderBy(
                countDistinct(path(Unlike::id)).desc(),
                path(Member::id).desc(),
            )
        }

        val rendered = jpqlRenderer.render(query, jpqlRenderContext)
        val jpaQuery = entityManager.createQuery(rendered.query, RankResult::class.java).apply {
            rendered.params.forEach { (name, value) ->
                setParameter(name, value)
            }
        }
        return jpaQuery.setMaxResults(size).resultList
    }

    private fun genderFilter(gender: String): Gender? = Gender.entries.find {
        it.name.equals(gender, ignoreCase = true)
    }
}