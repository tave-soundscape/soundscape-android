package com.mobile.soundscape.api.dto

import com.google.gson.annotations.SerializedName

// 공통 응답 양식
data class BaseResponse<T>(
    // 요청 성공 여부
    @SerializedName("result")
    val result: String?,

    // 에러코드 - 성공일 땐 없을 수 있으니 Nullable (?)
    @SerializedName("errorCode")
    val errorCode: String?, // 성공일 땐 없을 수 있으니 Nullable (?)

    // 메시지 (성공일 때 - "성공 메시지"  | 실패일 때 - "실패 메시지")
    @SerializedName("message")
    val message: String,

    // 응답 데이터 (실패일 땐 null | 성공일 땐 내용물)
    @SerializedName("data")
    val data: T? // 성공일 땐 내용물이 바뀌므로 Generic T + Nullable
)