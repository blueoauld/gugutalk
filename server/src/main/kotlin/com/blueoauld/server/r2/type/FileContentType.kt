package com.blueoauld.server.r2.type

import com.blueoauld.server.common.exception.CustomException
import com.blueoauld.server.common.exception.type.ErrorCode.FILE_01

enum class FileContentType(

    val extension: String,
    val contentType: String,
) {

    JPEG("jpg", "image/jpeg"),
    PNG("png", "image/png"),
    GIF("gif", "image/gif"),
    WEBP("webp", "image/webp"),
    MP4("mp4", "video/mp4"),
    MOV("mov", "video/quicktime"),
    ;

    companion object {
        private val BY_CONTENT_TYPE = entries.associateBy { it.contentType }

        fun from(contentType: String): FileContentType {
            return BY_CONTENT_TYPE[contentType.lowercase()] ?: throw CustomException(FILE_01)
        }
    }
}
