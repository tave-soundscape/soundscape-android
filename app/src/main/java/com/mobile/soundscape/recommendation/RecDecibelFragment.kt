package com.mobile.soundscape.recommendation

import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.mobile.soundscape.R
import com.mobile.soundscape.databinding.FragmentRecDecibelBinding
import android.Manifest
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.activityViewModels

class RecDecibelFragment : Fragment() {

    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private lateinit var recordingThread: Thread
    private val viewModel: RecommendationViewModel by activityViewModels()

    private var allRecordedValues = mutableListOf<Double>()
    private val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL, ENCODING)

    private val autoStopHandler = Handler(Looper.getMainLooper())
    private val autoStopRunnable = Runnable {
        if (isRecording) {
            stopRecording()
        }
    }

    // [신규] 2초 뒤 자동 시작을 위한 Runnable
    private val autoStartRunnable = Runnable {
        startRecording()
    }

    companion object {
        private const val SAMPLE_RATE = 44100
        private const val CHANNEL = AudioFormat.CHANNEL_IN_MONO
        private const val ENCODING = AudioFormat.ENCODING_PCM_16BIT
        private const val RECORDING_DURATION = 5000L // 5초 동안 측정
        private const val AUTO_START_DELAY = 1500L   // 2초 뒤 시작
    }

    private lateinit var binding: FragmentRecDecibelBinding
    private val REQUEST_RECORD_AUDIO_PERMISSION = 200
    private var currentDecibelValue: Double = 0.0
    private var currentState = DecibelState.INITIAL

    enum class DecibelState { INITIAL, RECORDING, FINISHED }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecDecibelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateUI(DecibelState.INITIAL)

        // [변경] 버튼 클릭 리스너는 유지하되, 2초 뒤 자동 시작 예약
        binding.btnAction.setOnClickListener {
            when (currentState) {
                DecibelState.INITIAL -> {
                    autoStopHandler.removeCallbacks(autoStartRunnable) // 수동 클릭 시 자동 예약 취소
                    startRecording()
                }
                DecibelState.RECORDING -> stopRecording()
                DecibelState.FINISHED -> restartRecording()
            }
        }

        binding.nextBtn.setOnClickListener {
            findNavController().navigate(R.id.action_recDecibelFragment_to_recGoalFragment)
        }

        // [핵심] 화면 진입 2초 뒤 자동으로 측정 시작
        autoStopHandler.postDelayed(autoStartRunnable, AUTO_START_DELAY)
    }

    private fun updateUI(newState: DecibelState) {
        currentState = newState
        when (newState) {
            DecibelState.INITIAL -> {
                binding.tvCaption.text = "5초 동안 공간의 소리를 들어볼게요"
                binding.tvDecibelValue.text = "0 dB"
                binding.btnAction.setImageResource(R.drawable.decibel_play)
                binding.ellipse.isVisible = false
                binding.nextBtn.isVisible = false
            }
            DecibelState.RECORDING -> {
                binding.tvCaption.text = "주변을 듣고 있어요"
                binding.btnAction.setImageResource(R.drawable.decibel_pause)
                binding.ellipse.isVisible = false
                binding.nextBtn.isVisible = false
            }
            DecibelState.FINISHED -> {
                binding.tvCaption.text = "주변 소리를 들었어요"
                binding.tvDecibelValue.text = "${String.format("%.1f", currentDecibelValue)} dB"
                binding.btnAction.setImageResource(R.drawable.decibel_restart)
                binding.ellipse.isVisible = true
                binding.nextBtn.isVisible = true
                binding.nextBtn.isEnabled = true
            }
        }
    }

    private fun startRecording() {
        allRecordedValues.clear()
        updateUI(DecibelState.RECORDING)

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO_PERMISSION)
            return
        }

        audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL, ENCODING, bufferSize)
        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) return

        audioRecord?.startRecording()
        isRecording = true
        recordingThread = Thread { measureDecibel() }
        recordingThread.start()

        autoStopHandler.postDelayed(autoStopRunnable, RECORDING_DURATION)
    }

    private fun measureDecibel() {
        val audioBuffer = ShortArray(bufferSize)

        // [수정] 보정값 조정: 도서관에서 40~50이 나오도록 하려면 OFFSET을 0이나 마이너스로 조정
        // 기기마다 마이크 감도가 다르므로 테스트 후 -5.0 ~ 5.0 사이에서 조절해보세요.
        val OFFSET = 0.0
        val ALPHA = 0.1
        var smoothedDB = 0.0

        while (isRecording && audioRecord != null) {
            val readSize = audioRecord!!.read(audioBuffer, 0, bufferSize)
            if (readSize > 0) {
                var sumOfSquares = 0.0
                for (i in 0 until readSize) {
                    sumOfSquares += audioBuffer[i] * audioBuffer[i]
                }
                val rms = kotlin.math.sqrt(sumOfSquares / readSize)

                // RMS 기반 데시벨 계산 (일반적으로 RMS 1일 때 0dB 기준)
                val dB = if (rms > 0) 20 * kotlin.math.log10(rms) else 0.0

                // [보정] 너무 높게 측정되면 OFFSET을 더 낮추세요.
                val calibratedDB = maxOf(0.0, dB + OFFSET)

                if (smoothedDB == 0.0) smoothedDB = calibratedDB
                smoothedDB = ALPHA * calibratedDB + (1 - ALPHA) * smoothedDB

                allRecordedValues.add(calibratedDB)

                activity?.runOnUiThread {
                    binding.tvDecibelValue.text = "${String.format("%.1f", smoothedDB)} dB"
                }
            }
        }
    }

    private fun stopRecording() {
        autoStopHandler.removeCallbacks(autoStopRunnable)
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null

        if (allRecordedValues.isNotEmpty()) {
            currentDecibelValue = allRecordedValues.average()
        }

        updateUI(DecibelState.FINISHED)
        viewModel.decibel = currentDecibelValue.toFloat()
        viewModel.checkData()
    }

    private fun restartRecording() {
        updateUI(DecibelState.INITIAL)
        startRecording()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // [중요] 모든 예약된 실행 취소 (메모리 누수 방지)
        autoStopHandler.removeCallbacks(autoStopRunnable)
        autoStopHandler.removeCallbacks(autoStartRunnable)
    }
}