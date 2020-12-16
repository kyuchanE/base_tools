package com.chan9u.basetools.base

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.blankj.utilcode.util.BarUtils

/*------------------------------------------------------------------------------
 * DESC    : 다이얼로그 기본 정의
 *------------------------------------------------------------------------------*/
abstract class BaseDialog<B: ViewDataBinding>(val activity: BaseActivity<*>): Dialog(activity) {

    // data binding layoutId
    abstract val layoutId: Int

    // data binding
    protected lateinit var binding: B
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE) //타이틀 바 삭제

        binding = DataBindingUtil.inflate(layoutInflater, layoutId, null, false)

        setCancelable(false)
        setContentView(binding.root)

        with(window!!) {
            setBackgroundDrawableResource(android.R.color.transparent) // 그림자 삭제
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            BarUtils.setStatusBarColor(this, Color.TRANSPARENT)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                statusBarColor = Color.TRANSPARENT
//            } else {
//
//            }
        }

    }

    open fun showDialog() {
        activity.showDialog(this)
    }

    open fun hideDialog() {
        activity.hideDialog(this)
    }
}