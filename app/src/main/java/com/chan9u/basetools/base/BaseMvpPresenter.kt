package com.chan9u.basetools.base

import com.chan9u.basetools.activity.main.MainActivity
import com.chan9u.basetools.utils.L
import com.chan9u.basetools.utils.log
import com.hannesdorfmann.mosby3.mvp.MvpBasePresenter
import com.hannesdorfmann.mosby3.mvp.MvpView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Job
import java.util.concurrent.atomic.AtomicInteger

/*------------------------------------------------------------------------------
 * DESC    : MVP Presenter 기본 정의
 *------------------------------------------------------------------------------*/

open class BaseMvpPresenter<V : MvpView> : MvpBasePresenter<V>() {
    // RX 핸들러
    private var compositeDisposable = CompositeDisposable()

    // 중복요청 제한 잠금
    private val lockingMap: MutableMap<String, Boolean> = mutableMapOf()

    // API 동기화
    private val apiCounterMap: MutableMap<String, AtomicInteger> = mutableMapOf()
    private val apiCountMap: MutableMap<String, Int> = mutableMapOf()


    override fun detachView() {
        clearDisposable()
        super.detachView()
    }

    /**
     * Rx 동작을 관리목록에 추가
     *
     * @param disposable
     */
    fun addDisposable(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    /**
     * 관리목록에 등록된 Rx 동작을 모두 취소
     *
     */
    fun clearDisposable() {
        compositeDisposable.clear()
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
     * 입출력 전용 스레드 반환
     *
     * @return 입출력 전용 스레드
     */
    protected fun io() = Schedulers.io()

    /**
     * 안드로이드 메인 스레드 반환
     *
     * @return 안드로이드 메인 스레드
     */
    protected fun ui() = AndroidSchedulers.mainThread()

    /**
     * 현재 요청이 잠겨있는지 확인 및 잠금 설정
     *
     * @param tag 잠금설정 태그
     *
     * @return 잠금여부
     */
    @Synchronized
    private fun locking(tag: String): Boolean {
        val loading =  lockingMap.containsKey(tag)
        lockingMap[tag] = true

        L.d("locking : $loading")
        return loading
    }

    /**
     * 요청 잠금 설정 해제
     *
     * @param tag 잠금설정 태그
     */
    @Synchronized
    fun unlock(tag: String) {
        lockingMap[tag] = false
    }

    /**
     * 뷰가 설정되면 액셜 실행
     *
     * @param action 요청 액션
     */
    fun ifViewAttached(action: V.() -> Disposable) {
        super.ifViewAttached {
            addDisposable(it.action())
        }
    }

    /**
     * super의 ifViewAttached를 사용하기 위해
     *
     * * @param action 요청 액션
     */
    fun withView(action: V.() -> Unit) {
        super.ifViewAttached {
            it.action()
        }
    }

    /**
     * 요청 잠금설정
     *
     * @param tag 잠금설정 태그
     * @param action 요청 액션
     */
    fun lock(tag: String, action: V.() -> Disposable) {
        if (!locking(tag)) {
            ifViewAttached { action() }
        }
    }

    /**
     * API 요청 싱크 카운팅 설정
     *
     * @param tag 설정할 태그
     * @param count 호출되어야할 API 카운트
     */
    fun syncComplete(tag: String, count: Int) {
        if (!apiCounterMap.containsKey(tag)) {
            apiCounterMap[tag] = AtomicInteger()
        }
        if (!apiCountMap.containsKey(tag)) {
            apiCountMap[tag] = count
        }
    }

    /**
     * 하나의 API가 호출 완료될때마다 싱크 카운트 증가 후 완료시 액션실행
     *
     * @param tag 설정된 태그
     * @param action 완료시 실행될 액션
     */
    @Synchronized
    fun complete(tag: String, action: () -> Unit = {}) {
        if (apiCounterMap.containsKey(tag) &&
            apiCountMap.containsKey(tag) &&
            apiCounterMap[tag]!!.incrementAndGet() == apiCountMap[tag]) {
            apiCounterMap[tag] = AtomicInteger()
            action()
            "API syncComplete".log()
        }
    }
}