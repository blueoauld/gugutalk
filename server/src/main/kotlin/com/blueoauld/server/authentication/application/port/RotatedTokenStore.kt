package com.blueoauld.server.authentication.application.port

/**
 * 리프레시 토큰 회전(rotate) 결과를 짧은 grace window 동안 기록한다.
 *
 * 회전은 1회용이라 기존 토큰을 즉시 폐기하는데, 회전 응답이 네트워크 유실되면 클라이언트는 옛(이미 폐기된)
 * 토큰을 계속 들고 있어 이후 회전이 영구히 실패한다. 옛 토큰 → 새로 발급한 토큰 쌍을 잠시 보관해 두면,
 * 같은 옛 토큰으로 들어온 재시도에 동일한 결과를 돌려줄 수 있어(멱등) 이 문제를 막는다.
 */
interface RotatedTokenStore {

    fun save(oldRefreshToken: String, memberId: Long, accessToken: String, refreshToken: String)

    fun get(oldRefreshToken: String): RotatedTokens?
}

data class RotatedTokens(

    val memberId: Long,
    val accessToken: String,
    val refreshToken: String,
)
