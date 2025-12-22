package com.mobile.soundscape.data.local

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

    // 내부 저장소(SharedPreferences) 객체를 가져오는 함수
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * 토큰 저장하기 (로그인 성공 시 호출)
     */
    fun saveToken(context: Context, accessToken: String, refreshToken: String?) {
        val editor = getPreferences(context).edit()
        editor.putString(KEY_ACCESS_TOKEN, accessToken)
        if (refreshToken != null) {
            editor.putString(KEY_REFRESH_TOKEN, refreshToken)
        }
        editor.apply() // 저장 확정 (비동기)
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