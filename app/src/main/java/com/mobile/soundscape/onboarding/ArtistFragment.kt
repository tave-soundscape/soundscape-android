package com.mobile.soundscape.onboarding

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobile.soundscape.R
import com.mobile.soundscape.databinding.FragmentArtistBinding


class ArtistFragment : Fragment() {

    private lateinit var binding: FragmentArtistBinding

    private lateinit var recyclerView: RecyclerView
    private lateinit var nextButton: AppCompatButton
    private lateinit var nextEllipse: AppCompatImageView
    private lateinit var initButton: AppCompatButton
    private lateinit var searchEditText: EditText // 검색창 변수 추가

    private lateinit var adapter: ArtistAdapter

    private val dummyArtistList = mutableListOf<ArtistData>(
        ArtistData("NewJeans", ""), ArtistData("IVE", ""), ArtistData("aespa", ""),
        ArtistData("LE SSERAFIM", ""), ArtistData("IU", ""), ArtistData("BTS", ""),
        ArtistData("BLACKPINK", ""), ArtistData("NCT", ""), ArtistData("SEVENTEEN", ""),
        ArtistData("EXO", ""), ArtistData("TWICE", ""), ArtistData("Red Velvet", "")
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_artist, container, false)

        recyclerView = view.findViewById(R.id.rv_artist_list)
        nextButton = view.findViewById(R.id.nextButton)
        nextEllipse = view.findViewById(R.id.nextEllipse)
        initButton = view.findViewById(R.id.initButton)
        searchEditText = view.findViewById(R.id.search_artist)

        updateButtonVisibility(0)
        setupRecyclerView()
        setupButtons()

        // 검색 기능 실행
        setupSearchListener()

        return view
    }

    private fun setupRecyclerView() {
        adapter = ArtistAdapter(dummyArtistList) {
            // 람다: 아이템이 클릭될 때마다 실행됨

            // 화면에 보이는 리스트가 아니라 dummyArtistList 전체에서 몇 개가 선택됐는지 갯수 세기
            val totalSelectedCount = dummyArtistList.count { it.isSelected }

            // 숫자 버튼 상태를 업데이트
            updateButtonVisibility(totalSelectedCount)
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
    }

    // --- 검색 로직 ---
    private fun setupSearchListener() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            // 텍스트가 바뀔 때마다 실행됨
            override fun afterTextChanged(s: Editable?) {
                val searchText = s.toString().trim() // 입력된 텍스트 (공백 제거)
                filterList(searchText)
            }
        })
    }

    // 실제 필터링 함수
    private fun filterList(query: String) {
        if (query.isEmpty()) {
            // 1. 검색어가 없으면 -> 원본 리스트를 보여줌
            adapter.updateList(dummyArtistList)
        } else {
            // 2. 검색어가 있으면 -> 이름에 검색어가 포함된 애들만 거름 (대소문자 무시)
            val filteredList = dummyArtistList.filter { artist ->
                artist.name.contains(query, ignoreCase = true)
            }
            adapter.updateList(filteredList)

        }
    }

    private fun setupButtons() {
        nextButton.setOnClickListener {
            // 다음 프래그먼트 이동 코드
            nextButton.setOnClickListener {
                val nextFragment = GenreFragment() // 이동할 프래그먼트
                parentFragmentManager.beginTransaction()
                    .replace(R.id.onboarding_fragment_container, nextFragment) // R.id.fragment_container는 메인 액티비티의 컨테이너 ID여야 함
                    .addToBackStack(null)
                    .commit()
            }
        }

        initButton.setOnClickListener {
            adapter.clearSelection()
            // 초기화 시 선택된 개수 0개로 버튼 숨김 처리
            updateButtonVisibility(0)
        }
    }



    private fun updateButtonVisibility(count: Int) {
        // 기존 코드 유지...
        if (count == 3) {
            if (nextButton.visibility != View.VISIBLE) {
                nextButton.visibility = View.VISIBLE
                nextEllipse.visibility = View.VISIBLE
                initButton.visibility = View.VISIBLE

                nextButton.alpha = 0f; nextButton.animate().alpha(1f).duration = 300
                nextEllipse.alpha = 0f; nextEllipse.animate().alpha(1f).duration = 300
                initButton.alpha = 0f; initButton.animate().alpha(1f).duration = 300
            }
        } else {
            nextButton.visibility = View.GONE
            nextEllipse.visibility = View.GONE
            initButton.visibility = View.GONE
        }
    }
}

