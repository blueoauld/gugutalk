package com.blueoauld.server.common.initialization

import com.blueoauld.server.activity.entity.Review
import com.blueoauld.server.activity.repository.ReviewRepository
import com.blueoauld.server.common.util.RandomNicknameGenerator
import com.blueoauld.server.member.entity.Member
import com.blueoauld.server.member.entity.type.Gender
import com.blueoauld.server.member.repository.MemberRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.util.*

@Component
class DummyData(

    private val passwordEncoder: PasswordEncoder
) {

    private val log = KotlinLogging.logger {}

    @Bean
    fun setup(
        memberRepository: MemberRepository,
        reviewRepository: ReviewRepository,
    ): CommandLineRunner {
        return CommandLineRunner {
            if (memberRepository.count() == 0L) {
                val members = (0 until 100).map {
                    Member(
                        phone = "0100000%04d".format(it.toLong()),
                        password = passwordEncoder.encode("1234")!!,
                        deviceId = UUID.randomUUID().toString(),
                        gender = if (it % 2 == 0) Gender.MALE else Gender.FEMALE,
                        comment = "안녕하세요. $it",
                        bio = "반갑습니다. $it"
                    )
                }

                memberRepository.saveAll(members)
            }
            if (reviewRepository.count() == 0L) {
                val reviews = (1 until 100).map {
                    Review(
                        fromId = it.toLong(),
                        toId = 1,
                        nickname = RandomNicknameGenerator.generate(),
                        content = "리뷰입니다. $it"
                    )
                }

                reviewRepository.saveAll(reviews)
            }

            log.info { "회원 더미 데이터 ${memberRepository.count()}개 생성" }
            log.info { "리뷰 더미 데이터 ${memberRepository.count()}개 생성" }
        }
    }
}