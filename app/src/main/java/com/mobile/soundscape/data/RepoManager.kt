package com.mobile.soundscape.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mobile.soundscape.api.dto.RecommendationResponse
import com.mobile.soundscape.data.OnboardingManager.PREFS_NAME
import com.mobile.soundscape.data.OnboardingManager.getPreferences

// 문제점: recommendation에서 사용하는 뷰모델을 패키지가 다른 ListFragment에서 사용할 수 없음
// 싱글톤 형식으로 앱 어디서든 접근 가능한 공용 창고

object RecommendationManager {
    var place: String = ""
    var decibel: Float = 0.0f
    var goal: String = ""
    var englishPlace: String = ""
    var englishGoal: String = ""

    var cachedPlaylist: RecommendationResponse? = null
    private const val PREFS_NAME = "soundscape_prefs" // 저장소 이름 상수화
    private const val KEY_PLAYLIST_NAME = "my_playlist_name" // 플리 이름 키
    private const val KEY_PLAYLIST_ID = "my_playlist_id" // 플리 이름 키

    private fun getPreferences(context: Context): android.content.SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // cachedPlaylist의 playlistName만 수정하는 함수
    fun updatePlaylistName(newName: String) {
        // 데이터 클래스는 불변(val)일 수 있으므로 copy()를 사용하여 안전하게 수정
        cachedPlaylist = cachedPlaylist?.copy(playlistName = newName)
    }

    // 플리 이름 저장
    fun savePlaylistName(context: Context, playlistName: String) {
        getPreferences(context).edit().putString(KEY_PLAYLIST_NAME, playlistName).apply()
    }

    // 플리 이름 불러오기
    fun getPlaylistName(context: Context): String {
        return getPreferences(context).getString(KEY_PLAYLIST_NAME, "") ?: ""
    }

    // 플리 ID 저장
    fun savePlaylistId(context: Context, playlistName: String) {
        getPreferences(context).edit().putString(KEY_PLAYLIST_ID, playlistName).apply()
    }

    // 플리 ID 불러오기
    fun getPlaylistId(context: Context): String {
        return getPreferences(context).getString(KEY_PLAYLIST_ID, "") ?: ""
    }

}

/**
 * [OnboardingManager]
 * ViewModel 사용하면 해당 액티비티에서만 유효하고 다른 액티비티에서 사라짐
 * 온보딩이 끝난 후 manager 사용해서 내부저장소에 저장
 * 마이페이지에서 사용!
 */



object OnboardingManager {
    private const val PREFS_NAME = "soundscape_prefs" // 저장소 이름 상수화

    private const val KEY_MY_NICKNAME = "my_nickname"
    private const val KEY_MY_ARTISTS = "my_artists"
    private const val KEY_MY_GENRE = "my_genre"

    // 공통으로 사용하는 Prefs 가져오기
    private fun getPreferences(context: Context): android.content.SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // 닉네임 저장
    fun saveNickname(context: Context, nickname: String) {
        getPreferences(context).edit().putString(KEY_MY_NICKNAME, nickname).apply()
    }

    // 닉네임 불러오기
    fun getNickname(context: Context): String {
        val nickname = getPreferences(context).getString(KEY_MY_NICKNAME, "") ?: ""
        if (nickname.isEmpty()) return ""
        return nickname
    }

    // 아티스트 목록 저장 (List -> String 변환, 콤마로 구분)
    fun saveArtistList(context: Context, list: List<LocalArtistModel>) {
        val gson= Gson()
        val joinString = gson.toJson(list)
        getPreferences(context).edit().putString(KEY_MY_ARTISTS, joinString).apply()
    }

    // 아티스트 목록 불러오기 (String -> List 변환)
    fun getArtistList(context: Context): List<LocalArtistModel> {
        val joinString = getPreferences(context).getString(KEY_MY_ARTISTS, "") ?: ""
        if (joinString.isEmpty()) return emptyList()

        val gson = Gson()
        val type = object : TypeToken<List<LocalArtistModel>>() {}.type
        return gson.fromJson(joinString, type)
    }

    // 장르 목록 저장
    fun saveGenreList(context: Context, list: List<String>){
        val joinString = list.joinToString(",")
        getPreferences(context).edit().putString(KEY_MY_GENRE, joinString).apply()
    }

    // 장르 목록 불러오기
    fun getGenreList(context: Context): List<String> {
        val joinString = getPreferences(context).getString(KEY_MY_GENRE, "") ?: ""
        if (joinString.isEmpty()) return emptyList()
        return joinString.split(",")
    }

}


// 스플래시에서 쓰는거 - 업데이트 후 최초 1회 업데이트
object PreferenceManager {
    private const val PREF_NAME = "my_app_prefs"

    // 이번 업데이트에서 최초 1회 온보딩을 다시 실행해야함.
    // v2를 붙여서 키 이름을 변경
    // 기존 사용자는 이 키를 가지고 있지 않으므로 false가 반환됩니다.
    private const val KEY_ONBOARDING_DONE = "is_onboarding_done_v2"

    fun isOnboardingFinished(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        // 기본값 false: 키가 없으면(업데이트 직후) 안 한 걸로 간주
        return prefs.getBoolean(KEY_ONBOARDING_DONE, false)
    }

    fun setOnboardingFinished(context: Context, isFinished: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_ONBOARDING_DONE, isFinished).apply()
    }
}