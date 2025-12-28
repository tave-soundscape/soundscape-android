package com.mobile.soundscape.data

import com.mobile.soundscape.api.dto.RecommendationResponse

// 문제점: recommendation에서 사용하는 뷰모델을 패키지가 다른 ListFragment에서 사용할 수 없음
// 싱글톤 형식으로 앱 어디서든 접근 가능한 공용 창고

object RecommendationRepository {
    var place: String = ""
    var goal: String = ""
    var cachedPlaylist: RecommendationResponse? = null
}