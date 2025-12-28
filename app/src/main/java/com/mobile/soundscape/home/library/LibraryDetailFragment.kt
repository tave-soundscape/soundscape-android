package com.mobile.soundscape.home.library

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobile.soundscape.R
import com.mobile.soundscape.databinding.FragmentLibraryDetailBinding // 수정된 레이아웃 바인딩
import com.mobile.soundscape.result.MusicModel

// 1. 여기서 우리가 새로 만든 레이아웃(fragment_library_detail)을 연결합니다.
class LibraryDetailFragment : Fragment(R.layout.fragment_library_detail) {

    private var _binding: FragmentLibraryDetailBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLibraryDetailBinding.bind(view)

        // 2. Bundle에서 전달받은 데이터 꺼내기 (title, date, songs)
        val title = arguments?.getString("title") ?: "알 수 없는 플레이리스트"
        val date = arguments?.getString("date") ?: ""
        val songs = arguments?.getSerializable("songs") as? ArrayList<MusicModel> ?: arrayListOf()

        // 3. UI 데이터 연결
        binding.tvDetailPlaylistName.text = title
        binding.tvDetailDescription.text = "${date}에 생성된 플레이리스트"
        binding.tvDetailCount.text = "곡 ${songs.size}개"

        // 4. 투명 배경 어댑터 연결 (LibraryDetailAdapter 사용)
        val detailAdapter = LibraryDetailAdapter(songs)
        binding.rvSongList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = detailAdapter
        }

        // 5. 뒤로가기 버튼
        binding.btnBack.setOnClickListener {
            // 이전 화면(LibraryFragment)으로 돌아가기
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        // 6. 생성일(date)도 함께 넘겨주도록 수정
        fun newInstance(title: String, date: String, songs: List<MusicModel>): LibraryDetailFragment {
            val fragment = LibraryDetailFragment()
            val args = Bundle()
            args.putString("title", title)
            args.putString("date", date) // 날짜 추가
            args.putSerializable("songs", ArrayList(songs))
            fragment.arguments = args
            return fragment
        }
    }
}