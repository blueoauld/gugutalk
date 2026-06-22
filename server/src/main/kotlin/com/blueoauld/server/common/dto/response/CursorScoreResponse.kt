package com.blueoauld.server.common.dto.response

data class CursorScoreResponse<T>(

    val payload: List<T>,
    val nextId: Long?,
    val nextScore: Long?,
    val hasNext: Boolean
) {

    companion object {

        /**
         * size + 1 건을 조회한 결과(rows)로 점수 기반 커서 응답을 만든다.
         * rows 크기가 size 를 넘으면 다음 페이지가 있는 것으로 보고 마지막 한 건을 잘라낸다.
         * nextId / nextScore 는 잘라낸 뒤 마지막 항목에서 추출한다.
         */
        inline fun <T> of(
            rows: List<T>,
            size: Int,
            nextId: (T) -> Long?,
            nextScore: (T) -> Long?,
        ): CursorScoreResponse<T> {
            val hasNext = rows.size > size
            val items = if (hasNext) rows.dropLast(1) else rows
            val last = items.lastOrNull()

            return CursorScoreResponse(
                payload = items,
                nextId = last?.let(nextId),
                nextScore = last?.let(nextScore),
                hasNext = hasNext,
            )
        }
    }
}