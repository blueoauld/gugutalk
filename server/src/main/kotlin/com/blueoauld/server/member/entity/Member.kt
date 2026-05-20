package com.blueoauld.server.member.entity

import com.blueoauld.server.member.entity.type.Gender
import com.blueoauld.server.member.entity.type.Region
import jakarta.persistence.*
import org.hibernate.annotations.SoftDelete
import org.hibernate.annotations.SoftDeleteType
import java.time.Instant
import java.util.*

@SoftDelete(columnName = "deleted_at", strategy = SoftDeleteType.TIMESTAMP)
@Entity
class Member(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(name = "phone", unique = true, nullable = false)
    val phone: String,

    @Column(name = "password", nullable = false)
    val password: String,

    @Column(name = "device_id", nullable = false)
    val deviceId: String,

    @Column(name = "profile_url")
    val profileUrl: String? = null,

    @Column(name = "nickname", unique = true, nullable = false)
    val nickname: String = "닉네임_${UUID.randomUUID().toString().replace("-", "").take(6)}",

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    val gender: Gender,

    @Enumerated(EnumType.STRING)
    @Column(name = "region", nullable = false)
    val region: Region = Region.OVERSEAS,

    @Column(name = "birth_year", nullable = false)
    val birthYear: Int = 2000,

    @Column(name = "comment", nullable = false)
    val comment: String = "안녕하세요.",

    @Column(name = "bio", nullable = false)
    val bio: String = "",

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant = Instant.now(),
)