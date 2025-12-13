package com.mobile.soundscape.onboarding

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.mobile.soundscape.api.SpotifyClient
import com.mobile.soundscape.data.local.TokenManager
import com.mobile.soundscape.data.model.music.DeviceResponse
import com.mobile.soundscape.data.model.music.PlayRequest
import com.mobile.soundscape.databinding.ActivityPlaytestBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PlaytestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlaytestBinding


    // 1. 앨범 (Card 1용)
    private val album1Uri = "https://open.spotify.com/album/3ypVkFQVBorVyk2hdaYs4w?si=TOZBEQm6T9GGpUolLXXszw"
    private val track1Img = "https://image.bugsm.co.kr/album/images/500/207092/20709260.jpg"

    // 2. 곡 하나 (Card 2용)
    private val singleTrackUri = "https://open.spotify.com/track/3cLXrotzCjYCkN73PewALM?si=b6b99af007634195"
    private val track2Img = "https://image.bugsm.co.kr/album/images/500/41305/4130508.jpg"

    // 3. (Card 3용)
    private val track3Uri = ""
    private val track3Img = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaytestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 이미지 세팅
        Glide.with(this).load(track1Img).into(binding.imgTrack1)
        Glide.with(this).load(track2Img).into(binding.imgTrack2)
        Glide.with(this).load(track3Img).into(binding.imgTrack3)

        // --- 클릭 리스너 ---

        // 1. 앨범 재생 (기존 방식)
        binding.cardTrack1.setOnClickListener {
            playAlbum(album1Uri, "NMIXX 앨범")
        }

        // 2. 곡 한 개 재생 (★ 새로 만든 간편한 방식!)
        binding.cardTrack2.setOnClickListener {
            // 리스트(listOf) 안 만들고 그냥 String 하나만 넣으면 됨!
            playSingleTrack(singleTrackUri, "화사 곡")
        }

        binding.cardTrack3.setOnClickListener {
            Toast.makeText(this, "준비중", Toast.LENGTH_SHORT).show()
        }
    }

    // =========================================================
    // ★ 1. 곡 한 개만 재생하는 전용 함수 (String 입력)
    // =========================================================
    private fun playSingleTrack(trackUri: String, logMsg: String) {
        // 내부에서 알아서 리스트로 포장해줍니다.
        val requestBody = PlayRequest(
            uris = listOf(trackUri), // 여기서 포장!
            contextUri = null
        )
        // 공통 로직으로 넘김
        findDeviceAndPlay(requestBody, logMsg)
    }

    // =========================================================
    // ★ 2. 앨범 재생하는 전용 함수 (String 입력)
    // =========================================================
    private fun playAlbum(albumUri: String, logMsg: String) {
        val requestBody = PlayRequest(
            contextUri = albumUri,
            uris = null
        )
        // 공통 로직으로 넘김
        findDeviceAndPlay(requestBody, logMsg)
    }

    // =========================================================
    // ★ 3. 공통 로직 (기기 찾기 -> 전송)
    // =========================================================
    private fun findDeviceAndPlay(requestBody: PlayRequest, logMsg: String) {
        val token = TokenManager.getAccessToken(this)
        if (token == null) {
            Toast.makeText(this, "토큰 없음", Toast.LENGTH_SHORT).show()
            return
        }

        // 기기 찾기
        SpotifyClient.api.getAvailableDevices("Bearer $token").enqueue(object : Callback<DeviceResponse> {
            override fun onResponse(call: Call<DeviceResponse>, response: Response<DeviceResponse>) {
                if (response.isSuccessful) {
                    val devices = response.body()?.devices
                    if (!devices.isNullOrEmpty()) {
                        val targetDevice = devices[0]
                        Log.d("PlayTest", "기기 발견: ${targetDevice.name}")

                        // 찾은 기기로 요청 전송
                        sendRequestToSpotify(token, requestBody, targetDevice.id, logMsg)

                    } else {
                        binding.tvStatusLog.text = "⚠️ 스포티파이 앱을 켜주세요!"
                    }
                } else {
                    Log.e("PlayTest", "기기 조회 실패: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<DeviceResponse>, t: Throwable) {
                Log.e("PlayTest", "통신 실패: ${t.message}")
            }
        })
    }

    // =========================================================
    // ★ 4. 최종 전송 (Retrofit)
    // =========================================================
    private fun sendRequestToSpotify(token: String, body: PlayRequest, deviceId: String, logMsg: String) {
        SpotifyClient.api.playTrack("Bearer $token", body, deviceId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("PlayTest", "성공: $logMsg")
                    binding.tvStatusLog.text = "🎵 재생 중: $logMsg"
                    Toast.makeText(applicationContext, "재생 성공!", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("PlayTest", "실패: ${response.code()}")
                    binding.tvStatusLog.text = "⚠️ 오류: ${response.code()}"
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("PlayTest", "전송 실패: ${t.message}")
            }
        })
    }
}