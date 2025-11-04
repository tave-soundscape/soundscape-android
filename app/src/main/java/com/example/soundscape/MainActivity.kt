package com.example.soundscape

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.IOException
import kotlin.math.log10

class MainActivity : AppCompatActivity() {
    private lateinit var decibelText: TextView
    private lateinit var measureButton: Button

    // 데시벨 측정 관련
    private var mediaRecorder: MediaRecorder? = null
    private var isMeasuring = false

    // 주기적인 업데이트를 위한 Handler
    private val handler = Handler(Looper.getMainLooper())
    private val updateIntervalMs: Long = 300 // 0.3초마다 업데이트


    private lateinit var tmpButton: Button


    // 권한 요청 런처
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // 권한이 승인되면 측정 시작
                startMeasuring()
            } else {
                // 권한이 거부되면 사용자에게 알림
                decibelText.text = "마이크 권한이 필요합니다."
            }
        }

    // 데시벨을 주기적으로 업데이트하는 작업
    private val updateDecibelTask = object : Runnable {
        override fun run() {
            if (isMeasuring) {
                val amplitude = mediaRecorder?.maxAmplitude ?: 0
                val db = amplitudeToDb(amplitude.toDouble())

                // UI 업데이트
                decibelText.text = String.format("%.2f dB", db)

                // 다음 업데이트 예약
                handler.postDelayed(this, updateIntervalMs)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        decibelText = findViewById(R.id.decibelText)
        measureButton = findViewById(R.id.measureButton)

        // 버튼 클릭 리스너 안에 권한 확인 로직을 넣음
        measureButton.setOnClickListener {
            if (isMeasuring) {
                // 측정이 진행 중이면 중지
                stopMeasuring()
            } else {
                // 측정이 중지 상태면 권한 확인 후 시작
                when (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)) {
                    PackageManager.PERMISSION_GRANTED -> {
                        // 권한이 이미 있으면 측정 시작
                        startMeasuring()
                    }
                    else -> {
                        // 권한이 없으면 요청
                        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            }
        }



    }

    private fun startMeasuring() {
        val outputFile = "${cacheDir.absolutePath}/temp_audio.3gp"

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(outputFile)

            try {
                prepare()
            } catch (e: IOException) {
                Log.e("MediaRecorder", "prepare() failed: ${e.message}")
                decibelText.text = "측정 준비 실패"
                // 실패 시 자원 해제
                mediaRecorder?.release()
                mediaRecorder = null
                return
            }

            // try-cathc로 Exception 잡기
            try {
                start()
                isMeasuring = true
                measureButton.text = "측정 중지"
                decibelText.text = "측정 중..."
                handler.post(updateDecibelTask)
            } catch (e: Exception) { // RuntimeException 또는 IllegalStateException
                Log.e("MediaRecorder", "start() failed: ${e.message}")
                decibelText.text = "측정 시작 실패 (Error: ${e.message})"
                // start가 실패했으므로 stop()을 부르지 않고 release()만 호출
                mediaRecorder?.release()
                mediaRecorder = null
                isMeasuring = false
            }
        }
    }

    private fun stopMeasuring() {
        if (!isMeasuring && mediaRecorder == null) {
            // 이미 중지되었거나 시작되지 않았으면 아무것도 안 함
            return
        }

        // 주기적 업데이트 중단
        handler.removeCallbacks(updateDecibelTask)

        mediaRecorder?.apply {
            try {
                // stop()과 release()는 순서가 중요하며, 각각 예외가 발생할 수 있음
                stop()
            } catch (e: IllegalStateException) {
                Log.e("MediaRecorder", "stop() failed: ${e.message}")
            }
            try {
                release()
            } catch (e: IllegalStateException) {
                Log.e("MediaRecorder", "release() failed: ${e.message}")
            }
        }
        mediaRecorder = null
        isMeasuring = false
        measureButton.text = "데시벨 측정"


        if (decibelText.text.toString().contains("측정")) {
            decibelText.text = "측정이 중지되었습니다."
        }
    }

    // 진폭(Amplitude)을 데시벨(dB)로 변환하는 함수
    private fun amplitudeToDb(amplitude: Double): Double {
        if (amplitude > 0) {
            // 20 * log10(amplitude)
            return 20.0 * log10(amplitude)
        }
        return 0.0 // 소리가 없으면 0 dB
    }

    override fun onStop() {
        super.onStop()
        // 앱이 백그라운드로 가면 측정을 중지 (배터리 절약 및 마이크 독점 방지)
        stopMeasuring()
    }
}

