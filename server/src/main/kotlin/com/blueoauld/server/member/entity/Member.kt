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
    var deviceId: String,

    @Column(name = "profile_url")
    val profileUrl: String? = null,

    @Column(name = "nickname", length = 10, unique = true, nullable = false)
    var nickname: String = "닉네임_${UUID.randomUUID().toString().replace("-", "").take(6)}",

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    val gender: Gender,

    @Enumerated(EnumType.STRING)
    @Column(name = "region", nullable = false)
    var region: Region = Region.OVERSEAS,

    @Column(name = "birth_year", nullable = false)
    var birthYear: Int = 2000,

    @Column(name = "comment", length = 50, nullable = false)
    var comment: String = "안녕하세요.",

    @Column(name = "bio", length = 500, nullable = false)
    var bio: String = "",

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),
) {

    fun setup(nickname: String, birthYear: Int, region: Region, bio: String) {
        this.nickname = nickname
        this.birthYear = birthYear
        this.region = region
        this.bio = bio
    }

    fun updateDeviceId(deviceId: String) {
        this.deviceId = deviceId
    }

    fun updateComment(comment: String) {
        this.comment = comment
        this.updatedAt = Instant.now()
    }

    fun bump() {
        this.updatedAt = Instant.now()
    }
}