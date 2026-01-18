package com.mobile.soundscape.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.mobile.soundscape.R
import com.mobile.soundscape.databinding.FragmentMypageBinding
import com.mobile.soundscape.onboarding.OnboardingViewModel


class MypageFragment : Fragment() {
    private var _binding: FragmentMypageBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OnboardingViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMypageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 온보딩에서 정한 데이터 불러오기
        viewModel.loadSavedData(requireContext())

        // 사용자 닉네임 불러오기
        viewModel.Nickname.observe(viewLifecycleOwner) { nickname ->
            binding.tvNameValue.text = nickname
        }

        // 아티스트 취향 불러오기
        viewModel.artistList.observe(viewLifecycleOwner) { artists ->
            // artists: List<LocalArtistModel>

            // 1. 첫 번째 아티스트
            val artist1 = artists.getOrNull(0)
            binding.tvArtist1.text = artist1?.name ?: ""
            if (artist1 != null && artist1.imageUrl.isNotEmpty()) {
                com.bumptech.glide.Glide.with(this)
                    .load(artist1.imageUrl)
                    .circleCrop() // 원형으로 자르기 (선택사항)
                    .into(binding.ivArtist1) // ★ xml에 ImageView(ivArtist1) 있어야 함
            }

            // 2. 두 번째 아티스트
            val artist2 = artists.getOrNull(1)
            binding.tvArtist2.text = artist2?.name ?: ""
            if (artist2 != null && artist2.imageUrl.isNotEmpty()) {
                com.bumptech.glide.Glide.with(this)
                    .load(artist2.imageUrl)
                    .circleCrop()
                    .into(binding.ivArtist2)
            }

            // 3. 세 번째 아티스트
            val artist3 = artists.getOrNull(2)
            binding.tvArtist3.text = artist3?.name ?: ""
            if (artist3 != null && artist3.imageUrl.isNotEmpty()) {
                com.bumptech.glide.Glide.with(this)
                    .load(artist3.imageUrl)
                    .circleCrop()
                    .into(binding.ivArtist3)
            }
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