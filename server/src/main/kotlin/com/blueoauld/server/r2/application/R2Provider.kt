package com.blueoauld.server.r2.application

import com.blueoauld.server.common.properties.R2Properties
import com.blueoauld.server.r2.application.response.UploadUrlResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.CopyObjectRequest
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.S3Exception
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.time.Duration

@Service
class R2Provider(

    private val s3Client: S3Client,
    private val s3Presigner: S3Presigner,
    private val r2Properties: R2Properties
) {

    private val log = KotlinLogging.logger {}

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

    fun moveFile(sourceKey: String, destinationKey: String) {
        try {
            s3Client.copyObject(
                CopyObjectRequest.builder()
                    .sourceBucket(r2Properties.bucket)
                    .sourceKey(sourceKey)
                    .destinationBucket(r2Properties.bucket)
                    .destinationKey(destinationKey)
                    .build()
            )
        } catch (e: S3Exception) {
            log.error(e) { "파일 복사에 실패했습니다. $sourceKey -> $destinationKey" }
            throw e
        }

        try {
            s3Client.deleteObject(
                DeleteObjectRequest.builder()
                    .bucket(r2Properties.bucket)
                    .key(sourceKey)
                    .build()
            )
        } catch (e: S3Exception) {
            log.error(e) { "파일 복사 후 삭제에 실패했습니다. 키 = $sourceKey" }
            throw e
        }
    }

    fun deleteFile(key: String) {
        try {
            s3Client.deleteObject(
                DeleteObjectRequest.builder()
                    .bucket(r2Properties.bucket)
                    .key(key)
                    .build()
            )
        } catch (e: S3Exception) {
            log.error(e) { "파일 삭제에 실패했습니다. 키 = $key" }
            throw e
        }
    }
}