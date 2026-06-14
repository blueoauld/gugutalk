package com.blueoauld.server.activity.repository.impl

import com.blueoauld.server.activity.entity.Unlike
import com.blueoauld.server.activity.repository.UnlikeCustomRepository
import com.blueoauld.server.activity.repository.result.ActivityResult
import com.blueoauld.server.activity.repository.result.RankResult
import com.blueoauld.server.member.entity.Member
import com.blueoauld.server.member.entity.type.Gender
import com.blueoauld.server.member.entity.type.Region
import com.linecorp.kotlinjdsl.dsl.jpql.jpql
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderContext
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderer
import jakarta.persistence.EntityManager
import jakarta.persistence.Tuple
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
        val genderValue = genderFilter(gender)
        val hasCursor = cursorId != null && cursorScore != null

        val sql = buildString {
            append(
                """
                    SELECT
                        m.id,
                        m.nickname,
                        m.profile_url,
                        m.gender,
                        m.birth_year,
                        m.region,
                        m.comment,
                        m.updated_at,
                        COALESCE(l.cnt, 0) AS like_count,
                        COALESCE(u.cnt, 0) AS unlike_count,
                        COALESCE(r.cnt, 0) AS review_count
                    FROM member m
                    LEFT JOIN (SELECT to_id, count(*) AS cnt FROM likes  GROUP BY to_id) l ON l.to_id = m.id
                    LEFT JOIN (SELECT to_id, count(*) AS cnt FROM unlike GROUP BY to_id) u ON u.to_id = m.id
                    LEFT JOIN (SELECT to_id, count(*) AS cnt FROM review
                               WHERE deleted_at IS NULL GROUP BY to_id) r ON r.to_id = m.id
                    WHERE m.deleted_at IS NULL
                """.trimIndent()
            )
            if (genderValue != null) {
                append("\n  AND m.gender = :gender")
            }
            if (hasCursor) {
                append(
                    """
                        
                      AND (COALESCE(l.cnt, 0) < :cursorScore
                           OR (COALESCE(l.cnt, 0) = :cursorScore AND m.id < :cursorId))
                    """.trimIndent()
                )
            }
            append("\nORDER BY unlike_count DESC, m.id DESC")
        }

        val query = entityManager.createNativeQuery(sql, Tuple::class.java).apply {
            if (genderValue != null) {
                setParameter("gender", genderValue.name)
            }
            if (hasCursor) {
                setParameter("cursorScore", cursorScore)
                setParameter("cursorId", cursorId)
            }
            maxResults = size
        }

        @Suppress("UNCHECKED_CAST")
        val rows = query.resultList as List<Tuple>
        return rows.map { t ->
            RankResult(
                (t.get("id") as Number).toLong(),
                t.get("nickname") as String,
                t.get("profile_url") as String?,
                Gender.valueOf(t.get("gender") as String),
                (t.get("birth_year") as Number).toInt(),
                Region.valueOf(t.get("region") as String),
                t.get("comment") as String,
                t.get("updated_at", Instant::class.java),
                (t.get("like_count") as Number).toLong(),
                (t.get("unlike_count") as Number).toLong(),
                (t.get("review_count") as Number).toLong(),
            )
        }
    }

    private fun genderFilter(gender: String): Gender? = Gender.entries.find {
        it.name.equals(gender, ignoreCase = true)
    }
}