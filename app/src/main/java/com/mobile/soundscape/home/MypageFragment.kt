package com.mobile.soundscape.home

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mobile.soundscape.R
import com.mobile.soundscape.api.client.RetrofitClient
import com.mobile.soundscape.api.dto.BaseResponse
import com.mobile.soundscape.api.dto.MypageGenreRequest
import com.mobile.soundscape.data.RecommendationManager
import com.mobile.soundscape.databinding.FragmentMypageBinding
import com.mobile.soundscape.onboarding.ArtistFragment
import com.mobile.soundscape.onboarding.GenreFragment
import com.mobile.soundscape.onboarding.OnboardingViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MypageFragment : Fragment() {
    private var _binding: FragmentMypageBinding? = null // 바인딩 이름 확인
    private val binding get() = _binding!!

    // Activity 범위의 ViewModel 공유
    private val viewModel: OnboardingViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMypageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /* --- 온보딩에서 정한 데이터 불러오기 --- */
        viewModel.loadSavedData(requireContext())

        // 사용자 닉네임 불러오기
        viewModel.Nickname.observe(viewLifecycleOwner) { nickname ->
            binding.tvNameValue.text = nickname
        }

        // 아티스트 취향 불러오기
        viewModel.artistList.observe(viewLifecycleOwner) { artists ->
            binding.tvArtist1.text = artists.getOrNull(0) ?: ""
            binding.tvArtist2.text = artists.getOrNull(1) ?: ""
            binding.tvArtist3.text = artists.getOrNull(2) ?: ""
        }

        // 장르 취향 불러오기
        viewModel.genreList.observe(viewLifecycleOwner) { genres ->
            binding.tvGenre1.text = genres.getOrNull(0) ?: ""
            binding.tvGenre2.text = genres.getOrNull(1) ?: ""
            binding.tvGenre3.text = genres.getOrNull(2) ?: ""
        }

        // 이름 변경하기
        binding.btnEditName.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("mode", "edit")

            findNavController().navigate(R.id.action_mypageFragment_to_setnameFragment2, bundle)
        }

        /* --- 취향 변경하기 --- */
        // 아티스트 취향 변경
        binding.btnEditArtist.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("mode", "edit")

            // navigate 함수의 '두 번째 파라미터'로 번들을 넘겨줘야 합니다!
            findNavController().navigate(R.id.action_mypageFragment_to_artistFragment2, bundle)
        }

        // 3. 장르 수정 버튼 클릭 (비슷하게 구현)
        binding.btnEditGenre.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("mode", "edit")

            findNavController().navigate(R.id.action_mypageFragment_to_genreFragment2, bundle)
        }
    }

}