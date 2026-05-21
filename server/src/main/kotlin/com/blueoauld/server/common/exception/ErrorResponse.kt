package com.blueoauld.server.common.exception

import com.blueoauld.server.common.exception.type.ErrorCode

class ErrorResponse(

    val code: String,
    val message: String,
) {

    companion object {
        fun of(errorCode: ErrorCode) = ErrorResponse(
            code = errorCode.name,
            message = errorCode.message,
        )

        fun of(errorCode: ErrorCode, message: String) = ErrorResponse(
            code = errorCode.name,
            message = message,
        )
    }
}