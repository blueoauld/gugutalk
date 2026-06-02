package com.blueoauld.server.point.repository.impl

import com.blueoauld.server.point.entity.Point
import com.blueoauld.server.point.entity.PointHistory
import com.blueoauld.server.point.repository.PointHistoryCustomRepository
import com.blueoauld.server.point.repository.result.PointHistoryResult
import com.linecorp.kotlinjdsl.dsl.jpql.jpql
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderContext
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderer
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class PointHistoryCustomRepositoryImpl(

    private val entityManager: EntityManager,
    private val jpqlRenderContext: JpqlRenderContext,
) : PointHistoryCustomRepository {

    private val jpqlRenderer = JpqlRenderer()

    override fun findAllByCursor(
        memberId: Long,
        cursorId: Long?,
        cursorDateAt: Instant?,
        size: Int
    ): List<PointHistoryResult> {
        val query = jpql {
            selectNew<PointHistoryResult>(
                path(PointHistory::id),
                path(PointHistory::source),
                path(PointHistory::createdAt),
            ).from(
                entity(PointHistory::class),
                join(Point::class).on(path(PointHistory::pointId).eq(path(Point::id))),
            ).whereAnd(
                path(Point::memberId).eq(memberId),
                if (cursorId != null && cursorDateAt != null) {
                    or(
                        path(PointHistory::createdAt).lt(cursorDateAt),
                        and(
                            path(PointHistory::createdAt).eq(cursorDateAt),
                            path(PointHistory::id).lt(cursorId),
                        ),
                    )
                } else null,
            ).orderBy(
                path(PointHistory::createdAt).desc(),
                path(PointHistory::id).desc(),
            )
        }

        val rendered = jpqlRenderer.render(query, jpqlRenderContext)
        val jpaQuery = entityManager.createQuery(rendered.query, PointHistoryResult::class.java).apply {
            rendered.params.forEach { (name, value) ->
                setParameter(name, value)
            }
        }
        return jpaQuery.setMaxResults(size).resultList
    }
}