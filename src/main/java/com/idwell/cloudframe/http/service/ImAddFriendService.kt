package com.idwell.cloudframe.http.service

import com.idwell.cloudframe.http.entity.Base
import com.idwell.cloudframe.http.entity.ImAddFriendInfo
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ImAddFriendService {
    @FormUrlEncoded
    @POST("device/addVideoFriends")
    fun addFriend(@Field("device_id") device_id: Int, @Field("device_token") device_token: String): Observable<Base<ImAddFriendInfo>>
}