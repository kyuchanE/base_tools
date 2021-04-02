package com.chan9u.basetools.activity.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.chan9u.basetools.R
import com.chan9u.basetools.base.BaseActivity
import com.chan9u.basetools.base.BaseMvpActivity
import com.chan9u.basetools.databinding.ActivityMainBinding
import com.chan9u.basetools.ex.RippleButtonActivity
import com.chan9u.basetools.utils.L
import com.chan9u.basetools.utils.ckTest
import com.chan9u.basetools.utils.hTest
import com.chan9u.basetools.utils.kyuTest

class MainActivity : BaseMvpActivity<ActivityMainBinding, MainActivity, MainPresenter>() {

    override val layoutId: Int = R.layout.activity_main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initViews()
    }

    override fun initViews() {
        super.initViews()

        binding.btnRipple.setOnClickListener {
            startActivity(Intent(this, RippleButtonActivity::class.java))
        }

    }

    override fun onBtnEvents(v: View) {
        super.onBtnEvents(v)
        when (v.id) {
            R.id.btn_coroutine -> {
                presenter.testCoroutine()
            }

            R.id.btn_test -> {
                L.d("@@@@@@@@@@@@@@ 111 >> ${"hwang".ckTest()}")
                L.d("@@@@@@@@@@@@@@ 222 >> ${hTest("chan")}")
                L.d("@@@@@@@@@@@@@@ 333 >> ${"TEST".kyuTest}")
            }
        }
    }

    override fun createPresenter(): MainPresenter = MainPresenter()
}