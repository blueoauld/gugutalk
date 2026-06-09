package com.blueoauld.server.push.application

import com.blueoauld.server.common.properties.ApnsProperties
import com.blueoauld.server.push.repository.PushRepository
import com.eatthepath.pushy.apns.ApnsClient
import com.eatthepath.pushy.apns.util.SimpleApnsPayloadBuilder
import com.eatthepath.pushy.apns.util.SimpleApnsPushNotification
import com.eatthepath.pushy.apns.util.TokenUtil
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class PushSender(

    private val apnsClient: ApnsClient,
    private val pushRepository: PushRepository,
    private val apnsProperties: ApnsProperties
) {

    private val log = KotlinLogging.logger {}

    fun sendToMember(
        memberId: Long,
        title: String,
        body: String,
        threadId: String? = null,
        data: Map<String, String> = emptyMap(),
    ) {
        pushRepository.findAllByMemberId(memberId)
            .map { it.token }
            .forEach { send(it, title, body, threadId, data) }
    }

    private fun send(
        token: String,
        title: String,
        body: String,
        threadId: String?,
        data: Map<String, String> = emptyMap(),
    ) {
        val payload = SimpleApnsPayloadBuilder()
            .setAlertTitle(title)
            .setAlertBody(body)
            .setSound("default")
            .setBadgeNumber(0)
            .apply { threadId?.let { setThreadId(it) } }
            .apply { data.forEach { (k, v) -> addCustomProperty(k, v) } }
            .build()

        val notification = SimpleApnsPushNotification(
            TokenUtil.sanitizeTokenString(token),
            apnsProperties.bundleId,
            payload,
        )

        apnsClient.sendNotification(notification).whenComplete { response, throwable ->
            when {
                throwable != null -> error { "APNs 전송에 실패했습니다. 토큰 = $token" }
                response.isAccepted -> log.info { "APNs 전송에 성공했습니다. 토큰 = $token" }

                else -> {
                    val reason = response.rejectionReason.orElse("UNKNOWN")
                    log.warn { "APNs 전송에 거절당했습니다. 토큰 = $token, 사유 = $reason" }

                    if (reason == "BadDeviceToken" || reason == "Unregistered" ||
                        response.tokenInvalidationTimestamp.isPresent
                    ) {
                        pushRepository.deleteByToken(token)
                    }
                }
            }
        }
    }
}