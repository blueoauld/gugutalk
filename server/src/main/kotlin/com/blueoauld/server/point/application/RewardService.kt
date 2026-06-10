package com.blueoauld.server.point.application

import com.blueoauld.server.common.exception.CustomException
import com.blueoauld.server.common.exception.type.ErrorCode.POINT_01
import com.blueoauld.server.common.exception.type.ErrorCode.REWARD_01
import com.blueoauld.server.point.entity.PointHistory
import com.blueoauld.server.point.entity.RewardTransaction
import com.blueoauld.server.point.entity.type.PointSource.ADVERTISEMENT
import com.blueoauld.server.point.repository.PointHistoryRepository
import com.blueoauld.server.point.repository.PointRepository
import com.blueoauld.server.point.repository.RewardTransactionRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RewardService(

    private val rewardTransactionRepository: RewardTransactionRepository,
    private val pointRepository: PointRepository,
    private val pointHistoryRepository: PointHistoryRepository,
) {

    @Transactional
    fun grantReward(transactionId: String, userId: String?) {
        if (rewardTransactionRepository.existsById(transactionId)) {
            return
        }

        if (userId == null) {
            throw CustomException(REWARD_01)
        }

        val memberId = userId.toLong()
        val point = pointRepository.findByMemberId(memberId) ?: throw CustomException(POINT_01)

        try {
            val rewardTransaction = RewardTransaction(
                id = transactionId,
                memberId = memberId,
                amount = ADVERTISEMENT.point
            )
            rewardTransactionRepository.save(rewardTransaction)
        } catch (_: DataIntegrityViolationException) {
            return
        }

        val pointHistory = PointHistory(
            pointId = point.id,
            source = ADVERTISEMENT,
            balanceSnapshot = point.balance
        )
        pointHistoryRepository.save(pointHistory)

        point.earn(ADVERTISEMENT.point)
    }
}