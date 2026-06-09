package com.blueoauld.server.authentication.infrastructure

import com.blueoauld.server.authentication.application.port.MessageSender
import com.blueoauld.server.common.exception.CustomException
import com.blueoauld.server.common.exception.type.ErrorCode.VERIFICATION_CODE_03
import com.blueoauld.server.common.properties.SmsProperties
import com.solapi.sdk.SolapiClient.createInstance
import com.solapi.sdk.message.exception.SolapiMessageNotReceivedException
import com.solapi.sdk.message.exception.SolapiUnknownException
import com.solapi.sdk.message.model.Message
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Repository

@Repository
class VerificationMessageSender(

    private val smsProperties: SmsProperties,
) : MessageSender {

    private val log = KotlinLogging.logger {}

    private val messageService by lazy {
        createInstance(smsProperties.apiKey, smsProperties.apiSecret)
    }

    override fun send(to: String, code: String) {
        val message = Message().apply {
            this.from = smsProperties.phone
            this.to = to
            this.text = "구구톡 인증 번호는 [$code] 입니다."
        }

        try {
            messageService.send(message)
        } catch (e: Exception) {
            log.error(e) { e.message }

            when (e) {
                is SolapiMessageNotReceivedException, is SolapiUnknownException -> throw CustomException(
                    VERIFICATION_CODE_03
                )
            }
        }
    }
}