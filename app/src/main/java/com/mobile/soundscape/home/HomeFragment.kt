package com.mobile.soundscape.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.mobile.soundscape.R
import com.mobile.soundscape.databinding.FragmentHomeBinding

data class Song(
    val songName: String,   // 곡 이름
    val spotifyUrl: String  // Spotify 재생 URL
)

//  Playlist 데이터 클래스 최종 정의 (Song 리스트 포함)
data class Playlist(
    val id: Long,               // Long 타입 ID
    val playListName: String,   // 플레이리스트 이름 (UI 표시용)
    val userId: Long,           // 사용자 ID
    val location: String,       // 장소 (String, 내부적으로 Enum 권장)
    val purpose: String,        // 목적 (String, 내부적으로 Enum 권장)
    val iconName: String,       // 사용할 Drawable 파일 이름
    val songs: List<Song>,      // 플레이리스트 안의 곡들 (List<Song>)
    val spotifyUri: String      // Spotify 재생 요청 주소 (플레이리스트 전체 URI)
)

class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 2. View Binding으로 초기화 및 루트 뷰 반환
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root //  binding 객체의 루트 뷰 반환
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 홈화면 구슬 움직이는 animation
        com.bumptech.glide.Glide.with(this)
            .load(R.drawable.orb_animation)
            .into(binding.centerButton)

        // 1. 더미 데이터 준비 (Has Value 상태 테스트용)
        val dummySongs = listOf(
            Song("Peaceful Piano Track", "spotify:track:12345"),
            Song("Lo-Fi Study Beat", "spotify:track:67890"),
            Song("Ambient Focus Sound", "spotify:track:abcde")
        )
        // 이 데이터의 순서(index)가 btn1, btn2, btn3, ...순서와 매칭되어야 함
        val playlistData = listOf(
            Playlist(
                id = 101L,
                playListName = "책 읽기",
                userId = 999L,
                location = "도서관",
                purpose = "집중",
                iconName = "button1",
                songs = dummySongs,
                spotifyUri = "spotify:playlist:test_read"
            ),
            Playlist(
                id = 102L,
                playListName = "카페 공부",
                userId = 999L,
                location = "카페",
                purpose = "집중",
                iconName = "button2",
                songs = dummySongs,
                spotifyUri = "spotify:playlist:test_cafe"
            ),
            Playlist(
                id = 103L,
                playListName = "휴식용",
                userId = 999L,
                location = "집/실내",
                purpose = "휴식",
                iconName = "button3",
                songs = dummySongs,
                spotifyUri = "spotify:playlist:test_relax"
            )
        )

        val isDataEmpty = playlistData.isEmpty()
        val buttons = listOf(
            binding.btn1,
            binding.btn2,
            binding.btn3,
            binding.btn4,
            binding.btn5,
            binding.btn6
        )

        // 2. Placeholder 아이콘 ID
        val placeholderIconId = R.drawable.empty_button // 회색 원형 이미지 리소스 ID

        // 3. 중앙 버튼의 아이콘은 건드리지 않고, 주변 버튼들만 업데이트함
        buttons.forEachIndexed { index, button ->
            if (index < playlistData.size) {
                //  데이터가 있을 때 (Has Value 상태)
                val data = playlistData[index]

                // iconName (문자열)을 Drawable ID로 찾습니다.
                val iconId = resources.getIdentifier(
                    data.iconName, "drawable", requireContext().packageName
                )

                button.setImageResource(iconId)
                button.alpha = 1.0f
                button.isClickable = true
                // TODO: 클릭 리스너 설정

                button.setOnClickListener {
                    val clickedPlaylist = playlistData[index] // 클릭된 버튼에 해당하는 Playlist 데이터 획득

                    // 2. Navigation Component를 사용하여 데이터 전달 및 화면 이동
                    // HomeFragmentDirections 클래스는 Argument 사용 시 Gradle이 자동 생성
                    val action = HomeFragmentDirections.actionHomeFragmentToPlaylistDetailFragment(
                        playlistId = clickedPlaylist.id //  클릭된 플레이리스트의 ID 전달
                    )
                    findNavController().navigate(action)
                }

            } else {
                //  데이터가 없을 때 (Empty View 상태 또는 데이터 수 부족 시)

                // 8개 중 나머지 버튼은 Placeholder 이미지로 설정
                button.setImageResource(placeholderIconId)
                button.alpha = 0.5f
                button.isClickable = false
            }
        }

        binding.centerButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_recPlaceFragment)
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}