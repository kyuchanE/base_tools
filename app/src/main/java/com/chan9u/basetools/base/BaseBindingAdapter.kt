package com.chan9u.basetools.base

import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import com.chan9u.basetools.utils.load

/*------------------------------------------------------------------------------
 * DESC    : DataBinding사용시 자동적용
 *------------------------------------------------------------------------------*/

object BaseBindingAdapter {

    @BindingAdapter("android:visibleIf")
    @JvmStatic
    fun View.setVisibleIf(value: Boolean) {
        isVisible = value
    }

    @BindingAdapter("url")
    @JvmStatic
    fun ImageView.url(url: String) {
        if (url.isEmpty()) return
        load(url)
    }

}