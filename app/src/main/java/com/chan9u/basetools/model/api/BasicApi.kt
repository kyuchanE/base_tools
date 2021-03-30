package com.chan9u.basetools.model.api

import com.google.gson.JsonObject
import io.reactivex.Flowable
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

/*------------------------------------------------------------------------------
 * DESC    : 기본 API
 *------------------------------------------------------------------------------*/

@JvmSuppressWildcards
interface BasicApi {
    @GET
    fun get(
        @Url url: String,
        @QueryMap params: Map<String, Any?> = mapOf(),
        @HeaderMap headers: Map<String, Any?> = mapOf()
    ): Flowable<JsonObject>

    @FormUrlEncoded
    @POST
    fun form(
        @Url url: String,
        @FieldMap params: Map<String, Any?> = mapOf(),
        @HeaderMap headers: Map<String, Any?> = mapOf()

    ): Flowable<JsonObject>

    @POST
    fun post(
        @Url url: String,
        @Body params: Map<String, Any?> = mapOf(),
        @HeaderMap headers: Map<String, Any?> = mapOf()
    ): Flowable<JsonObject>


    @POST
    fun upload(
        @Url url: String,
        @Body body: RequestBody,
        @HeaderMap headers: Map<String, Any?> = mapOf()
    ): Flowable<JsonObject>

    @Streaming
    @GET
    fun download(
        @Url url: String,
        @QueryMap params: Map<String, Any?> = mapOf(),
        @HeaderMap headers: Map<String, Any?> = mapOf()
    ): Flowable<Response<ResponseBody>>
}