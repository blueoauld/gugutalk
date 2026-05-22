package com.blueoauld.server.common.initialization

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
    fun setup(memberRepository: MemberRepository): CommandLineRunner {
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

            log.info { "회원 더미 데이터 ${memberRepository.count()}개 생성" }
        }
    }
}