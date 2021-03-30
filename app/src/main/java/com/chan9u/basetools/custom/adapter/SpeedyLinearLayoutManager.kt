package com.chan9u.basetools.custom.adapter

import android.content.Context
import android.util.AttributeSet
import android.util.DisplayMetrics
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.ceil

/*------------------------------------------------------------------------------
 * DESC    : 스크롤 속도 조정 레이아웃 매니저
 *------------------------------------------------------------------------------*/

class SpeedyLinearLayoutManager: LinearLayoutManager {

    constructor(context: Context) : super(context, VERTICAL, false)
    constructor(context: Context, orientation: Int, reverseLayout: Boolean) : super(context, orientation, reverseLayout)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun smoothScrollToPosition(recyclerView: RecyclerView, state: RecyclerView.State, position: Int) {

        val linearSmoothScroller: LinearSmoothScroller = object : LinearSmoothScroller(recyclerView.context) {
            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics) =
                super.calculateSpeedPerPixel(displayMetrics) * (1 / ceil(findFirstVisibleItemPosition() / 10.toFloat()))
        }
        linearSmoothScroller.targetPosition = position
        startSmoothScroll(linearSmoothScroller)
    }
}