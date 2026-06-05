package com.blueoauld.server.push.application

import com.blueoauld.server.push.entity.Push
import com.blueoauld.server.push.repository.PushRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PushService(

    private val pushRepository: PushRepository,
) {

    @Transactional
    fun upsert(memberId: Long, token: String) {
        val push = pushRepository.findByToken(token)

        if (push == null) {
            pushRepository.save(Push(token = token, memberId = memberId))
            return
        }

        push.touch(memberId)
    }

    @Transactional
    fun delete(token: String) {
        pushRepository.deleteByToken(token)
    }
}