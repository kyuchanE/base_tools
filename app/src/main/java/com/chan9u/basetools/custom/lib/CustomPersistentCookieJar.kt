package com.chan9u.basetools.custom.lib

import com.franmontiel.persistentcookiejar.ClearableCookieJar
import com.franmontiel.persistentcookiejar.cache.CookieCache
import com.franmontiel.persistentcookiejar.persistence.CookiePersistor
import okhttp3.Cookie
import okhttp3.HttpUrl

/*------------------------------------------------------------------------------
 * DESC    : PersistentCookieJar 커스텀
 *------------------------------------------------------------------------------*/

class CustomPersistentCookieJar(var cache: CookieCache, var persistor: CookiePersistor) :
    ClearableCookieJar {

    init {
        cache.addAll(persistor.loadAll())
    }

    @Synchronized
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cache.addAll(cookies)
        persistor.saveAll(cookies)
    }

    @Synchronized
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val cookiesToRemove: MutableList<Cookie>    = mutableListOf()
        val validCookies: MutableList<Cookie>       = mutableListOf()

        val iterator = cache.iterator()
        for (currentCookie in iterator) {
            if (isCookieExpired(currentCookie)) {
                cookiesToRemove.add(currentCookie)
                iterator.remove()
            } else if (currentCookie.matches(url)) {
                validCookies.add(currentCookie)
            }
        }
        persistor.removeAll(cookiesToRemove)
        return validCookies
    }

    @Synchronized
    fun loadForAll(): List<Cookie> {
        val cookiesToRemove: MutableList<Cookie>    = mutableListOf()
        val validCookies: MutableList<Cookie>       = mutableListOf()

        val iterator = cache.iterator()
        for (currentCookie in iterator) {
            if (isCookieExpired(currentCookie)) {
                cookiesToRemove.add(currentCookie)
                iterator.remove()
            } else {
                validCookies.add(currentCookie)
            }
        }
        persistor.removeAll(cookiesToRemove)
        return validCookies
    }

    @Synchronized
    override fun clearSession() {
        cache.clear()
        cache.addAll(persistor.loadAll())
    }

    @Synchronized
    override fun clear() {
        cache.clear()
        persistor.clear()
    }

    companion object {
        private fun isCookieExpired(cookie: Cookie): Boolean {
            return cookie.expiresAt < System.currentTimeMillis()
        }
    }
}