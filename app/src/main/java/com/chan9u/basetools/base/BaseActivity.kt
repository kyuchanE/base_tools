package com.chan9u.basetools.base

import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.util.Pair
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import com.chan9u.basetools.BuildConfig
import com.chan9u.basetools.R
import com.chan9u.basetools.custom.view.DefaultDialog
import com.chan9u.basetools.databinding.LoadingBinding
import com.chan9u.basetools.listener.RequestSubscriber
import com.chan9u.basetools.utils.*
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.scottyab.rootbeer.RootBeer
import com.trello.rxlifecycle2.android.ActivityEvent
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.CoroutineContext
import kotlin.system.exitProcess

/*------------------------------------------------------------------------------
 * DESC    : 액티비티 기본 정의
 *------------------------------------------------------------------------------*/
abstract class BaseActivity<B: ViewDataBinding>: AppCompatActivity(), CoroutineScope {

    // coroutineScope 관리 
    lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    companion object{
        // 이벤트 주기
        private const val THROTTLE_FIRST_DURATION = 500L

        // req permission
        private const val REQ_CODE_PERMISSION = 777
    }

    // view data binding
    protected lateinit var binding: B
        private set

    // data binding layoutId
    abstract val layoutId: Int

    // Rx handler
    private val compositeDisposable = CompositeDisposable()

    // Rx lifecycle
    val rxLifeCycle = BehaviorSubject.create<ActivityEvent>()

    // Rx event
    private val btnEventsSubject = PublishSubject.create<View>()     // 버튼 이벤트
    private val onCheckedEventsSubject = PublishSubject.create<Pair<View, Boolean>>()  // 체크박스 이벤트

    // activity finish action
    var onFinish: () -> Unit = {}

    // applicationContext
    val context: Context get() = applicationContext

    // dialogList
    private val dialogList = mutableListOf<Dialog>()

    // loading
    private lateinit var loadingBinding: LoadingBinding
    // 로딩 뷰 사용 카운트
    private val loadingCount = AtomicInteger()

    // 권한 허용 액션
    private var granted: () -> Unit = {}
    // 권한 거절 액션
    private var notGranted: () -> Unit = {}

    // 기본 에러 핸들러 on/off
    var useHandleError = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = bind(layoutId)
        binding.setOnEvents()

        job = Job()

        loadingBinding = bindView(R.layout.loading)
        (binding.root as ViewGroup).addView(
                loadingBinding.root,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        )

        rxLifeCycle.onNext(ActivityEvent.CREATE)

        btnEventsSubject
                .default()
                .doOnNext(::onBtnEvents)
                .subscribe()

        onCheckedEventsSubject
                .default()
                .doOnNext { onCheckedEvents(it.first, it.second) }
                .subscribe()
    }

    override fun onResume() {
        super.onResume()
        rxLifeCycle.onNext(ActivityEvent.RESUME)

        // 인트로는 자체 검사
        if (false) {
            // 단말기가 루팅되었는지 체크
            Single.fromCallable { RootBeer(this).isRooted || TigerTeam.detect() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter { it }
                .doOnSuccess {
                dialog("앱 위변조가 발견되어 종료합니다.")
                    .right {
                        Process.killProcess(Process.myPid())
                        exitProcess(10)
                    }
                }
                .subscribe()
        }
    }

    override fun onStart() {
        super.onStart()
        rxLifeCycle.onNext(ActivityEvent.START)
    }

    override fun onStop() {
        super.onStop()
        rxLifeCycle.onNext(ActivityEvent.STOP)
    }

    override fun finish() {
        // 액티비티 종료시 다이얼로그 닫기
        dialogList.forEach {
            if (it.isShowing) it.dismiss()
        }
        dialogList.clear()

        job.cancel()

        super.finish()

        // activity finish action
        onFinish()
    }

    override fun onDestroy() {
        // rx clear : 등록된 모든 핸들 중지
        compositeDisposable.clear()

        super.onDestroy()
        rxLifeCycle.onNext(ActivityEvent.DESTROY)
    }

    open fun initViews() {}

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

    fun <T> PublishSubject<T>.default(): Observable<T> {
        return this.observeOn(AndroidSchedulers.mainThread())
                .throttleFirst(THROTTLE_FIRST_DURATION, TimeUnit.MILLISECONDS)
                .doOnSubscribe(::addDisposable)
    }

    /**
     * 버튼 이벤트 처리
     *
     * @param v
     */
    open fun onBtnEvents(v: View) {}

    /**
     * 버튼 이벤트 처리 RX
     *
     * @param v
     */
    fun onRxBtnEvents(v: View) {
        btnEventsSubject.onNext(v)
    }

    /**
     * 체크박스 이벤트 처리
     *
     * @param v
     * @param isChecked
     */
    open fun onCheckedEvents(v: View, isChecked: Boolean) {}

    /**
     * 체크박스 이벤트 처리 RX
     *
     * @param v
     * @param isChecked
     */
    fun onRxCheckedEvents(v: View, isChecked: Boolean) {
        onCheckedEventsSubject.onNext(Pair(v, isChecked))
    }

    /**
     * 다이얼로그 띄우기
     *
     * @param dialog
     */
    @Synchronized
    fun showDialog(dialog: Dialog) {
        if (!isFinishing) {
            dialogList.forEach {
                if (it.isShowing) it.dismiss()
                dialogList.remove(it)
            }
            dialogList.add(dialog)
            dialog.show()
        }
    }

    /**
     * 다이얼로그 숨김
     *
     * @param dialog
     */
    @Synchronized
    fun hideDialog(dialog: Dialog) {
        if (!isFinishing) {
            dialog.dismiss()
            dialogList.remove(dialog)
        }
    }

    /**
     * 로딩표시
     */
    fun showLoading() {
        if (loadingCount.incrementAndGet() == 1) {
            runOnUiThread {
                loadingBinding.root.show()
            }
        }
    }

    /**
     * 로딩숨김
     */
    fun hideLoading() {
        if (loadingCount.decrementAndGet() == 0) {
            runOnUiThread {
                loadingBinding.root.gone()
            }
        }
    }

    /**
     * 공통 속성을 정의한 Subscriber
     *
     * @param useLoading 로딩 사용여부
     * @return
     */
    fun <T> buildSubscriber(useLoading: Boolean = true) = object : RequestSubscriber<T>() {
        override fun onStart() {
            super.onStart()
            if (useLoading) showLoading()
        }

        override fun onError(t: Throwable) {
            if (!skipErrorHandle) handleError(t)
            if (useLoading) hideLoading()
        }

        override fun onNext(t: T) {
            if (!skipErrorHandle) {
                if (t is JsonObject) {
                    handleServerCode(t)
                }
            }
        }

        override fun onComplete() {
            if (useLoading) hideLoading()
        }
    }

    /**
     * 기본 다이얼로그에 메시지 설정
     *
     * @param message 메시지
     *
     * @return 다이얼로그
     */
    fun dialog(message: String = "") = DefaultDialog(this).apply {
        right()
        if (message.isNotEmpty()) message(message)
    }

    fun dialog(action: DefaultDialog.() -> Unit) = DefaultDialog(this).apply {
        right()
        action()
    }

    /**
     * 기본 다이얼로그에 메시지 설정
     *
     * @param res 메시지 리소스
     *
     * @return 다이얼로그
     */
    fun dialog(@StringRes res: Int) = dialog(getString(res))

    /**
     * 퍼미션 다이얼로그가 보이기전 이벤트
     */
    open fun onShowPermissionDialog() {}

    /**
     * 퍼미션 다이얼로그가 사라진 직후 이벤트
     */
    open fun onHidePermissionDialog() {}

    /**
     * 퍼미션 허용 또는 거절 후 이벤트
     */
    open fun permissionNext() {}

    /**
     * 퍼미션 허용 이벤트
     */
    open fun grantedPermission() = granted()

    /**
     * 퍼미션 거절 이벤트
     */
    open fun notGrantedPermission() = notGranted()

    /**
     * 퍼미션 다이얼로그 다시보지 않기 이벤트
     *
     * @param permission
     */
    open fun notAskPermission(permission: String) {}

    /**
     * 버전에 따라 퍼미션 요청
     * 버전 < 마시멜로우 : 바로 서버요청
     * 버전 >= 마시멜로우 : 퍼미션 요청
     */
    @JvmOverloads
    fun permissions(
        permissions: List<String>,
        granted: () -> Unit = {},
        notGranted: () -> Unit = {}
    ) {
        this.granted = granted
        this.notGranted = notGranted

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            grantedPermission()
            permissionNext()

        } else {
            val notGrants = permissions
                .filter { ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED }
                .toTypedArray()

            if (notGrants.isNotEmpty()) {
                onShowPermissionDialog()
                ActivityCompat.requestPermissions(this, notGrants, REQ_CODE_PERMISSION)
                return
            }

            grantedPermission()
            permissionNext()
        }
    }

    /**
     * Runtime Permission 결과 이벤트
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQ_CODE_PERMISSION) {
            if (grantResults.filter { it == PackageManager.PERMISSION_GRANTED }
                    .count() == grantResults.size) {
                // 퍼미션 허용
                grantedPermission()
            } else {
                // 퍼미션 거절
                notGrantedPermission()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    permissions
                        .filterNot(::shouldShowRequestPermissionRationale)
                        .forEach(::notAskPermission)
                }
            }

            onHidePermissionDialog()
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    /**
     * 서버 에러코드 처리
     */
    fun handleServerCode(result: JsonObject) {
        val status = result.asInt("returnStatus")
        val code = result.asString("returnCode")
        val message = result.asString("returnMessage")

        // (!200 || !SUCCESS) && !401 && message
        if (((status != 200 || !code.equals("SUCCESS", true)) && code != "0000" && status != 401 && message.isNotEmpty())) {
            dialog(message)
        }
    }

    /**
     * 예상되는 예외처리
     *
     * @param t
     */
    fun handleError(t: Throwable) {
        if (useHandleError) {
            when (t) {
                is HttpException -> {
                    // 에러코드별 핸들링
                    when (t.code()) {
                        401 -> {
                        }
                        400, 404, 503 -> {
                        }
                        500 -> {
                            dialog("서비스가 지연되고 있습니다. 잠시 후 다시 시도해주시기 바랍니다.")
                                .right {
                                    finish()
                                }
                        }
                        else -> {
                        }
                    }

                }
                is ConnectException -> {
                    L.e("ConnectException")
                }
                is UnknownHostException -> {
                    L.e("UnknownHostException")
                }
                is SocketTimeoutException -> {
                    L.e("SocketTimeoutException")
                }
                else -> {
                }
            }
        }

        if (BuildConfig.DEV) {
            L.e(t)
        }
    }

}