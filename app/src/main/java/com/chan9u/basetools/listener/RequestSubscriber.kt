package com.chan9u.basetools.listener

import io.reactivex.subscribers.ResourceSubscriber

/*------------------------------------------------------------------------------
 * DESC    : 공통으로 사용되는 Subscriber
 *------------------------------------------------------------------------------*/
open class RequestSubscriber<T> : ResourceSubscriber<T>() {
    var skipErrorHandle = false
    override fun onNext(t: T) {}
    override fun onError(t: Throwable) {}
    override fun onComplete() {}
}