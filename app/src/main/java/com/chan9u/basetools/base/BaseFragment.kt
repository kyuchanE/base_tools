package com.chan9u.basetools.base

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageButton
import androidx.core.view.isVisible
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.chan9u.basetools.utils.bind
import com.chan9u.basetools.utils.dp2px
import com.chan9u.basetools.utils.gone
import com.chan9u.basetools.utils.setOnEvents
import com.jakewharton.rxbinding3.recyclerview.scrollEvents
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe


/*------------------------------------------------------------------------------
 * DESC    : 프래그먼트 기본 정의
 *------------------------------------------------------------------------------*/

abstract class BaseFragment<B : ViewDataBinding> : Fragment() {

    private var touchDownScroll = 0
    private var toTop = false

    protected lateinit var binding: B

    abstract val layoutId: Int

    // Rx 핸들러
    private val compositeDisposable = CompositeDisposable()

    val baseActivity: BaseActivity<B>
        get() = activity as BaseActivity<B>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = inflater.bind(layoutId, container)
        binding.setOnEvents()

        // 이벤트 버스 등록
        EventBus.getDefault().register(this)

        return binding.root
    }

    override fun onDestroyView() {
        // 이벤트 버스 해제
        EventBus.getDefault().unregister(this)
        compositeDisposable.clear()
        super.onDestroyView()
    }

    /**
     * 필요 메소드
     */
    open fun initValues() {}

    open fun initViews() {}
    open fun initEvents() {}
    open fun initAdapters() {}

    fun <T> getParentActivity(): T = activity as T

    /**
     * 이벤트 버스 버그를 막기위한 메소드
     * greenrobot 이벤트 버스는 기본적으로 Subscribe 메소드가 하나라도 작성되어있어야 함
     *
     * @param fragment
     */
    @Subscribe
    fun eventbus(fragment: BaseFragment<B>) {
    }

    /**
     * Rx 핸들을 핸들러에 등록
     *
     * @param disposable Rx 핸들
     */
    fun addDisposable(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    /**
     * Rx 핸들을 핸들러에 제외
     *
     * @param disposable Rx 핸들
     */
    fun deleteDisposable(disposable: Disposable) {
        compositeDisposable.delete(disposable)
    }

    /**
     * Rx 핸들을 중지하고 핸들러에서 제외
     *
     * @param disposable Rx 핸들
     */
    fun removeDisposable(disposable: Disposable) {
        compositeDisposable.remove(disposable)
    }

    /**
     * 버튼 이벤트 처리
     *
     * @param v
     */
    open fun onBtnEvents(v: View) {}

    /**
     * 리스트뷰와 탑버튼을 링크
     *
     * @param view 리스트뷰
     * @param btn 탑버튼
     * @param upAction 스크롤 업 액션
     * @param downAction 스크롤 다운 액션
     */
    @SuppressLint("ClickableViewAccessibility")
    fun linkScrollTop(view: RecyclerView, btn: ImageButton, upAction: () -> Unit = {}, downAction: () -> Unit = {}) {
        view.scrollEvents()
            .subscribe {
                btn.isVisible = !toTop && view.computeVerticalScrollOffset() > 50.dp2px
            }

        view.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> {
                        toTop = false
                        touchDownScroll = view.computeVerticalScrollOffset()
                    }
                    MotionEvent.ACTION_UP -> {
                        var scrollTerm = view.computeVerticalScrollOffset() - touchDownScroll
                        if (scrollTerm < (-15).dp2px || view.computeVerticalScrollOffset() == 0) {
                            upAction()
                        } else if (scrollTerm > 15.dp2px) {
                            downAction()
                        }
                    }
                    else -> {}
                }

                return false
            }
        })

        btn.setOnClickListener {
            view.smoothScrollToPosition(0)

            btn.gone()
            toTop = true
            upAction()
        }
    }

}