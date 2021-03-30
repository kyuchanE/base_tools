package com.chan9u.basetools.base

import android.app.Activity
import android.app.Application
import android.os.Build
import android.os.Bundle
import android.webkit.WebView
import androidx.multidex.MultiDexApplication
import com.blankj.utilcode.util.Utils
import com.chan9u.basetools.BuildConfig
import com.chan9u.basetools.utils.API
import com.facebook.stetho.Stetho
import com.orhanobut.hawk.Hawk
import es.dmoral.toasty.Toasty

/*------------------------------------------------------------------------------
 * DESC    : Application을 커스텀
 *------------------------------------------------------------------------------*/

class BaseApplication: MultiDexApplication(), Application.ActivityLifecycleCallbacks {
    var mainActivityLive = false

    override fun onCreate() {
        super.onCreate()

        // 유틸
        Utils.init(this)

        // 프리퍼런스
        Hawk.init(this).build()

        // 통신모듈
        API.init(this)

        // debug
        Stetho.initializeWithDefaults(this)

        // 웹뷰 디버깅
        if (BuildConfig.DEV && Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }

        // Toast
        Toasty.Config.getInstance()
            .allowQueue(false) // optional (prevents several Toastys from queuing)
            .apply()
    }

    override fun onActivityPaused(activity: Activity) {
        TODO("Not yet implemented")
    }

    override fun onActivityStarted(activity: Activity) {
        TODO("Not yet implemented")
    }

    override fun onActivityDestroyed(activity: Activity) {
        TODO("Not yet implemented")
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        TODO("Not yet implemented")
    }

    override fun onActivityStopped(activity: Activity) {
        TODO("Not yet implemented")
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        TODO("Not yet implemented")
    }

    override fun onActivityResumed(activity: Activity) {
        TODO("Not yet implemented")
    }
}