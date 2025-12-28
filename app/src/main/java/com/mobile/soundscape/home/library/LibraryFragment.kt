package com.mobile.soundscape.home.library

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobile.soundscape.R
import com.mobile.soundscape.databinding.FragmentLibraryBinding
import com.mobile.soundscape.result.MusicDataProvider

class LibraryFragment : Fragment(R.layout.fragment_library) {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLibraryBinding.bind(view)

        // 1. 동료의 MusicDataProvider 더미 데이터 가져오기
        val dummySongs = MusicDataProvider.createDummyData()

        // 2. 라이브러리용 리스트 데이터 생성
        val libraryData = listOf(
            LibraryPlaylistModel("플레이리스트 01", dummySongs.take(5), "25.05.15"),
            LibraryPlaylistModel("운동할 때 듣는 곡", dummySongs.takeLast(3), "25.06.01")
        )

        // 3. 어댑터 설정
        val libraryAdapter = LibraryAdapter(libraryData) { selectedPlaylist ->
            // 1. 상세 페이지로 보낼 데이터를 Bundle에 담기
            val bundle = Bundle().apply {
                putString("title", selectedPlaylist.title)
                putString("date", selectedPlaylist.date)
                putSerializable("songs", ArrayList(selectedPlaylist.songs))
            }

            // 2. nav_graph에 정의한 action ID를 사용하여 이동
            // findNavController()를 쓰려면 상단에 import androidx.navigation.fragment.findNavController 가 필요합니다.
            findNavController().navigate(R.id.action_libraryFragment_to_libraryDetailFragment, bundle)
        }

        binding.rvPlaylist.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = libraryAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}