package com.idwell.cloudframe.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.idwell.cloudframe.http.entity.ImAddFriendInfo

@Dao
interface ImDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg user: ImAddFriendInfo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(users: MutableList<ImAddFriendInfo>)

    @Delete
    fun delete(vararg user: ImAddFriendInfo)

    @Query("DELETE FROM ImAddFriendInfo WHERE id = :id")
    fun deleteId(id: Int)

    @Query("DELETE FROM ImAddFriendInfo WHERE videoCallName = :id")
    fun deleteId(id: String)

    @Query("DELETE FROM ImAddFriendInfo")
    fun deleteAll()

    @Update
    fun update(vararg user: ImAddFriendInfo)

    @Query("SELECT * FROM ImAddFriendInfo WHERE videoCallName = :id")
    fun queryId(id: String): ImAddFriendInfo

    @Query("SELECT * FROM ImAddFriendInfo WHERE videoCallName is not null")
    fun queryAll(): LiveData<MutableList<ImAddFriendInfo>>

    @Query("SELECT * FROM ImAddFriendInfo")
    fun queryAccepted(): LiveData<MutableList<ImAddFriendInfo>>
}