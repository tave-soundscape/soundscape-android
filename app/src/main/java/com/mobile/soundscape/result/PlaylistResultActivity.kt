package com.mobile.soundscape.result

import android.app.Activity
import android.os.Bundle
import android.util.Log.v
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mobile.soundscape.R
import kotlin.collections.isNotEmpty
import com.mobile.soundscape.databinding.ActivityPlaylistResultBinding
import android.widget.EditText
import android.widget.TextView
import com.mobile.soundscape.MainActivity
import androidx.core.widget.doAfterTextChanged 
import android.graphics.Color
import android.view.Gravity


class PlaylistResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlaylistResultBinding
    private lateinit var adapter: PlaylistResultAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaylistResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. 더미 데이터 생성
        val dummyList = MusicDataProvider.createDummyData()

        // 2. 리사이클러뷰 설정
        setupRecyclerView(dummyList)

        // 3. 상단 헤더 이미지 설정 (자동으로 dummyList의 앞 4개를 가져옴)
        setupHeaderImages(dummyList)

        setupButtons()

    }

    private fun setupButtons() {
        // 기존 플리 이름
        val currentName = binding.tvPlaylistName.text.toString()

        // 플리 다시 만들기
        binding.regenerateBtn.setOnClickListener {
            showRegenerateBottomSheet()
        }

        // addLibrary 버튼 누르면 -> 저장하겠다는 바텀시트 
        binding.addLibrary.setOnClickListener {
            showAddLibraryBottomSheet(currentName)
        }

        binding.tvPlaylistName.setOnClickListener {
            showAddLibraryBottomSheet(currentName)
        }
    }

    private fun setupRecyclerView(songList: List<MusicModel>) {
        adapter = PlaylistResultAdapter(songList) {
            // 푸터 클릭 시 바텀시트
            showRegenerateBottomSheet()
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@PlaylistResultActivity)
            this.adapter = this@PlaylistResultActivity.adapter
        }
    }

    private fun setupHeaderImages(songList: List<MusicModel>) {
        // XML에 있는 4개의 이미지뷰 ID (ivCover1 ~ ivCover4)
        val headerImageViews = listOf(
            binding.ivCover1,
            binding.ivCover2,
            binding.ivCover3,
            binding.ivCover4
        )

        // 리스트에서 앞에서부터 순서대로 이미지를 꺼내 헤더에 넣음
        for (i in headerImageViews.indices) {
            if (i < songList.size) {
                // 각 ImageView에 URL 로드
                loadUrlToImageView(headerImageViews[i], songList[i].albumCover)
            }
        }

        // 배경 그라데이션 이미지 (첫 번째 곡의 커버를 배경으로 사용)
        if (songList.isNotEmpty()) {
            loadUrlToImageView(binding.ivBackgroundGradient, songList[0].albumCover)
        }
    }

    // --- 이미지 로드 헬퍼 함수 ---
    private fun loadUrlToImageView(imageView: ImageView, url: String) {
        if (url.isNotEmpty()) {
            Glide.with(this)
                .load(url)
                .transform(CenterCrop()) // 꽉 채우기
                .into(imageView)
        } else {
            // URL이 비어있을 때 보여줄 기본 색상
            imageView.setImageResource(R.color.black)
        }
    }

    private fun showRegenerateBottomSheet() {
        // 1. 다이얼로그 생성
        val bottomSheetDialog = BottomSheetDialog(this)

        // 2. 레이아웃(XML) 가져오기
        val view = layoutInflater.inflate(R.layout.bottom_sheet_result, null)
        bottomSheetDialog.setContentView(view)

        // --- [닫기 기능 구현] ---

        // 3. 뷰에서 닫기 버튼 찾기 (ID: closeBtn)
        val closeBtn = view.findViewById<View>(R.id.btnClose)

        // 4. 클릭 리스너 연결 -> 다이얼로그 닫기(.dismiss())
        closeBtn.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        // (참고) 다시하기 버튼에도 기능 연결 가능
        val confirmBtn = view.findViewById<View>(R.id.btnRegenerateConfirm)
        confirmBtn?.setOnClickListener {
            // 플리 다시 생성하는 코드
            // 일단 베타 버전에서는 불가능 -> 토스트로 알리기
            //showCustomToast("죄송합니다.베타버전에서는\n플레이리스트 새로 생성이 어렵습니다.")
            Toast.makeText(this, "죄송합니다.베타버전에서는\n지원하지 않는 기능입니다.",Toast.LENGTH_SHORT).show()
            bottomSheetDialog.dismiss() // 작업 후 닫기
        }

        // 5. 다이얼로그 보여주기
        bottomSheetDialog.show()
    }



    // 플리 이름 수정 코드
    private fun showAddLibraryBottomSheet(currentName: String) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_edit_name, null)
        bottomSheetDialog.setContentView(view)

        // 뷰 찾기
        val etName = view.findViewById<EditText>(R.id.etPlaylistName)
        val btnConfirm = view.findViewById<View>(R.id.btnConfirmEdit)
        val btnClose = view.findViewById<View>(R.id.btnClose)

        // 현재 이름 입력창에 넣어두기
        etName.setText(currentName)
        // 커서를 글자 맨 뒤로 이동
        etName.setSelection(currentName.length)

        // 닫기(X) 버튼
        btnClose.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        // 아무 입력도 없으면 "추가하기" 버튼 색깔 블러처리
        btnConfirm.isEnabled = false
        btnConfirm.alpha = 0.4f // 투명도를 낮쳐서 흐릿하게(블러 느낌) 만듦

        // 텍스트 변경 감지 리스너 추가 -> 어떤 입력이라도 들어오면
        etName.doAfterTextChanged { text ->
            val input = text.toString().trim()

            if (input.isNotEmpty()) {
                // 입력이 있을 때 -> 선명하게 & 클릭 가능
                btnConfirm.isEnabled = true
                btnConfirm.alpha = 1.0f // 투명도 원상복구
                // 배경색을 바꿨었다면 다시 원래 색으로: 
                // btnConfirm.setBackgroundColor(Color.BLACK) 
            } else {  // 입력이 없을 때 -> 흐리게 & 클릭 불가
                btnConfirm.isEnabled = false
                btnConfirm.alpha = 0.4f
            }
        }

        // 수정하기 버튼 눌렀을 때 이벤트 처리
        btnConfirm.setOnClickListener {
            // 위에서 이미 isEnabled로 막아뒀기 때문에, 여기서는 굳이 빈 값 체크를 안 해도 되지만 안전을 위해 유지
            val newName = etName.text.toString().trim()
            // 액티비티 화면의 제목을 변경
            binding.tvPlaylistName.text = newName
            // 안내 메시지 및 닫기
            //Toast.makeText(this, "이름이 수정되었습니다.", Toast.LENGTH_SHORT).show()
            showCustomToast("이름이 수정되었습니다.")
            bottomSheetDialog.dismiss()

        }

        bottomSheetDialog.show()
    }



    // --- 커스텀 토스트 사용하기 위한 함수 ---
    fun showCustomToast(message: String, iconResId: Int? = null) {
        // 1. 커스텀 레이아웃 불러오기
        val inflater = LayoutInflater.from(this)
        val layout = inflater.inflate(R.layout.toast_custom, null)

        // 2. 텍스트 설정
        val textView = layout.findViewById<TextView>(R.id.tv_toast_message)
        textView.text = message

        // 4. 토스트 생성 및 설정
        val toast = Toast(applicationContext)
        toast.duration = Toast.LENGTH_SHORT
        toast.view = layout // 내가 만든 레이아웃을 끼워넣음

        // 위치 조정 (선택사항: 화면 중앙 하단 등)
        toast.setGravity(Gravity.BOTTOM, 0, 100)

        toast.show()
    }

}
