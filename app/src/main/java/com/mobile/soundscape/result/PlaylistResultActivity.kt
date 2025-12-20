package com.mobile.soundscape.result

import android.os.Bundle
import android.util.Log.v
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


class PlaylistResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlaylistResultBinding
    private lateinit var adapter: PlaylistResultAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaylistResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. 데이터 생성 (여기에 사진 URL을 직접 넣으세요)
        val dummyList = MusicDataProvider.createDummyData()

        // 2. 리사이클러뷰 설정
        setupRecyclerView(dummyList)

        // 3. 상단 헤더 이미지 설정 (자동으로 dummyList의 앞 4개를 가져옴)
        setupHeaderImages(dummyList)

        setupButtons()

    }

    private fun setupButtons() {
        // 현재 설정된 이름을 가져와서 넘겨줍니다.
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
            // 여기에 다시 생성하는 로직 작성
            Toast.makeText(this, "다시 생성합니다!", Toast.LENGTH_SHORT).show()
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

        // 1. 뷰 찾기
        val etName = view.findViewById<EditText>(R.id.etPlaylistName)
        val btnConfirm = view.findViewById<View>(R.id.btnConfirmEdit)
        val btnClose = view.findViewById<View>(R.id.btnClose)

        // 2. 현재 이름 입력창에 넣어두기
        etName.setText(currentName)
        // 커서를 글자 맨 뒤로 이동
        etName.setSelection(currentName.length)

        // 3. 닫기(X) 버튼
        btnClose.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        // 4. 수정하기 버튼
        btnConfirm.setOnClickListener {
            val newName = etName.text.toString().trim()

            if (newName.isNotEmpty()) {
                // (1) 액티비티 화면의 제목을 변경
                binding.tvPlaylistName.text = newName

                // (2) 안내 메시지 및 닫기
                Toast.makeText(this, "이름이 수정되었습니다.", Toast.LENGTH_SHORT).show()
                bottomSheetDialog.dismiss()
            } else {
                Toast.makeText(this, "이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
        bottomSheetDialog.show()
    }

}
