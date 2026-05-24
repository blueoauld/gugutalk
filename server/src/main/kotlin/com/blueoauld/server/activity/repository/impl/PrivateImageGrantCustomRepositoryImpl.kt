package com.blueoauld.server.activity.repository.impl

import com.blueoauld.server.activity.entity.PrivateImageGrant
import com.blueoauld.server.activity.repository.PrivateImageGrantCustomRepository
import com.blueoauld.server.activity.repository.result.ActivityResult
import com.blueoauld.server.member.entity.Member
import com.linecorp.kotlinjdsl.dsl.jpql.jpql
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderContext
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderer
import jakarta.persistence.EntityManager
import java.time.Instant

class PrivateImageGrantCustomRepositoryImpl(

    private val entityManager: EntityManager,
    private val jpqlRenderContext: JpqlRenderContext,
) : PrivateImageGrantCustomRepository {

    private val jpqlRenderer = JpqlRenderer()

    override fun findAllByCursor(
        memberId: Long,
        cursorId: Long?,
        cursorDateAt: Instant?,
        size: Int
    ): List<ActivityResult> {
        val query = jpql {
            selectNew<ActivityResult>(
                path(PrivateImageGrant::id),
                path(PrivateImageGrant::toId),
                path(Member::profileUrl),
                path(Member::nickname),
                path(Member::gender),
                path(Member::birthYear),
                path(Member::region),
                path(PrivateImageGrant::createdAt),
            ).from(
                entity(PrivateImageGrant::class),
                join(Member::class).on(path(PrivateImageGrant::toId).eq(path(Member::id))),
            ).whereAnd(
                path(PrivateImageGrant::fromId).eq(memberId),
                if (cursorId != null && cursorDateAt != null) {
                    or(
                        path(PrivateImageGrant::createdAt).lt(cursorDateAt),
                        and(
                            path(PrivateImageGrant::createdAt).eq(cursorDateAt),
                            path(PrivateImageGrant::id).lt(cursorId),
                        ),
                    )
                } else null,
            ).orderBy(
                path(PrivateImageGrant::createdAt).desc(),
                path(PrivateImageGrant::id).desc(),
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