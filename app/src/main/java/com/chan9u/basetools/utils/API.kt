package com.chan9u.basetools.utils

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.os.Build
import android.os.Environment
import androidx.core.net.toUri
import com.chan9u.basetools.BuildConfig
import com.chan9u.basetools.base.BaseActivity
import com.chan9u.basetools.base.BaseMvpActivity
import com.chan9u.basetools.base.BaseMvpFragment
import com.chan9u.basetools.base.BaseMvpPresenter
import com.chan9u.basetools.custom.lib.CustomPersistentCookieJar
import com.chan9u.basetools.listener.RequestSubscriber
import com.chan9u.basetools.model.api.BasicApi
import com.chan9u.basetools.model.api.DownloadFile
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.google.gson.JsonObject
import com.hannesdorfmann.mosby3.mvp.MvpView
import com.ihsanbal.logging.Level
import com.ihsanbal.logging.LoggingInterceptor
import com.itkacher.okhttpprofiler.OkHttpProfilerInterceptor
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.collect
import okhttp3.OkHttpClient
import okhttp3.internal.platform.Platform
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

/*------------------------------------------------------------------------------
 * DESC    : RESTful 통신을 위한 Retrofit2를 설정하여 반환하는 API 클래스
 *------------------------------------------------------------------------------*/

object API {

    private const val PRINT_LOG = BuildConfig.DEV // 로그 출력여부

    private const val CONNECT_TIMEOUT = 3000L // 커넥션 타임
    private const val WRITE_TIMEOUT = 3000L // 쓰기 타임
    private const val READ_TIMEOUT = 3000L // 읽기 타임

    private val BASE_URL = BuildConfig.BASE_URL // API URL

    const val START_PAGE = 1 // 페이징 시작페이지
    const val PER_PAGE = 20 // 페이지당 아이템 수

    lateinit var context: Context

    val okHttpClient by lazy {
        val okHttpClientBuilder = configureClient(OkHttpClient().newBuilder())!!
//        val okHttpClientBuilder = OkHttpClient.Builder()

        val cookieJar = CustomPersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(context))

        okHttpClientBuilder
//                .connectionSpecs(listOf(ConnectionSpec.COMPATIBLE_TLS)) // https 관련 보안 옵션
            .cookieJar(cookieJar)                               // 쿠키 저장
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)  // 쓰기 타임아웃 시간 설정
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)      // 읽기 타임아웃 시간 설정
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)        // 연결 타임아웃 시간 설정
            .cache(null)                                 // 캐시사용 안함
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request()
                        .newBuilder()
                        .header("User-Agent", "")
                        .header("devicemodel", Build.MODEL)
//                                    .header("key", "value")
                        .build()
                )
            }

        if (PRINT_LOG) {
            val httpLoggingInterceptor = LoggingInterceptor.Builder() // 전송로그
                .setLevel(Level.BASIC)
                .log(Platform.INFO)
                .tag("ohlog")
                .build()

            okHttpClientBuilder
                .addInterceptor(httpLoggingInterceptor)
                .addInterceptor(OkHttpProfilerInterceptor())
                .addNetworkInterceptor(StethoInterceptor()) // Stetho 로그
        }

        okHttpClientBuilder.build()
    }

    val basicApi by lazy { build().create(BasicApi::class.java) }

    fun init(ctx: Context) {
        context = ctx
    }

    /**
     * 기본 설정하여 Retrofit을 반환
     *
     * @return 설정이 반영된 Retrofit
     */
    fun build(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())  // Rx를 사용할 수 있도록 아답터 적용
            .addConverterFactory(ScalarsConverterFactory.create())      // ScalarConverter 적용
            .addConverterFactory(GsonConverterFactory.create())         // GsonConverter 적용
            .build()
    }

}

/**
 * UnCertificated 허용
 *
 * @param builder OkHttpClient builder
 * @return 인증서를 무시하는 OkHttpClient builder
 */
@SuppressLint("TrustAllX509TrustManager")
fun configureClient(builder: OkHttpClient.Builder): OkHttpClient.Builder? {
    val certs = arrayOf<TrustManager>(object : X509TrustManager {
        @Throws(CertificateException::class)
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
        }


        @Throws(CertificateException::class)
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
        }

        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return arrayOf()
        }
    }
    )
    var ctx: SSLContext? = null
    try {
        ctx = SSLContext.getInstance("TLS")
        ctx.init(null, certs, SecureRandom())
    } catch (ex: Exception) {
        L.e(ex)
    }
    if (ctx.notNull) {
        try {
            val trustManager = certs[0] as X509TrustManager
            val sslSocketFactory = ctx!!.socketFactory
            builder.sslSocketFactory(sslSocketFactory, trustManager).hostnameVerifier(
                HostnameVerifier { _, _ -> true })
        } catch (e: Exception) {
            L.e(e)
        }
    }
    return builder
}

// 단축 네이밍
val basicApi by lazy { API.basicApi }

/**
 * API 요청 파라미터 세터
 */
class ApiSetter {
    var url: String = ""                        // 요청 url
    var params: Map<String, Any?> = mapOf()     // 파라미터 맵
    var headers: Map<String, Any?> = mapOf()    // 헤더 맵
    var handler: BaseActivity<*>? = null        // 로딩 및 에러 핸들러
    var presenter: BaseMvpPresenter<*>? = null  // 프리젠터
    var unlockTag: String? = null               // 요청 잠금 태그
    var showLoading: Boolean = true             // 로딩 노출 여부
    var skipErrorHandle: Boolean = false        // 기본 에러핸들 스킵여부
    var error: (Throwable) -> Unit = { _ -> }   // 에러 핸들러
    var finish: () -> Unit = {}                 // finally 핸들러
    var success: (JsonObject.() -> Unit) = {}   // 요청 성공 리시버
}

/**
 * 타입에 따른 api 요청 실행
 *
 * @param method 요청 메소드
 * @param setter 요청 세터
 */
fun api(
    method: String,
    setter: ApiSetter.() -> Unit
): RequestSubscriber<JsonObject> = ApiSetter().run {
    setter()

    when {
        handler.notNull -> reqWithView(method, setter)
        presenter.notNull -> reqWithPresenter(method, setter)
        else -> reqAlone(method, setter)
    }
}

/**
 * 프리젠터 안에서 get 요청
 *
 * @param lockTag 잠금태그
 * @param setter 요청 세터
 */
fun <V : MvpView> BaseMvpPresenter<V>.get(lockTag: String? = null, setter: ApiSetter.() -> Unit) = apiInPresenter("get", lockTag, setter)

/**
 * 프리젠터 안에서 post 요청
 *
 * @param lockTag 잠금태그
 * @param setter 요청 세터
 */
fun <V : MvpView> BaseMvpPresenter<V>.post(lockTag: String? = null, setter: ApiSetter.() -> Unit) = apiInPresenter("post", lockTag, setter)

/**
 * 프리젠터 안에서 form 요청
 *
 * @param lockTag 잠금태그
 * @param setter 요청 세터
 */
fun <V : MvpView> BaseMvpPresenter<V>.form(lockTag: String? = null, setter: ApiSetter.() -> Unit) = apiInPresenter("form", lockTag, setter)

/**
 * 뷰(핸들러)를 사용한 요청
 *
 * @param method 요청 메소드
 * @param setter 요청 세터
 */
private fun reqWithView(method: String, setter: ApiSetter.() -> Unit) = ApiSetter().run {
    setter()

    val disposable = reqMethod(method, url, params, headers)
        .ioMain()
        .doOnError(error)
        .doOnNext(success)
        .doFinally {
            unlockTag?.let { tag ->
                if (handler is BaseMvpActivity<*, *, *>) {
                    (handler as BaseMvpActivity<*, *, *>).getPresenter().unlock(tag)
                } else {
                    presenter!!.unlock(tag)
                }
            }
            finish()
        }
        .subscribeWith(handler!!.buildSubscriber(showLoading))
        .apply {
            skipErrorHandle = this@run.skipErrorHandle
        }

    when {
        handler is BaseMvpActivity<*, *, *> -> (handler as BaseMvpActivity<*, *, *>).getPresenter().addDisposable(disposable)
        presenter.notNull -> presenter!!.addDisposable(disposable)
        else -> handler!!.addDisposable(disposable)
    }

    disposable
}

/**
 * 프리젠터 사용한 요청
 *
 * @param method 요청 메소드
 * @param setter 요청 세터
 */
private fun reqWithPresenter(method: String, setter: ApiSetter.() -> Unit) = ApiSetter().run {
    setter()

    val disposable = reqMethod(method, url, params, headers)
        .ioMain()
        .doOnError(error)
        .doOnNext(success)
        .doFinally {
            unlockTag?.let { tag -> presenter!!.unlock(tag) }
            finish()
        }
        .subscribeWith(RequestSubscriber())

    presenter!!.addDisposable(disposable)

    disposable
}

/**
 * 단독 요청
 *
 * @param method 요청 메소드
 * @param setter 요청 세터
 */
private fun reqAlone(method: String, setter: ApiSetter.() -> Unit) = ApiSetter().run {
    setter()

    reqMethod(method, url, params, headers)
        .ioMain()
        .doOnError(error)
        .doOnNext(success)
        .doFinally(finish)
        .subscribeWith(RequestSubscriber())
}


/**
 * 요청 메소드에 따른 Flowable 반환
 *
 * @param method 요청 메소드
 * @param url 요청 url
 * @param params 요청 파라미터
 * @param headers 요청 헤더
 */
private fun reqMethod(
        method: String,
        url: String,
        params: Map<String, Any?> = mapOf(),
        headers: Map<String, Any?> = mapOf()
) = when (method) {
    "post" -> basicApi.post(url.asUrl, params, headers)
    "form" -> basicApi.form(url.asUrl, params, headers)
    else -> basicApi.get(url.asUrl, params, headers)
}

/**
 * 사용자 API class를 실행
 *
 * @param action API 실행 액션
 * @return 사용자 API
 */
inline fun <reified T> api(action: T.() -> Unit) = API.build().create(T::class.java).action()

/**
 * 프리젠터 안에서 api 요청
 *
 * @param method 요청 메소드
 * @param lockTag 잠금태그
 * @param setter 요청 세터
 */
private fun <V : MvpView> BaseMvpPresenter<V>.apiInPresenter(
    method: String,
    lockTag: String? = null,
    setter: ApiSetter.() -> Unit
) {
    lockTag?.let {
        // 잠금 태그가 있을경우
        lock(lockTag) {
            api(method) {
                setter()
                handler = getHandlerFromView(this@lock)
                presenter = this@apiInPresenter
                unlockTag = lockTag
            }
        }
    } ?: ifViewAttached {
        // 잠금 태그가 없을경우
        api(method) {
            setter()
            handler = getHandlerFromView(this@ifViewAttached)
            presenter = this@apiInPresenter
        }
    }
}

/**
 * 뷰에서 핸들러 추출
 *
 * @param view
 */
private fun <V : MvpView> getHandlerFromView(view: V) = when (view) {
    is BaseMvpFragment<*, *, *> -> view.baseActivity
    is BaseMvpActivity<*, *, *> -> view
    else -> null
}

/**
 * API 요청 URL을 생성하여 반환
 * url이 http로 시작하면 해당 url을 사용하고
 * 그렇지 않으면 base url에 append
 *
 * @param url 요청 URL
 * @return 연결 url
 */
fun getUrl(url: String): String {
    return if (url.startsWith("http")) {
        url
    } else {
//        "${C.init.mfront}$url".replaceFirst("${C.init.mfront}/", C.init.mfront)
        ""
    }
}

/**
 * 다운로드 요청 파라미터 세터
 */
class DownloadSetter {
    lateinit var context: Context                   // 컨텍스트
    var downloads: List<DownloadFile> = listOf()    // 다운로드 파일 리스트
    var title: String? = null                       // 다운로드 텍스트
}

/**
 * 파일 다운로드
 *
 * @param setter 요청 세터
 */
fun download(setter: DownloadSetter.() -> Unit) = DownloadSetter().run {
    setter()

    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    val downloadIdList = mutableListOf<Long>()
    downloads.forEach {
        val request = DownloadManager.Request(it.url.toUri())
            .setTitle(title ?: "${it.name}을(를) 다운로드 합니다.")
//                    .setDescription("Downloading...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, it.name)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
//                    .addRequestHeader("", "")

        downloadIdList.add(downloadManager.enqueue(request))
    }

    downloadIdList
}

fun <T> Flowable<T>.ioMain(): Flowable<T> = subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
fun <T> Flowable<T>.subIo(): Flowable<T> = subscribeOn(Schedulers.io())
fun <T> Flowable<T>.obMain(): Flowable<T> = observeOn(AndroidSchedulers.mainThread())