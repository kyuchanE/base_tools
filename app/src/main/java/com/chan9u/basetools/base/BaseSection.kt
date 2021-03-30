package com.chan9u.basetools.base

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.chan9u.basetools.utils.isLast
import io.github.luizgrp.sectionedrecyclerviewadapter.Section
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters

/*------------------------------------------------------------------------------
 * DESC    : 기본 섹션
 *------------------------------------------------------------------------------*/

abstract class BaseSection<A : BaseActivity<*>, D>(params: SectionParameters) : Section(params) {

    var handler: A? = null // 기본 핸들러

    var isHeaderBind: Boolean = false // 헤더가 그려졌는지 여부
    var isFooterBind: Boolean = false // 푸터가 그려졌는지 여부

    var items: MutableList<D> = mutableListOf()
        set(value) {
            field.clear()
            field.addAll(value)
        }

    var totalCount: Int = 0 // 아이템 카운트

    constructor(params: SectionParameters, handler: A) : this(params) {
        this.handler = handler
    }

    constructor(params: SectionParameters, handler: A, items: List<D>) : this(params, handler) {
        this.items.addAll(items)
    }

    fun isMore(position: Int) = items.isLast(position) && position < totalCount - 1

    override fun getContentItemsTotal() = items.size

    override fun getItemViewHolder(view: View): RecyclerView.ViewHolder = object : RecyclerView.ViewHolder(view) {}
    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder, pos: Int) = Unit
}