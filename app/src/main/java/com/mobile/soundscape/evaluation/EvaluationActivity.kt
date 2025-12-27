package com.mobile.soundscape.evaluation

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mobile.soundscape.R
import com.mobile.soundscape.databinding.ActivityEvaluationBinding

class EvaluationActivity : AppCompatActivity() {

    // ViewBinding 설정
    private lateinit var binding: ActivityEvaluationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Binding 초기화
        binding = ActivityEvaluationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. 시스템바 간격 설정 (XML 최상위 ID가 fragment_container라고 가정)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 3. 앱 실행 시 첫 번째 프래그먼트(Step 1) 띄우기
        // savedInstanceState == null 일 때만 생성해야 화면 회전 시 중복 생성을 막음
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, EvaluationStep1Fragment())
                .commit()
        }
    }
}