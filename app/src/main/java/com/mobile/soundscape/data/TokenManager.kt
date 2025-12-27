package com.mobile.soundscape.data

import android.content.Context
import android.content.SharedPreferences

/**
 * [TokenManager]
 * 앱 내부 저장소(SharedPreferences)에 토큰을 저장하고 꺼내 쓰는 관리자
 * object 키워드를 사용해 싱글톤(전역에서 하나만 존재)으로 만듦
 */
object TokenManager {

    private const val PREFS_NAME = "soundscape_prefs" // 저장소 이름
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_TOKEN_SAVE_TIME = "token_save_time" // 저장된 시간
    private const val TOKEN_VALIDITY_MS = 55 * 60 * 1000L

    // 내부 저장소(SharedPreferences) 객체를 가져오는 함수
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * 토큰 저장하기 (로그인 성공 시 호출)
     */
    fun saveToken(context: Context, accessToken: String, refreshToken: String?) {
        val editor = getPreferences(context).edit()
        val currentTime = System.currentTimeMillis() // 현재 시간 (ms)

        editor.putString(KEY_ACCESS_TOKEN, accessToken)
        editor.putLong(KEY_TOKEN_SAVE_TIME, currentTime) // 시간 저장

        if (refreshToken != null) {
            editor.putString(KEY_REFRESH_TOKEN, refreshToken)
        }
        editor.apply() // 저장 확정 (비동기)
    }

    /**
     * 토큰이 유효한지 검사하는 함수 (Splash 화면에서 사용)
     * @return true: 유효함(로그인 상태), false: 만료됨 or 토큰 없음(로그인 필요)
     */
    fun isTokenValid(context: Context): Boolean {
        val prefs = getPreferences(context)
        val accessToken = prefs.getString(KEY_ACCESS_TOKEN, null)
        val saveTime = prefs.getLong(KEY_TOKEN_SAVE_TIME, 0L)

        // 1. 토큰 자체가 없으면 무효
        if (accessToken == null) return false

        // 2. 시간 계산 (현재 시간 - 저장된 시간)
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - saveTime

        // 3. 경과 시간이 유효기간(55분) 이내인지 확인
        return elapsedTime < TOKEN_VALIDITY_MS
    }

    /**
     * 엑세스 토큰 꺼내기 (음악 재생할 때 호출)
     */
    fun getAccessToken(context: Context): String? {
        return getPreferences(context).getString(KEY_ACCESS_TOKEN, null)
    }

    /**
     * 리프레시 토큰 꺼내기 (토큰 만료 시 갱신할 때 호출)
     */
    fun getRefreshToken(context: Context): String? {
        return getPreferences(context).getString(KEY_REFRESH_TOKEN, null)
    }

    /**
     * 토큰 삭제하기 (로그아웃 시 호출)
     */
    fun clearTokens(context: Context) {
        val editor = getPreferences(context).edit()
        editor.clear()
        editor.apply()
    }
}