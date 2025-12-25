package com.mobile.soundscape.playlist

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mobile.soundscape.R
import com.mobile.soundscape.databinding.FragmentGalleryBinding
import com.mobile.soundscape.result.GalleryAdapter
import com.mobile.soundscape.result.ListFragment
import com.mobile.soundscape.result.MusicDataProvider
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlin.math.abs

class GalleryFragment : Fragment() {

    // 1. 바인딩 변수 설정
    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // ★★★ [수정됨] 바인딩 초기화 (이게 빠져서 죽었던 겁니다!)
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val musicList = MusicDataProvider.createDummyData()

        // 2. [수정됨] findViewById 대신 binding 사용
        // (XML ID: vpGallery, ivBlurBackground, tvCurrentTitle, tvCurrentArtist)
        val viewPager = binding.vpGallery

        // 어댑터 연결
        val adapter = GalleryAdapter(musicList)
        viewPager.adapter = adapter

        // 시작 위치 계산
        val centerPosition = Int.MAX_VALUE / 2
        val startPosition = centerPosition - (centerPosition % musicList.size)
        viewPager.setCurrentItem(startPosition, false)

        // ViewPager2 설정
        viewPager.offscreenPageLimit = 3
        viewPager.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER

        // 변환 효과 (Transformer)
        val compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.addTransformer(MarginPageTransformer(20))
        compositePageTransformer.addTransformer { page, position ->
            val r = 1 - abs(position)
            val scaleFactor = 0.50f + r * 0.50f
            page.scaleY = scaleFactor
            page.scaleX = scaleFactor
            val myOffset = (page.width - (page.width * scaleFactor)) / 3

            if (position < 0) {
                page.translationX = myOffset
            } else {
                page.translationX = -myOffset
            }
        }
        viewPager.setPageTransformer(compositePageTransformer)

        // UI 업데이트 함수
        fun updateUI(position: Int) {
            val realPosition = position % musicList.size
            val currentMusic = musicList[realPosition]

            binding.tvCurrentTitle.text = currentMusic.title
            binding.tvCurrentArtist.text = currentMusic.artist

            // 배경 블러 처리 (Context 안전하게 사용)
            context?.let { ctx ->
                Glide.with(ctx)
                    .load(currentMusic.albumCover)
                    .apply(RequestOptions.bitmapTransform(BlurTransformation(25, 3)))
                    .into(binding.ivBlurBackground)
            }
        }

        // 초기 실행
        viewPager.post {
            updateUI(startPosition)
        }

        // 페이지 변경 감지
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateUI(position)
            }
        })

        // 3. [수정됨] 리스트 모드로 변경 버튼 (binding 사용)
        binding.btnGalleryToList.setOnClickListener {
            if (parentFragmentManager.backStackEntryCount > 0) {
                parentFragmentManager.popBackStack()
            } else {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.playlist_fragment_container, ListFragment())
                    .commit()
            }
        }

        // 4. [수정됨] 라이브러리 추가 버튼
        // ★ 중요: XML에 android:id="@+id/btnAddLibrary"를 꼭 추가해야 합니다!
        binding.addLibrary.setOnClickListener {
            val currentName = binding.tvPlaylistTitle.text.toString()
            showAddLibraryBottomSheet(currentName)
        }
    }

    private fun showAddLibraryBottomSheet(currentName: String) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = requireActivity().layoutInflater.inflate(R.layout.bottom_sheet_edit_name, null)
        bottomSheetDialog.setContentView(view)

        val etName = view.findViewById<android.widget.EditText>(R.id.etPlaylistName)
        val btnConfirm = view.findViewById<View>(R.id.btnConfirmEdit)
        val btnClose = view.findViewById<View>(R.id.btnClose)

        etName.setText(currentName)
        etName.setSelection(currentName.length)

        btnClose.setOnClickListener { bottomSheetDialog.dismiss() }

        btnConfirm.isEnabled = false
        btnConfirm.alpha = 0.4f

        etName.doAfterTextChanged { text ->
            val input = text.toString().trim()
            if (input.isNotEmpty()) {
                btnConfirm.isEnabled = true
                btnConfirm.alpha = 1.0f
            } else {
                btnConfirm.isEnabled = false
                btnConfirm.alpha = 0.4f
            }
        }

        btnConfirm.setOnClickListener {
            val newName = etName.text.toString().trim()
            // [수정됨] binding 사용
            binding.tvPlaylistTitle.text = newName
            showCustomToast("이름이 수정되었습니다.")
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    fun showCustomToast(message: String) {
        val inflater = LayoutInflater.from(requireContext())
        val layout = inflater.inflate(R.layout.toast_custom, null)
        val textView = layout.findViewById<TextView>(R.id.tv_toast_message)
        textView.text = message

        val toast = Toast(requireContext())
        toast.duration = Toast.LENGTH_SHORT
        toast.view = layout
        toast.setGravity(Gravity.BOTTOM, 0, 300)
        toast.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 메모리 누수 방지
    }
}