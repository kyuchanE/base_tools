package com.chan9u.basetools.base

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.databinding.ViewDataBinding
import com.chan9u.basetools.utils.isNull
import com.hannesdorfmann.mosby3.mvp.MvpView
import com.hannesdorfmann.mosby3.mvp.delegate.FragmentMvpDelegateImpl
import com.hannesdorfmann.mosby3.mvp.delegate.MvpDelegateCallback


/*------------------------------------------------------------------------------
 * DESC    : MVP Fragment 기본 정의
 *------------------------------------------------------------------------------*/

abstract class BaseMvpFragment<B : ViewDataBinding, V : MvpView, P : BaseMvpPresenter<V>> : BaseFragment<B>(),
    MvpDelegateCallback<V, P>, MvpView {

    private var mvpDelegate: FragmentMvpDelegateImpl<V, P>? = null

    /**
     * The presenter for this view. Will be instantiated with [.createPresenter]
     */
    private lateinit var presenter: P

    /**
     * Creates a new presenter instance, if needed. Will reuse the previous presenter instance if
     * [.setRetainInstance] is set to true. This method will be called from
     * [.onViewCreated]
     */
    abstract override fun createPresenter(): P

    /**
     * * Get the mvp delegate. This is internally used for creating presenter, attaching and
     * detaching view from presenter.
     *
     *
     *
     * **Please note that only one instance of mvp delegate should be used per fragment
     * instance**.
     *
     *
     *
     *
     * Only override this method if you really know what you are doing.
     *
     *
     * @return [FragmentMvpDelegateImpl]
     */
    protected fun getMvpDelegate(): FragmentMvpDelegateImpl<V, P> {
        if (mvpDelegate.isNull) {
            mvpDelegate = FragmentMvpDelegateImpl(this, this, true, true)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getMvpDelegate().onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        getMvpDelegate().onDestroyView()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getMvpDelegate().onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        getMvpDelegate().onDestroy()
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        getMvpDelegate().onActivityCreated(savedInstanceState)
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        getMvpDelegate().onAttach(activity)
    }

    override fun onDetach() {
        super.onDetach()
        getMvpDelegate().onDetach()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        getMvpDelegate().onSaveInstanceState(outState)
    }
}