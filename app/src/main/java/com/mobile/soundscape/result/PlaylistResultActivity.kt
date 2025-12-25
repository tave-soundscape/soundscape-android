package com.mobile.soundscape.result

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.mobile.soundscape.R
import com.mobile.soundscape.databinding.ActivityPlaylistResultBinding

class PlaylistResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlaylistResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityPlaylistResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 앱이 화면 회전 등으로 재생성될 때 프래그먼트가 겹치는 것 방지
        if (savedInstanceState == null) {
            val fragment = ListFragment()

            supportFragmentManager.beginTransaction()
                .replace(R.id.playlist_fragment_container, fragment)
                .commit() // ★ 첫 화면은 addToBackStack 하지 않음!
        }
    }
}