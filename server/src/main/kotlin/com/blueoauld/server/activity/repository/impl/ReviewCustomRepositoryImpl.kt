package com.blueoauld.server.activity.repository.impl

import com.blueoauld.server.activity.entity.Review
import com.blueoauld.server.activity.repository.ReviewCustomRepository
import com.blueoauld.server.activity.repository.result.ReviewResult
import com.linecorp.kotlinjdsl.dsl.jpql.jpql
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderContext
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderer
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class ReviewCustomRepositoryImpl(

    private val entityManager: EntityManager,
    private val jpqlRenderContext: JpqlRenderContext,
) : ReviewCustomRepository {

    private val jpqlRenderer = JpqlRenderer()

    override fun findAllByCursor(
        memberId: Long,
        cursorId: Long?,
        cursorDateAt: Instant?,
        size: Int
    ): List<ReviewResult> {
        val query = jpql {
            selectNew<ReviewResult>(
                path(Review::id),
                path(Review::fromId),
                path(Review::toId),
                path(Review::nickname),
                path(Review::content),
                path(Review::createdAt),
            ).from(
                entity(Review::class),
            ).whereAnd(
                path(Review::toId).eq(memberId),
                if (cursorId != null && cursorDateAt != null) {
                    or(
                        path(Review::createdAt).lt(cursorDateAt),
                        and(
                            path(Review::createdAt).eq(cursorDateAt),
                            path(Review::id).lt(cursorId),
                        ),
                    )
                } else null,
            ).orderBy(
                path(Review::createdAt).desc(),
                path(Review::id).desc(),
            )
        }

        val rendered = jpqlRenderer.render(query, jpqlRenderContext)
        val jpaQuery = entityManager.createQuery(rendered.query, ReviewResult::class.java).apply {
            rendered.params.forEach { (name, value) ->
                setParameter(name, value)
            }
        }
        return jpaQuery.setMaxResults(size).resultList
    }
}