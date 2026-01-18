package com.mobile.soundscape.result

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import com.mobile.soundscape.MainActivity
import com.mobile.soundscape.PreferenceManager
import com.mobile.soundscape.R
import com.mobile.soundscape.databinding.ActivityPlaylistResultBinding

class PlaylistResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlaylistResultBinding

    // 뒤로가기 누른 시간을 저장할 변수
    private var backPressedTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityPlaylistResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupOnBackPressed()

        // 앱이 화면 회전 등으로 재생성될 때 프래그먼트가 겹치는 것 방지
        if (savedInstanceState == null) {
            val fragment = ListFragment()

            supportFragmentManager.beginTransaction()
                .replace(R.id.playlist_fragment_container, fragment)
                .commit()
        }
    }

    private fun setupOnBackPressed() {
        // 시스템의 뒤로가기 동작을 가로챔
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 만약 프래그먼트 백스택이 있다면 (갤러리 화면 -> 리스트 화면)
                // 경고 없이 그냥 이전 화면으로 돌아감
                if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack()
                    return
                }

                // 백스택이 비어있다면 (리스트 화면일 때)
                // 2초(2000ms) 안에 두 번 눌렀는지 확인
                if (System.currentTimeMillis() - backPressedTime < 2000) {
                    // [종료 처리] 액티비티를 끄고 홈(이전 액티비티)으로 돌아감
                    val intent = Intent(this@PlaylistResultActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // [경고 처리] 첫 번째 눌림 -> 시간 갱신 및 토스트 띄우기
                    backPressedTime = System.currentTimeMillis()
                    showCustomToast("이 페이지를 나가면 현재 생성된 플레이리스트를 다시 복구할 수 없어요.\n플레이리스트를 또 보고 싶으면 라이브러리에 저장을 눌러주세요.")
                }
            }
        })
    }

    fun showCustomToast(message: String, iconResId: Int? = null) {
        val layout = layoutInflater.inflate(R.layout.toast_custom, null)

        val textView = layout.findViewById<TextView>(R.id.tv_toast_message)
        textView.text = message

        val iconView = layout.findViewById<ImageView>(R.id.iv_toast_icon)
        iconView?.visibility = View.GONE // 아이콘이 있으면 GONE 처리


        val toast = Toast(this)
        toast.duration = Toast.LENGTH_SHORT
        toast.view = layout // 내가 만든 레이아웃을 끼워넣음

        // 위치 조정 (선택사항: 화면 중앙 하단 등)
        toast.setGravity(Gravity.BOTTOM, 0, 700)

        toast.show()
    }
}