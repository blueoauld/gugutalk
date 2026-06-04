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
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthenticationService(

    private val verificationCodeRepository: VerificationCodeRepository,
    private val memberRepository: MemberRepository,
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
        val point = Point(memberId = member.id)

        return try {
            memberRepository.save(member)
            pointRepository.save(point)

            member
        } catch (_: DataIntegrityViolationException) {
            throw CustomException(MEMBER_02)
        }
    }

    @Transactional
    fun updateProfile(memberId: Long, request: SetupRequest) {
        val member = memberRepository.findByIdOrNull(memberId) ?: throw CustomException(MEMBER_01)

        if (member.nickname != request.nickname && memberRepository.existsByNickname(request.nickname)) {
            throw CustomException(MEMBER_03)
        }

        member.updateProfile(
            nickname = request.nickname,
            birthYear = request.birthYear,
            region = request.region,
            bio = request.bio,
        )

        try {
            memberRepository.saveAndFlush(member)
        } catch (_: DataIntegrityViolationException) {
            throw CustomException(MEMBER_03)
        }
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