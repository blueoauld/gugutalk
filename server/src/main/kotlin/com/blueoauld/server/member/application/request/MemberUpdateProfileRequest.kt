package com.blueoauld.server.member.application.request

import com.blueoauld.server.common.validator.Age
import com.blueoauld.server.common.validator.TrimDeserializer
import com.blueoauld.server.member.entity.type.Region
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class MemberUpdateProfileRequest(

    @field:Size(
        max = 5,
        message = "프로필 사진은 최대 5장까지 요청할 수 있습니다."
    )
    @field:Valid
    val publicImages: List<MemberImageCreateRequest>,

    @field:Size(
        max = 5,
        message = "비밀 사진은 최대 5장까지 요청할 수 있습니다."
    )
    @field:Valid
    val privateImages: List<MemberImageCreateRequest>,

    @field:JsonDeserialize(using = TrimDeserializer::class)
    @field:NotBlank(message = "닉네임은 필수입니다.")
    @field:Size(min = 2, max = 15, message = "닉네임은 2자 이상 15자 이하여야 합니다.")
    val nickname: String,

    @field:Age(min = 20, max = 50, message = "만 20세 이상 50세 이하만 가입 가능합니다.")
    val birthYear: Int,

    @field:NotNull(message = "지역은 필수입니다.")
    var region: Region,

    @field:Size(max = 500, message = "자기소개는 500자 이하여야 합니다.")
    val bio: String,
)
