package com.blueoauld.server.member.entity

import com.blueoauld.server.member.entity.type.MemberImageType
import jakarta.persistence.*
import java.time.Instant

@Entity
class MemberImage(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(name = "memberId", nullable = false)
    val memberId: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    var type: MemberImageType,

    @Column(name = "key", unique = true, nullable = false)
    val key: String,

    @Column(name = "url", unique = true, nullable = false)
    val url: String,

    @Column(name = "sortOrder", nullable = false)
    var sortOrder: Int,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
) {

    fun updateSortOrder(sortOrder: Int) {
        this.sortOrder = sortOrder
    }
}