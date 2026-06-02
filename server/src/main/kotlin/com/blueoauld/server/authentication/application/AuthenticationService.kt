package com.blueoauld.server.authentication.application

import com.blueoauld.server.authentication.application.request.LoginRequest
import com.blueoauld.server.authentication.application.request.SendVerificationCodeRequest
import com.blueoauld.server.authentication.application.request.SetupRequest
import com.blueoauld.server.authentication.application.request.SignupRequest
import com.blueoauld.server.authentication.entity.VerificationCode
import com.blueoauld.server.authentication.repository.VerificationCodeRepository
import com.blueoauld.server.common.exception.CustomException
import com.blueoauld.server.common.exception.type.ErrorCode.*
import com.blueoauld.server.member.entity.Member
import com.blueoauld.server.member.repository.MemberRepository
import com.blueoauld.server.point.entity.Point
import com.blueoauld.server.point.repository.PointRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

const val AUTHENTICATION_ACCESS_TOKEN_BLACKLIST_KEY = "authentication:access_token:blacklist:"

@Service
class AuthenticationService(

    private val memberRepository: MemberRepository,
    private val verificationCodeRepository: VerificationCodeRepository,
    private val pointRepository: PointRepository,
    private val passwordEncoder: PasswordEncoder,
) {

    @Transactional
    fun saveVerificationCode(request: SendVerificationCodeRequest, ip: String, code: String) {
        verificationCodeRepository.save(
            VerificationCode(
                phone = request.phone,
                deviceId = request.deviceId,
                ip = ip,
                code = code,
            )
        )
    }

    @Transactional
    fun createMember(request: SignupRequest): Member {
        if (memberRepository.existsByPhone(request.phone)) {
            throw CustomException(MEMBER_02)
        }

        val member = Member(
            phone = request.phone,
            password = passwordEncoder.encode(request.password)!!,
            deviceId = request.deviceId,
            gender = request.gender,
        )
        memberRepository.save(member)

        val point = Point(memberId = member.id)
        pointRepository.save(point)

        return member
    }

    @Transactional
    fun updateProfile(memberId: Long, request: SetupRequest) {
        val member = memberRepository.findByIdOrNull(memberId) ?: throw CustomException(MEMBER_01)

        if (memberRepository.existsByNickname(request.nickname)) {
            throw CustomException(MEMBER_03)
        }

        member.updateProfile(
            nickname = request.nickname,
            birthYear = request.birthYear,
            region = request.region,
            bio = request.bio,
        )
    }

    @Transactional
    fun authenticate(request: LoginRequest): Member {
        val member = memberRepository.findByPhone(request.phone) ?: throw CustomException(MEMBER_05)

        if (!passwordEncoder.matches(request.password, member.password)) {
            throw CustomException(MEMBER_05)
        }

        member.updateDeviceId(request.deviceId)
        return member
    }
}