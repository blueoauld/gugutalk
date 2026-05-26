package com.blueoauld.server.r2.application

import com.blueoauld.server.common.properties.R2Properties
import com.blueoauld.server.r2.application.response.UploadUrlResponse
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.time.Duration

@Service
class R2Provider(

    private val s3Presigner: S3Presigner,
    private val r2Properties: R2Properties
) {

    fun createUploadUrl(key: String, contentType: String, expiry: Duration): UploadUrlResponse {
        val putObjectRequest = PutObjectRequest.builder()
            .bucket(r2Properties.bucket)
            .key(key)
            .contentType(contentType)
            .build()

        val url = s3Presigner.presignPutObject(
            PutObjectPresignRequest.builder()
                .signatureDuration(expiry)
                .putObjectRequest(putObjectRequest)
                .build()
        ).url().toString()

        return UploadUrlResponse(url, key)
    }
}