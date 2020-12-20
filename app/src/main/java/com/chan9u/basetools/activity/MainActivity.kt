package com.chan9u.basetools.activity

import android.content.Intent
import android.os.Bundle
import com.chan9u.basetools.R
import com.chan9u.basetools.base.BaseActivity
import com.chan9u.basetools.databinding.ActivityMainBinding
import com.chan9u.basetools.ex.RippleButtonActivity

class MainActivity : BaseActivity<ActivityMainBinding>() {

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
}