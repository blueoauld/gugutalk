package com.blueoauld.server.r2.application

import com.blueoauld.server.common.exception.CustomException
import com.blueoauld.server.common.exception.type.ErrorCode.FILE_03
import com.blueoauld.server.common.properties.R2Properties
import com.blueoauld.server.r2.application.response.UploadUrlResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.time.Duration

@Service
class R2Provider(

    private val s3Client: S3Client,
    private val s3Presigner: S3Presigner,
    private val r2Properties: R2Properties
) {

    companion object {
        private const val MAX_UPLOAD_SIZE = 3L * 1024 * 1024 * 1024 // 3GB
    }

    private val log = KotlinLogging.logger {}

    fun createUploadUrl(key: String, contentType: String, contentLength: Long, expiry: Duration): UploadUrlResponse {
        if (contentLength !in 1..MAX_UPLOAD_SIZE) {
            throw CustomException(FILE_03)
        }

        val putObjectRequest = PutObjectRequest.builder()
            .bucket(r2Properties.bucket)
            .key(key)
            .contentType(contentType)
            .contentLength(contentLength)
            .build()

        val url = s3Presigner.presignPutObject(
            PutObjectPresignRequest.builder()
                .signatureDuration(expiry)
                .putObjectRequest(putObjectRequest)
                .build()
        ).url().toString()

        return UploadUrlResponse(url, key)
    }

    fun createDownloadUrl(key: String, expiry: Duration = Duration.ofMinutes(30)): String {
        val getObjectRequest = GetObjectRequest.builder()
            .bucket(r2Properties.bucket)
            .key(key)
            .build()

        return s3Presigner.presignGetObject(
            GetObjectPresignRequest.builder()
                .signatureDuration(expiry)
                .getObjectRequest(getObjectRequest)
                .build()
        ).url().toString()
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

    fun deleteAll(keys: List<String>) {
        keys.chunked(1000).forEach { chunk ->
            try {
                val objects = chunk.map { ObjectIdentifier.builder().key(it).build() }
                val result = s3Client.deleteObjects(
                    DeleteObjectsRequest.builder()
                        .bucket(r2Properties.bucket)
                        .delete(Delete.builder().objects(objects).build())
                        .build()
                )

                if (result.hasErrors()) {
                    result.errors().forEach { err ->
                        log.error { "R2 객체 삭제에 실패했습니다. 키 = ${err.key()}, 코드 = ${err.code()}, 메세지 = ${err.message()}" }
                    }
                }
            } catch (e: Exception) {
                log.error(e) { "R2 배치 삭제에 오류가 발생했습니다. 키 = $chunk" }
            }
        }
    }
}