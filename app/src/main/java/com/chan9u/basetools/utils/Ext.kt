package com.chan9u.basetools.utils

import android.app.Activity
import android.content.ContextWrapper
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.chan9u.basetools.base.BaseActivity


/*------------------------------------------------------------------------------
 * DESC    : 확장함수
 *------------------------------------------------------------------------------*/


////////////////////////////// DataBinding //////////////////////////////

fun <T : ViewDataBinding> Activity.bind(layoutId: Int): T {
    return DataBindingUtil.setContentView(this, layoutId)
}

fun <T : ViewDataBinding> Activity.bindView(layoutId: Int, parent: ViewGroup? = null, attachToRoot: Boolean = false): T {
    return DataBindingUtil.inflate(layoutInflater, layoutId, parent, attachToRoot)
}

fun ViewDataBinding.setOnEvents(activity: BaseActivity<*>? = null) = root.setOnEvents(activity)


////////////////////////////// View //////////////////////////////
val View.isClick get() = tag == "click"

val View.activity: BaseActivity<*>?
    get() {
        var ctx = context
        while (ctx is ContextWrapper) {
            if (ctx is BaseActivity<*>) {
                return ctx
            }
            ctx = ctx.baseContext
        }
        return null
    }

val ViewGroup.views: List<View>
    get() {
        val views = mutableListOf<View>()
        val childCount = childCount
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child is ViewGroup) {
                views.addAll(child.views)
            }

            views.add(child)
        }
        return views
    }

val ViewGroup.eventViews: List<View>
    get() {
        val result = mutableListOf<View>()

        for (view in views) {
            when (view) {
                is Button,
                is ImageButton,
                is CompoundButton,
                is CheckedTextView,
                is RadioButton,
                is CheckBox
                -> result.add(view)
            }
            if (view.isClick) result.add(view)
        }
        return result
    }

fun View.setOnEvents(baseActivity: BaseActivity<*>? = null): View {
    var views = mutableListOf<View>()

    if (this is ViewGroup) views.addAll(eventViews)
    else views.add(this)

    val handler = baseActivity ?: activity
    handler?.let { h ->
        views.filter { it.id != View.NO_ID }.forEach {
            when (it) {
                is CompoundButton -> {
                    it.setOnCheckedChangeListener(h::onRxCheckedEvents)
                    it.setOnClickListener(h::onRxBtnEvents)
                }
                is Button, is ImageButton, is CheckedTextView -> it.setOnClickListener(h::onRxBtnEvents)
            }

            if (it.isClick) it.setOnClickListener(h::onRxBtnEvents)
        }
    }

    return this
}