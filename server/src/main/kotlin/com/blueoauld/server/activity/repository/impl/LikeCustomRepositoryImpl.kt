package com.blueoauld.server.activity.repository.impl

import com.blueoauld.server.activity.entity.Like
import com.blueoauld.server.activity.repository.LikeCustomRepository
import com.blueoauld.server.activity.repository.result.ActivityResult
import com.blueoauld.server.member.entity.Member
import com.linecorp.kotlinjdsl.dsl.jpql.jpql
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderContext
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderer
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class LikeCustomRepositoryImpl(

    private val entityManager: EntityManager,
    private val jpqlRenderContext: JpqlRenderContext,
) : LikeCustomRepository {

    private val jpqlRenderer = JpqlRenderer()

    override fun findAllByCursor(
        memberId: Long,
        cursorId: Long?,
        cursorDateAt: Instant?,
        size: Int
    ): List<ActivityResult> {
        val query = jpql {
            selectNew<ActivityResult>(
                path(Like::id),
                path(Like::toId),
                path(Member::profileUrl),
                path(Member::nickname),
                path(Member::gender),
                path(Member::birthYear),
                path(Member::region),
                path(Like::createdAt),
            ).from(
                entity(Like::class),
                join(Member::class).on(path(Like::toId).eq(path(Member::id))),
            ).whereAnd(
                path(Like::fromId).eq(memberId),
                if (cursorId != null && cursorDateAt != null) {
                    or(
                        path(Like::createdAt).lt(cursorDateAt),
                        and(
                            path(Like::createdAt).eq(cursorDateAt),
                            path(Like::id).lt(cursorId),
                        ),
                    )
                } else null,
            ).orderBy(
                path(Like::createdAt).desc(),
                path(Like::id).desc(),
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
}