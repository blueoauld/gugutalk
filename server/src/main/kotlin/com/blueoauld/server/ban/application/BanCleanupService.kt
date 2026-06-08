package com.blueoauld.server.ban.application

import com.blueoauld.server.ban.repository.BanRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BanCleanupService(

    private val banRepository: BanRepository,
) {

    @Transactional
    fun deleteBatch(ids: List<Long>) {
        if (ids.isEmpty()) {
            return
        }

        banRepository.hardDeleteByIds(ids)
    }
}