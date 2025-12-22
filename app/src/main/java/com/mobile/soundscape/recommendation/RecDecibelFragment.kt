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
import android.util.Log
import com.mobile.soundscape.databinding.WidgetProgressBarBinding


// 버퍼 크기 계산 (AudioRecord를 초기화하는 데 필요)

class RecDecibelFragment : Fragment() {

    private var audioRecord: AudioRecord? = null
    private var isRecording = false // 녹음 상태 플래그
    private lateinit var recordingThread: Thread

    private var allRecordedValues = mutableListOf<Double>()
    private val bufferSize = AudioRecord.getMinBufferSize(
        SAMPLE_RATE,
        CHANNEL,
        ENCODING
    )
    companion object {
        private const val SAMPLE_RATE = 44100
        private const val CHANNEL = AudioFormat.CHANNEL_IN_MONO
        private const val ENCODING = AudioFormat.ENCODING_PCM_16BIT
    }
    private lateinit var binding: FragmentRecDecibelBinding

    private val REQUEST_RECORD_AUDIO_PERMISSION = 200

    private var currentDecibelValue: Double = 0.0 // 실시간 dB 값을 저장
    // 현재 데시벨 측정 상태를 저장할 변수
    private var currentState = DecibelState.INITIAL

    // (DecibelState Enum 클래스 정의는 별도로 필요)
    enum class DecibelState { INITIAL, RECORDING, FINISHED }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentRecDecibelBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 1. 초기 UI 설정 (0 dB)
        updateUI(DecibelState.INITIAL)

        // 2. 액션 버튼 클릭 로직
        binding.btnAction.setOnClickListener {
            when (currentState) {
                DecibelState.INITIAL -> startRecording() // 초기 -> 녹음 시작
                DecibelState.RECORDING -> stopRecording() // 녹음 중 -> 녹음 멈춤
                DecibelState.FINISHED -> restartRecording() // 완료 -> 다시 시작
            }
        }

        binding.nextBtn.setOnClickListener {
            findNavController().navigate(R.id.action_recDecibelFragment_to_recGoalFragment)
        }

        // TODO: 장소 선택 버튼들의 로직 (선택 시 Opacity 변경 등)은 여기에 구현
    }

    private fun updateUI(newState: DecibelState) {
        currentState = newState // 상태 업데이트

        when (newState) {
            DecibelState.INITIAL -> {
                binding.tvCaption.text = "지금 공간의 소리를 들어볼게요."
                binding.tvDecibelValue.text = "0 dB"
                binding.btnAction.setImageResource(R.drawable.decibel_play) // 시작 아이콘
                binding.ellipse.isVisible = false
                binding.nextBtn.isVisible = false
                binding.nextBtn.isEnabled = false
            }
            DecibelState.RECORDING -> {
                binding.tvCaption.text = "주변을 듣고 있어요."
                binding.btnAction.setImageResource(R.drawable.decibel_pause) // 멈춤 아이콘
                // TODO: (데시벨 측정 중에는 tv_decibel_value가 실시간으로 업데이트되어야 함)
                binding.ellipse.isVisible = false
                binding.nextBtn.isVisible = false
                binding.nextBtn.isEnabled = false

            }
            DecibelState.FINISHED -> {
                binding.tvCaption.text = "주변 소리를 들었어요."
                binding.tvDecibelValue.text = "${String.format("%.1f", currentDecibelValue)} dB"
                binding.btnAction.setImageResource(R.drawable.decibel_restart) // 다시 시작 아이콘
                binding.tvDecibelValue.isVisible = true
                binding.tvDecibelValue.requestLayout() // 레이아웃 재계산 요청
                binding.ellipse.isVisible = true
                binding.nextBtn.isVisible = true
                binding.nextBtn.isEnabled = true

            }
        }
    }
    private fun startRecording() {
        // 1. UI 상태 변경 (RECORDING)
        updateUI(DecibelState.RECORDING)

        // 2. 권한 확인 (사용자가 마이크 접근을 허용했는지)
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            // 권한이 없으면 사용자에게 요청
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO_PERMISSION)
            return
        }

        // 3. AudioRecord 초기화 및 시작
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            CHANNEL,
            ENCODING,
            bufferSize
        )

        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            // 초기화 실패 처리
            return
        }

        audioRecord?.startRecording()
        isRecording = true

        // 4. 측정 스레드 시작 (백그라운드에서 dB 계산)
        recordingThread = Thread { measureDecibel() }
        recordingThread.start()
    }

    private fun measureDecibel() {
        val audioBuffer = ShortArray(bufferSize)
        val OFFSET = 20.0 // 보정값 (환경에 맞춰 조정)
        val ALPHA = 0.1   // 낮을수록 부드럽게 변함 (0.1 ~ 0.2 추천)
        var smoothedDB = 0.0 // 실시간 감도를 위한 변수

        while (isRecording && audioRecord != null) {
            val readSize = audioRecord!!.read(audioBuffer, 0, bufferSize)
            if (readSize > 0) {
                var sumOfSquares = 0.0
                for (i in 0 until readSize) {
                    sumOfSquares += audioBuffer[i] * audioBuffer[i]
                }
                val rms = kotlin.math.sqrt(sumOfSquares / readSize)
                val dB = if (rms > 0) 20 * kotlin.math.log10(rms) else 0.0
                val calibratedDB = maxOf(0.0, dB + OFFSET)

                // 1. 실시간 시각화를 위한 부드러운 값 계산 (EWMA)
                if (smoothedDB == 0.0) smoothedDB = calibratedDB // 초기값 설정
                smoothedDB = ALPHA * calibratedDB + (1 - ALPHA) * smoothedDB

                // 2. 나중에 전체 평균을 내기 위해 모든 측정값 저장
                allRecordedValues.add(calibratedDB)

                // 3. UI 업데이트 (부드러운 값을 보여줌)
                activity?.runOnUiThread {
                    binding.tvDecibelValue.text = "${String.format("%.1f", smoothedDB)} dB"
                }
            }
        }
    }

    private fun stopRecording() {
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null

        if (allRecordedValues.isNotEmpty()) {
            // 녹음 기간 동안 저장된 모든 값의 평균을 계산
            currentDecibelValue = allRecordedValues.average()
        } else {
            currentDecibelValue = 0.0
        }

        // 최종 상태로 UI 업데이트 (이제 텍스트뷰에 전체 평균값이 고정됨)
        updateUI(DecibelState.FINISHED)
        Log.d("Decibel", "Final Average dB: $currentDecibelValue")

        val displayedText = binding.tvDecibelValue.text.toString()
        val matchResult = "([0-9.]+)\\s*dB".toRegex().find(displayedText)
        currentDecibelValue = matchResult?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
        // 최종 상태로 UI 업데이트
        updateUI(DecibelState.FINISHED)
        Log.d("Decibel", "Final Measured dB: $currentDecibelValue")
    }

    private fun restartRecording() {
        // 💡 1. UI를 초기 상태로 명시적으로 업데이트 (글자가 다시 보이는 것을 보장)
        updateUI(DecibelState.INITIAL)

        // 2. 잠시 후 녹음 시작 (optional: 지연 시간을 주어 상태 전환 보장)
        // 현재는 지연 없이 바로 startRecording()을 호출합니다.
        startRecording()
    }


    // RecDecibelFragment.kt 내부

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // 권한 허용 시 녹음 재시도
            startRecording()
        }
    }


}