package com.mobile.soundscape.data.model.music

import com.google.gson.annotations.SerializedName

//data class PlayRequest(
//    @SerializedName("uris")
//    val uris: List<String> // ["spotify:track:xxxx"] 형태로 보낼 예정
//)


data class PlayRequest(
    // 앨범이나 플레이리스트 ID
    @SerializedName("context_uri")
    val contextUri: String? = null,

    // 노래 목록 (이걸 쓸 땐 context_uri랑 같이 못 씀)
    @SerializedName("uris")
    val uris: List<String>? = null,

    // ★★★ [추가] 어디서부터 시작할지 지정하는 옵션
    @SerializedName("offset")
    val offset: Offset? = null,

    @SerializedName("position_ms")
    val positionMs: Int = 0
)

// ★★★ [새로 추가] Offset 객체
data class Offset(
    // 1. 트랙의 순서로 지정 (0번 트랙, 1번 트랙...)
    @SerializedName("position")
    val position: Int? = null,

    // 2. 트랙의 ID로 지정 (가장 추천!)
    @SerializedName("uri")
    val uri: String? = null
)


// 기기 목록 전체 응답
data class DeviceResponse(
    @SerializedName("devices")
    val devices: List<Device>
)

// 기기 하나하나의 정보
data class Device(
    @SerializedName("id")
    val id: String,       // 기기 ID (이게 필요함!)

    @SerializedName("name")
    val name: String,     // 기기 이름 (예: Galaxy S24)

    @SerializedName("is_active")
    val isActive: Boolean // 지금 활성화되어 있는지 여부
)