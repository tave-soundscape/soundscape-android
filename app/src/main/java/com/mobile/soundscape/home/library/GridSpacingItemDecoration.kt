package com.mobile.soundscape.home.library

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class GridSpacingItemDecoration(
    private val spanCount: Int, // 열 개수 (2)
    private val spacing: Int,   // 간격 (픽셀 단위)
    private val includeEdge: Boolean // 테두리 포함 여부
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view) // 아이템 위치
        if (position == RecyclerView.NO_POSITION) return // 유효하지 않은 위치면 패스

        val column = position % spanCount // 현재 열 (0 또는 1)

        if (includeEdge) {
            // 테두리 포함 (양쪽 끝에도 간격이 생김)
            outRect.left = spacing - column * spacing / spanCount
            outRect.right = (column + 1) * spacing / spanCount

            if (position < spanCount) { // 첫 줄 상단 여백
                outRect.top = spacing
            }
            outRect.bottom = spacing // 하단 여백
        } else {
            // 테두리 미포함 (아이템 사이만 간격 생김, 양끝은 화면에 딱 붙음)
            outRect.left = column * spacing / spanCount
            outRect.right = spacing - (column + 1) * spacing / spanCount
            if (position >= spanCount) {
                outRect.top = spacing
            }
        }
    }
}