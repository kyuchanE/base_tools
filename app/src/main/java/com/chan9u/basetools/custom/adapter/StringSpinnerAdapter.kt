package com.chan9u.basetools.custom.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.chan9u.basetools.R
import com.chan9u.basetools.utils.asColor
import com.chan9u.basetools.utils.simple

/*------------------------------------------------------------------------------
 * DESC    : String 스피너 어뎁터
 *------------------------------------------------------------------------------*/

class StringSpinnerAdapter: BaseAdapter() {

    private var viewPosition: Int = 0

    private lateinit var textView0: TextView
    private lateinit var textView1: TextView
    private lateinit var textView2: TextView

    private val spinnerStringItemList: ArrayList<String> = ArrayList<String>().apply {
        add("추천순")
        add("낮은가격순")
        add("높은가격순")
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.item_string_spinner, null)
        val tvSpinnerItem: TextView = view.findViewById(R.id.tv_spinner)
        tvSpinnerItem.text = spinnerStringItemList.get(position)
        when (position) {
            0 -> textView0 = tvSpinnerItem
            1 -> textView1 = tvSpinnerItem
            2 -> textView2 = tvSpinnerItem
        }

        return view
    }

    override fun getItem(position: Int): Any = spinnerStringItemList?.let { spinnerStringItemList.get(position) }

    override fun getItemId(position: Int): Long = 0

    override fun getCount(): Int = spinnerStringItemList?.let { spinnerStringItemList.size }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        textView0.text = spinnerStringItemList.get(0).simple.all(spinnerStringItemList.get(0)).textColor("#666666".asColor)
        textView1.text = spinnerStringItemList.get(1).simple.all(spinnerStringItemList.get(1)).textColor("#666666".asColor)
        textView2.text = spinnerStringItemList.get(2).simple.all(spinnerStringItemList.get(2)).textColor("#666666".asColor)

        when (viewPosition) {
            0 -> textView0.text = spinnerStringItemList.get(0).simple.all(spinnerStringItemList.get(0)).textColor("#fe0605".asColor).underline()
            1 -> textView1.text = spinnerStringItemList.get(1).simple.all(spinnerStringItemList.get(1)).textColor("#fe0605".asColor).underline()
            2 -> textView2.text = spinnerStringItemList.get(2).simple.all(spinnerStringItemList.get(2)).textColor("#fe0605".asColor).underline()
        }

        return super.getDropDownView(position, convertView, parent)
    }

    // 선택되어진 아이템 position
    fun selecteTextView(position: Int) {
        viewPosition = position
    }

}