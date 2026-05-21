package com.blueoauld.server.common.exception

import com.blueoauld.server.common.exception.type.ErrorCode

class CustomException(

    val errorCode: ErrorCode
) : RuntimeException(errorCode.message)