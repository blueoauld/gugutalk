package com.blueoauld.server.member.application

import com.blueoauld.server.r2.application.R2Provider
import com.blueoauld.server.r2.application.request.UploadUrlRequests
import com.blueoauld.server.r2.application.response.UploadUrlResponses
import com.blueoauld.server.r2.type.FileContentType
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.*

@Service
class MemberImageService(

    private val r2Provider: R2Provider,
) {

    fun createPublicUploadUrls(requests: UploadUrlRequests): UploadUrlResponses {
        val urls = requests.urls.map {
            val contentType = FileContentType.from(it.contentType)
            val fileName = "${UUID.randomUUID()}.${contentType.extension}"
            val key = "member/public/temporary/$fileName"

            r2Provider.createUploadUrl(key, it.contentType, Duration.ofMinutes(5))
        }
        return UploadUrlResponses(urls)
    }

    fun createPrivateUploadUrls(requests: UploadUrlRequests): UploadUrlResponses {
        val urls = requests.urls.map {
            val contentType = FileContentType.from(it.contentType)
            val fileName = "${UUID.randomUUID()}.${contentType.extension}"
            val key = "member/private/temporary/$fileName"

            r2Provider.createUploadUrl(key, it.contentType, Duration.ofMinutes(5))
        }
        return UploadUrlResponses(urls)
    }
}