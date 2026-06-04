package com.blueoauld.server.admin.application.type

enum class ResetTarget(val label: String) {

    NICKNAME("닉네임"),
    COMMENT("코멘트"),
    BIO("자기소개"),
    PUBLIC_IMAGES("공개사진"),
    PRIVATE_IMAGES("비밀사진"),
}