package com.chan9u.basetools.custom

import com.chan9u.basetools.base.BaseActivity
import com.chan9u.basetools.base.BaseDialog
import com.chan9u.basetools.databinding.DefaultDialogBinding
import com.chan9u.basetools.R

class DefaultDialog(activity: BaseActivity<*>): BaseDialog<DefaultDialogBinding>(activity) {

    override val layoutId: Int = R.layout.default_dialog

}