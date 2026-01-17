package com.mobile.soundscape.onboarding

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mobile.soundscape.api.client.RetrofitClient
import com.mobile.soundscape.api.dto.BaseResponse
import com.mobile.soundscape.api.dto.EvaluationRequest
import com.mobile.soundscape.api.dto.OnboardingSelectedRequest
import com.mobile.soundscape.data.OnboardingManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.mobile.soundscape.data.LocalArtistModel
import kotlin.collections.toMutableList

class OnboardingViewModel : ViewModel() {
    var nickname: String = ""
    var selectedArtists = mutableListOf<LocalArtistModel>()
    var selectedGenres: MutableList<String> = mutableListOf()
    val TAG ="OnboardingViewModel"

    // 통신 결과를 프래그먼트에게 알려주기 위한 LiveData
    // true: 성공, false: 실패 (또는 null: 대기)
    private val _onboardingResult = MutableLiveData<Boolean>()
    val onboardingResult: LiveData<Boolean> get() = _onboardingResult

    private val _Nickname = MutableLiveData<String>()
    val Nickname: LiveData<String> get() = _Nickname

    private val _artistList = MutableLiveData<List<LocalArtistModel>>()
    val artistList: LiveData<List<LocalArtistModel>> get() = _artistList

    private val _genreList = MutableLiveData<List<String>>()
    val genreList: LiveData<List<String>> get() = _genreList


    // 최종 수집된 내용을 서버로 전송
    fun submitOnboarding() {
        val artistNameList = selectedArtists.map { it.name }
        // DTO 생성
        val request = OnboardingSelectedRequest(
            nickname = nickname,
            artists = artistNameList,
            genres = selectedGenres
        )

        // [실제 서버 통신]
        RetrofitClient.onboardingApi.sendOnboarding(request).enqueue(object : Callback<BaseResponse<String>> {
            override fun onResponse(
                call: Call<BaseResponse<String>>,
                response: Response<BaseResponse<String>>
            ) {
                if (response.isSuccessful) {
                    // 프래그먼트한테 서버 통신 성공 알림
                    _onboardingResult.value = true
                } else {
                    // 프래그먼트한테 서버 통신 실패 알림
                    _onboardingResult.value = false
                }
            }

            override fun onFailure(call: Call<BaseResponse<String>>, t: Throwable) {
                Log.e("API_FAIL", "통신 실패: ${t.message}")
                _onboardingResult.value = false
            }
        })
    }


    fun loadSavedData(context: Context) {
        // 닉네임 불러오기
        val savedNickname = OnboardingManager.getNickname(context)
        if (savedNickname.isNotEmpty()) {
            nickname = savedNickname
            _Nickname.value = nickname
        }

        // artist 불러오기
        val savedArtists = OnboardingManager.getArtistList(context)
        if (savedArtists.isNotEmpty()) {
            selectedArtists = savedArtists.toMutableList()
            _artistList.value = savedArtists
        }

        // 장르 불러오기
        val savedGenre = OnboardingManager.getGenreList(context)
        if (savedGenre.isNotEmpty()) {
            selectedGenres = savedGenre.toMutableList()
            _genreList.value = savedGenre
        }

    }

    fun updateNickname(context: Context, name: String) {
        nickname = name
        _Nickname.value = nickname
        OnboardingManager.saveNickname(context, nickname)
    }

    fun updateArtists(context: Context, list: List<LocalArtistModel>) {
        selectedArtists = list.toMutableList()
        _artistList.value = list

        // 저장소에도 객체 리스트 저장 (Gson 사용된 메서드 호출)
        OnboardingManager.saveArtistList(context, list)
    }

    fun updateGenres(context: Context, list: List<String>) {
        selectedGenres = list.toMutableList()
        _genreList.value = list
        OnboardingManager.saveGenreList(context, list)
    }
}