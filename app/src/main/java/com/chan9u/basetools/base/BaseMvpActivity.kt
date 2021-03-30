package com.chan9u.basetools.base

import android.os.Bundle
import androidx.databinding.ViewDataBinding
import com.chan9u.basetools.utils.isNull
import com.hannesdorfmann.mosby3.mvp.MvpPresenter
import com.hannesdorfmann.mosby3.mvp.MvpView
import com.hannesdorfmann.mosby3.mvp.delegate.ActivityMvpDelegateImpl


/*------------------------------------------------------------------------------
 * DESC    : MVP Activity 기본 정의
 *------------------------------------------------------------------------------*/

abstract class BaseMvpActivity<B : ViewDataBinding, V : MvpView, P : BaseMvpPresenter<V>> : BaseActivity<B>(),
    MvpView, com.hannesdorfmann.mosby3.mvp.delegate.MvpDelegateCallback<V, P>{

    private var mvpDelegate: ActivityMvpDelegateImpl<V, P>? = null
    private lateinit var presenter: P
    protected var retainInstance: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getMvpDelegate().onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        getMvpDelegate().onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        getMvpDelegate().onSaveInstanceState(outState)
    }

    override fun onPause() {
        super.onPause()
        getMvpDelegate().onPause()
    }

    override fun onResume() {
        super.onResume()
        getMvpDelegate().onResume()
    }

    override fun onStart() {
        super.onStart()
        getMvpDelegate().onStart()
    }

    override fun onStop() {
        super.onStop()
        getMvpDelegate().onStop()
    }

    override fun onRestart() {
        super.onRestart()
        getMvpDelegate().onRestart()
    }

    override fun onContentChanged() {
        super.onContentChanged()
        getMvpDelegate().onContentChanged()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        getMvpDelegate().onPostCreate(savedInstanceState)
    }

    /**
     * Instantiate a presenter instance
     *
     * @return The [MvpPresenter] for this view
     */
    abstract override fun createPresenter(): P

    /**
     * Get the mvp delegate. This is internally used for creating presenter, attaching and detaching
     * view from presenter.
     *
     *
     * **Please note that only one instance of mvp delegate should be used per Activity
     * instance**.
     *
     *
     *
     *
     * Only override this method if you really know what you are doing.
     *
     *
     * @return [ActivityMvpDelegateImpl]
     */
    protected fun getMvpDelegate(): ActivityMvpDelegateImpl<V, P> {
        if (mvpDelegate.isNull) {
            mvpDelegate = ActivityMvpDelegateImpl(this, this, true)
        }

        return mvpDelegate!!
    }

    override fun getPresenter(): P {
        return presenter
    }

    override fun setPresenter(presenter: P) {
        this.presenter = presenter
    }

    override fun getMvpView(): V {
        return this as V
    }
}