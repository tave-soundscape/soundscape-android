package com.mobile.soundscape.recommendation

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
import com.mobile.soundscape.data.RecommendationManager
import com.mobile.soundscape.databinding.FragmentRecResultBinding
import com.mobile.soundscape.result.PlaylistResultActivity
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

        // "보러가기" 버튼을 누르면 -> 플레이리스트로 이동
        binding.nextButton.setOnClickListener {
            val intent = android.content.Intent(requireContext(), PlaylistResultActivity::class.java)
            startActivity(intent)
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
            place = viewModel.englishPlace,
            decibel = viewModel.decibel,
            goal = viewModel.englishGoal
        )
        viewModel.checkData()

        // 서버에서 응답 받아서 뷰모델에 저장
        RetrofitClient.recommendationApi.sendRecommendations(request).enqueue(object : Callback<BaseResponse<RecommendationResponse>> {

            override fun onResponse(
                call: Call<BaseResponse<RecommendationResponse>>,
                response: Response<BaseResponse<RecommendationResponse>>
            ) {
                if (response.isSuccessful) {
                    val baseResponse = response.body()
                    val resultData = baseResponse?.data

                    if (resultData != null) {

                        // 메모리(싱글톤)에 저장
                        RecommendationManager.englishGoal = viewModel.englishGoal
                        RecommendationManager.englishPlace = viewModel.englishPlace
                        RecommendationManager.decibel = viewModel.decibel
                        RecommendationManager.place = viewModel.place
                        RecommendationManager.goal = viewModel.goal
                        RecommendationManager.cachedPlaylist = resultData
                        // 뷰모델 업데이트
                        viewModel.currentPlaylist.value = resultData

                        // 내부 저장소에 플리 이름 저장 -> result의 두 프래그먼트에서 동일한 이름 패치되도록
                        context?.let { ctx ->
                            RecommendationManager.savePlaylistName(ctx, resultData.playlistName)
                            RecommendationManager.savePlaylistId(ctx, resultData.playlistId.toString())  // 아이디 저장
                        }

                        updateUIForCompletion()

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}