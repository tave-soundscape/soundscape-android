package com.mobile.soundscape

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.transition.TransitionManager
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.mobile.soundscape.databinding.ActivityMainBinding
import com.mobile.soundscape.evaluation.EvaluationPopupDialog


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        handleNavigationIntent(intent)

        // 2. NavController 가져오기
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // 3. 하단 바 버튼 클릭 리스너 설정
        setupBottomNav(navController)

        // 4. 화면이 바뀔 때마다 하단 바 UI 업데이트
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // 1. 하단 바를 보여줄 화면들 정의 (이 리스트에 없으면 숨김)
            val showNavDestinations = setOf(
                R.id.homeFragment,
                R.id.exploreFragment,
                R.id.libraryFragment,
                R.id.mypageFragment,
                R.id.homeHistoryFragment
            )

            // 2. 현재 화면이 리스트에 있는지 확인
            if (destination.id in showNavDestinations) {
                // 홈 네비게시연에 있는 프래그먼트면 보이기
                binding.bottomNavBar.navContainer.visibility = View.VISIBLE

                // 기존 UI 업데이트 로직 실행
                val selectedLayoutId = when (destination.id) {
                    R.id.homeFragment -> R.id.layout_nav_home
                    R.id.exploreFragment -> R.id.layout_nav_explore
                    R.id.libraryFragment -> R.id.layout_nav_library
                    R.id.mypageFragment -> R.id.layout_nav_mypage
                    else -> R.id.layout_nav_home
                }
                updateBottomNavUI(selectedLayoutId)

            } else {
                // 그 외 모든 화면(RecPlaceFragment, DetailFragment 등)에서는 숨김
                binding.bottomNavBar.navContainer.visibility = View.GONE
            }
        }
    }

    // MainActivity.kt 에 추가
    override fun onResume() {
        super.onResume()

        // 1. PreferenceManager를 통해 경험 여부 확인
        if (PreferenceManager.isPlaylistExperienced(this)) {

            // 2. 즉시 false로 변경하여 중복 방지
            PreferenceManager.setPlaylistExperienced(this, false)

            // 3. 화면이 완전히 뜬 후 팝업 노출 (2.5초 지연 대신 post 사용이 더 부드러움)
            binding.root.postDelayed({
                val existingDialog = supportFragmentManager.findFragmentByTag("EvaluationPopup")
                if (existingDialog == null && !isFinishing && !isDestroyed) {
                    val dialog = EvaluationPopupDialog()
                    dialog.show(supportFragmentManager, "EvaluationPopup")
                }
            }, 800) //
        }
    }


    // [추가] 이미 켜진 앱으로 다시 들어올 때 신호 처리
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // 새로운 인텐트로 교체
        handleNavigationIntent(intent) // 처리 함수 호출
    }

    private fun handleNavigationIntent(intent: Intent?) {
        if (intent?.getStringExtra("NAVIGATE_TO") == "LIBRARY") {
            // NavController 가져오기
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController

            navController.navigate(R.id.libraryFragment, null, getNavOptions())
        }
    }

    private fun setupBottomNav(navController: androidx.navigation.NavController) {
        binding.bottomNavBar.layoutNavHome.setOnClickListener {
            navController.navigate(R.id.homeFragment, null, getNavOptions())
        }

        binding.bottomNavBar.layoutNavExplore.setOnClickListener {
            navController.navigate(R.id.exploreFragment, null, getNavOptions())
        }

            // 라이브러리 눌렀을 때
        binding.bottomNavBar.layoutNavLibrary.setOnClickListener {
            navController.navigate(R.id.libraryFragment, null, getNavOptions())
        }
            // 마이페이지 눌렀을 때
        binding.bottomNavBar.layoutNavMypage.setOnClickListener {
            navController.navigate(R.id.mypageFragment, null, getNavOptions())
        }

    }

    private fun updateBottomNavUI(selectedLayoutId: Int) {
            // 애니메이션 시작
        TransitionManager.beginDelayedTransition(binding.bottomNavBar.navContainer)

        val navBinding = binding.bottomNavBar

            // 루프를 돌리기 위해 리스트로 묶음
        val items = listOf(
            Triple(navBinding.layoutNavHome, navBinding.ivNavHome, navBinding.tvNavHome),
            Triple(navBinding.layoutNavLibrary, navBinding.ivNavLibrary,navBinding.tvNavLibrary),
            Triple(navBinding.layoutNavExplore, navBinding.ivNavExplore, navBinding.tvNavExplore),
            Triple(navBinding.layoutNavMypage, navBinding.ivNavMypage, navBinding.tvNavMypage)
        )

            // dp 단위를 픽셀(px)로 변환
        val density = resources.displayMetrics.density
        val expandedWidthPx = (107 * density).toInt()
        val expandedHeightPx = (48 * density).toInt()

        items.forEach { (layout, icon, text) ->
            // 레이아웃 파라미터 가져오기 (너비와 가중치를 수정하기 위해)
            val params = layout.layoutParams as android.widget.LinearLayout.LayoutParams

            if (layout.id == selectedLayoutId) {
                // 너비를 107dp로 고정하고, 가중치(weight)를 없앰
                params.width = expandedWidthPx
                params.height = expandedHeightPx
                params.weight = 0f

                // 스타일 변경 (배경, 아이콘 색상, 텍스트 보이기)
                layout.setBackgroundResource(R.drawable.bg_nav_item_selected)
                icon.imageTintList = ColorStateList.valueOf(Color.BLACK)
                text.visibility = View.VISIBLE
            } else {
                // 선택되지 않은 버튼
                // 너비를 0으로 죽이고, 가중치(weight)를 1로 주어 남은 공간을 나눠먹게 함
                params.width = 0
                params.weight = 1f

                // 스타일 변경 (투명 배경, 회색 아이콘, 텍스트 숨기기)
                layout.setBackgroundColor(Color.TRANSPARENT)
                icon.imageTintList = ColorStateList.valueOf(Color.parseColor("#888888"))
                text.visibility = View.GONE
            }
            layout.layoutParams = params
        }
    }

    private fun getNavOptions(): androidx.navigation.NavOptions {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        return androidx.navigation.NavOptions.Builder()
            .setLaunchSingleTop(true) //
            .setPopUpTo(
                navController.graph.startDestinationId,
                false
            )
            .build()
    }
}
