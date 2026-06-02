package com.blueoauld.server.activity.repository.impl

import com.blueoauld.server.activity.entity.Block
import com.blueoauld.server.activity.repository.BlockCustomRepository
import com.blueoauld.server.activity.repository.result.ActivityResult
import com.blueoauld.server.member.entity.Member
import com.linecorp.kotlinjdsl.dsl.jpql.jpql
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderContext
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderer
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class BlockCustomRepositoryImpl(

    private val entityManager: EntityManager,
    private val jpqlRenderContext: JpqlRenderContext,
) : BlockCustomRepository {

    private val jpqlRenderer = JpqlRenderer()

    override fun findAllByCursor(
        memberId: Long,
        cursorId: Long?,
        cursorDateAt: Instant?,
        size: Int
    ): List<ActivityResult> {
        val query = jpql {
            selectNew<ActivityResult>(
                path(Block::id),
                path(Block::toId),
                path(Member::profileUrl),
                path(Member::nickname),
                path(Member::gender),
                path(Member::birthYear),
                path(Member::region),
                path(Block::createdAt),
            ).from(
                entity(Block::class),
                join(Member::class).on(path(Block::toId).eq(path(Member::id))),
            ).whereAnd(
                path(Block::fromId).eq(memberId),
                if (cursorId != null && cursorDateAt != null) {
                    or(
                        path(Block::createdAt).lt(cursorDateAt),
                        and(
                            path(Block::createdAt).eq(cursorDateAt),
                            path(Block::id).lt(cursorId),
                        ),
                    )
                } else null,
            ).orderBy(
                path(Block::createdAt).desc(),
                path(Block::id).desc(),
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