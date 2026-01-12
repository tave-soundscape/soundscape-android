package com.mobile.soundscape.recommendation

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.mobile.soundscape.R
import com.mobile.soundscape.api.client.RetrofitClient
import com.mobile.soundscape.api.dto.BaseResponse
import com.mobile.soundscape.api.dto.RecommendationRequest
import com.mobile.soundscape.api.dto.RecommendationResponse
import com.mobile.soundscape.data.AppDatabase
import com.mobile.soundscape.data.PlaylistHistory
import com.mobile.soundscape.data.RecommendationManager
import com.mobile.soundscape.databinding.FragmentRecResultBinding
import com.mobile.soundscape.result.PlaylistResultActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.getValue

class RecResultFragment : Fragment() {

    private var _binding: FragmentRecResultBinding? = null
    private val binding get() = _binding!!
    val TAG = "PlayTest"
    private val viewModel: RecommendationViewModel by activityViewModels()

    private var isDataLoaded = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 구슬 움직이는 animation
        com.bumptech.glide.Glide.with(this)
            .load(R.drawable.orb_animation)
            .into(binding.centerButton)

        sendRecommendationRequest()

        // 5초 딜레이 시작 (Coroutines 사용)
        // (viewLifecycleOwner를 사용해야 화면이 꺼지면 타이머도 안전하게 종료됨)
        viewLifecycleOwner.lifecycleScope.launch {
            delay(5000) // 5000ms = 5초 대기

            updateUIForCompletion()
        }

        // "보러가기" 버튼 클릭 시 -> ListFragment로 이동
        binding.nextButton.setOnClickListener {
            if (isDataLoaded) {
                findNavController().navigate(R.id.action_recResultFragment_to_listFragment)
            } else {
                // 아직 데이터 저장 중이면 조금만 기다려달라고 하기
                Toast.makeText(requireContext(), "플레이리스트를 불러오는 중입니다. 잠시만 기다려주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * [백엔드 전송 함수]
     * 1. 뷰모델의 (장소, 데시벨, 목표)를 모두 꺼내서 서버로 전송
     * 2. 서버에서 받은 결과(RecommendationResponse)를 뷰모델에 저장
     * 3. 다음 화면으로 이동
     */
    private fun sendRecommendationRequest() {
        // 1. 뷰모델에서 장소, 데시벨, 목표 꺼내서 서버로 전송
        val request = RecommendationRequest(
            place = viewModel.place,
            decibel = viewModel.decibel,
            goal = viewModel.goal
        )
        viewModel.checkData()

        // 서버에서 응답 받아서 뷰모델에 저장
        RetrofitClient.recommendationApi.sendRecommendations(request).enqueue(object : Callback<BaseResponse<RecommendationResponse>> {

            override fun onResponse(
                call: Call<BaseResponse<RecommendationResponse>>,
                response: Response<BaseResponse<RecommendationResponse>>
            )
            {
                if (response.isSuccessful) {
                    val baseResponse = response.body()
                    val resultData = baseResponse?.data

                    if (resultData != null) {
                        isDataLoaded = true

                        // 메모리(싱글톤)에 저장
                        RecommendationManager.place = viewModel.place
                        RecommendationManager.goal = viewModel.goal
                        RecommendationManager.cachedPlaylist = resultData
                        // 뷰모델 업데이트
                        viewModel.currentPlaylist.value = resultData

                        // 내부 저장소에 플리 이름 저장 -> result의 두 프래그먼트에서 동일한 이름 패치되도록
                        context?.let { ctx ->
                            com.mobile.soundscape.PreferenceManager.setPlaylistExperienced(ctx, true)

                            RecommendationManager.savePlaylistName(ctx, resultData.playlistName)
                            RecommendationManager.savePlaylistId(ctx, resultData.playlistId.toString())

                            // Room Database 에 최근 기록 저장
                            saveToRoomHistory(ctx, viewModel.place, viewModel.goal, resultData.playlistId)
                        }
                    } else {
                        Toast.makeText(context, "서버 응답이 비어있습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e(TAG, "서버 에러 코드: ${response.code()}")
                    Toast.makeText(context, "서버 에러 발생", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BaseResponse<RecommendationResponse>>, t: Throwable) {
                Log.e(TAG, "통신 실패: ${t.message}")
                Toast.makeText(context, "네트워크 연결을 확인해주세요.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateUIForCompletion() {
        // 뷰 바인딩이 유효한지 확인 (혹시 모를 크래시 방지)
        if (_binding == null) return

        binding.apply {
            // 1. 텍스트 변경
            tvSubtitle.text = "오늘의 몰입 플레이리스트가\n완성됐어요"

            // 2. "잠시만 기다려주세요" 텍스트 숨기기
            tvSubtitle2.visibility = View.GONE

            // 3. 버튼과 타원(ellipse) 보이기
            nextButton.visibility = View.VISIBLE
            ellipse.visibility = View.VISIBLE

            // 보러가기 버튼 애니메이션 주기
            nextButton.alpha = 0f
            nextButton.animate().alpha(1f).setDuration(500).start()

            ellipse.alpha = 0f
            ellipse.animate().alpha(1f).setDuration(1000).start()
        }
    }

    private fun saveToRoomHistory(context: Context, place: String, goal: String, playlistId: Int) {
        val history = PlaylistHistory(
            place = place,
            goal = translateGoal(goal),
            iconResName = getIconNameForPlace(place),
            playlistId = playlistId,
            timestamp = System.currentTimeMillis()
        )

        // GlobalScope나 별도 Scope 대신 viewLifecycleOwner.lifecycleScope 사용 권장
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                AppDatabase.getDatabase(context).historyDao().insert(history)
                // ✅ 로그캣에 이 문구가 뜨는지 꼭 확인하세요!
                Log.d("RoomDB_CHECK", "저장 성공! ID: $playlistId, Place: $place")
            } catch (e: Exception) {
                Log.e("RoomDB_CHECK", "저장 실패: ${e.message}")
            }
        }
    }
    //영문 -> 한글로
    private fun translateGoal(goal: String?): String {
        return when (goal?.lowercase()) {
            "sleep" -> "수면"
            "focus" -> "집중"
            "cosolation" -> "위로"
            "active" -> "활력"
            "stabilization" -> "안정"
            "anger" -> "분노"
            "relax" -> "휴식"
            "neutral", null -> "미선택"
            else -> goal ?: "미선택"
        }
    }

    //홈에 뜨게 할 아이콘
    private fun getIconNameForPlace(place: String): String {
        return when (place) {
            "집/실내" -> "place_icon0"
            "카페" -> "place_icon1"
            "코워킹" -> "place_icon2"
            "헬스장" -> "place_icon3"
            "도서관" -> "place_icon4"
            "이동중" -> "place_icon5"
            "공원" -> "place_icon6"
            else -> "place_icon0"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}