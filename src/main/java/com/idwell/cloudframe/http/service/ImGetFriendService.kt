package com.idwell.cloudframe.http.service

import com.idwell.cloudframe.http.entity.Base
import com.idwell.cloudframe.http.entity.ImFriendList
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ImGetFriendService {
    @FormUrlEncoded
    @POST("device/getVideoSingleUserFriends")
    fun getVideoSingleUserFriends(@Field("device_id") device_id: Int): Observable<Base<ImFriendList>>
}