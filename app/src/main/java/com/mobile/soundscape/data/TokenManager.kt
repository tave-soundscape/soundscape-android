package com.mobile.soundscape.data

import android.content.Context
import android.content.SharedPreferences

/**
 * TokenManager
 * 앱 내부 저장소(SharedPreferences)에 토큰을 저장하고 꺼내 쓰는 관리자
 * object 키워드를 사용해 싱글톤(전역에서 하나만 존재)으로 만듦
 */
object TokenManager {

    private const val PREFS_NAME = "soundscape_prefs" // 저장소 이름
    private const val KEY_ACCESS_TOKEN = "access_token" // JWT (백엔드 토큰)
    private const val KEY_REFRESH_TOKEN = "refresh_token"

    // 내부 저장소(SharedPreferences) 객체를 가져오는 함수
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * 토큰 저장 함수
     * @param accessToken : 백엔드에서 받은 JWT (필수)
     * @param refreshToken : 백엔드에서 null을 준다고 했으니 String? (Nullable)로 받음
     */
    // 토큰 저장 관리 함수
    fun saveToken(context: Context, accessToken: String, refreshToken: String?) {
        val editor = getPreferences(context).edit()

        // JWT 액세스 토큰 저장
        editor.putString(KEY_ACCESS_TOKEN, accessToken)

        // 리프레시 토큰 처리
        if (refreshToken != null) {
            editor.putString(KEY_REFRESH_TOKEN, refreshToken)
        } else {
            editor.remove(KEY_REFRESH_TOKEN)
        }
        editor.apply() // 저장 확정 (비동기)
    }

    // 저장된 JWT 가져오기 (API 호출할 때 사용)
    fun getAccessToken(context: Context): String? {
        return getPreferences(context).getString(KEY_ACCESS_TOKEN, null)
    }

    // 저장된 리프레시 토큰 가져오기
    fun getRefreshToken(context: Context): String? {
        return getPreferences(context).getString(KEY_REFRESH_TOKEN, null)
    }

    // 로그아웃 시 삭제
    fun clearTokens(context: Context) {
        val editor = getPreferences(context).edit()
        editor.clear()
        editor.apply()
    }
}