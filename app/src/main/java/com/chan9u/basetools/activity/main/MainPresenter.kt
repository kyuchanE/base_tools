package com.chan9u.basetools.activity.main

import com.chan9u.basetools.base.BaseMvpPresenter
import com.chan9u.basetools.utils.API
import com.chan9u.basetools.utils.L
import com.chan9u.basetools.utils.get
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainPresenter: BaseMvpPresenter<MainActivity>() {

    // coroutine test
    fun testCoroutine() {

        withView {
//            get {
//                url = "http://10.44.1.14:8110/main/0001/contents/DMN00000000000000000"
//                params = mutableMapOf(
//                    "startIndex" to 1,
//                    "endIndex" to 32
//                )
//                success = {
//                    L.d("@@@@@@@ testCoroutine >> $this")
//                }
//            }

            launch {

                API.basicApi.cGet(
                    url = "http://10.44.1.14:8110/main/0001/contents/DMN00000000000000000",
                    params = mutableMapOf(
                        "startIndex" to 1,
                        "endIndex" to 32
                    )
                ).collect { item ->
                    L.d("@@@@ testCoroutine 22 >> $item")
                }

//                cGet {
//                    url = "main/0001/contents/DMN00000000000000000"
//                    params = mutableMapOf(
//                        "startIndex" to 1,
//                        "endIndex" to 32
//                    )
//                }.collect { item ->
//                    L.d("@@@@ testCoroutine >> $item")
//                }


            }
        }
    }
}