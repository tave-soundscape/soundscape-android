package com.mobile.soundscape.data

import android.content.Context
import com.mobile.soundscape.api.dto.RecommendationResponse

// 문제점: recommendation에서 사용하는 뷰모델을 패키지가 다른 ListFragment에서 사용할 수 없음
// 싱글톤 형식으로 앱 어디서든 접근 가능한 공용 창고

object RecommendationManager {
    var place: String = ""
    var goal: String = ""
    var cachedPlaylist: RecommendationResponse? = null
}

/**
 * [OnboardingManager]
 * ViewModel 사용하면 해당 액티비티에서만 유효하고 다른 액티비티에서 사라짐
 * 온보딩이 끝난 후 manager 사용해서 내부저장소에 저장
 * 마이페이지에서 사용!
 */

object OnboardingManager {
    private const val KEY_MY_NICKNAME = "my_nickname"
    private const val KEY_MY_ARTISTS = "my_artists"
    private const val KEY_MY_GENRE = "my_genre"


    // 닉네임 저장
    fun saveNickname(context: Context, nickname: String) {
        val prefs = context.getSharedPreferences("soundscape_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_MY_NICKNAME, nickname).apply()
    }

    // 아티스트 목록 불러오기 (String -> List 변환)
    fun getNickname(context: Context): String {
        val prefs = context.getSharedPreferences("soundscape_prefs", Context.MODE_PRIVATE)
        val nickname = prefs.getString(KEY_MY_NICKNAME, "") ?: ""

        if (nickname.isEmpty()) return ""
        return nickname
    }

    // 아티스트 목록 저장 (List -> String 변환, 콤마로 구분)
    fun saveArtistList(context: Context, list: List<String>) {
        val prefs = context.getSharedPreferences("soundscape_prefs", Context.MODE_PRIVATE)
        // 예: ["아이유", "BTS"] -> "아이유,BTS"
        val joinString = list.joinToString(",")
        prefs.edit().putString(KEY_MY_ARTISTS, joinString).apply()
    }

    // 아티스트 목록 불러오기 (String -> List 변환)
    fun getArtistList(context: Context): List<String> {
        val prefs = context.getSharedPreferences("soundscape_prefs", Context.MODE_PRIVATE)
        val joinString = prefs.getString(KEY_MY_ARTISTS, "") ?: ""

        if (joinString.isEmpty()) return emptyList()
        return joinString.split(",")
    }

    // 장르 목록 저장
    fun saveGenreList(context: Context, list: List<String>){
        val prefs = context.getSharedPreferences("soundscape_prefs", Context.MODE_PRIVATE)
        val joinString = list.joinToString(",")
        prefs.edit().putString(KEY_MY_GENRE, joinString).apply()
    }

    // 장르 목록 불러오기
    fun getGenreList(context: Context): List<String> {
        val prefs = context.getSharedPreferences("soundscape_prefs", Context.MODE_PRIVATE)
        val joinString = prefs.getString(KEY_MY_GENRE, "") ?: ""

        if (joinString.isEmpty()) return emptyList()
        return joinString.split(",")
    }

}