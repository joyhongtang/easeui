package com.idwell.cloudframe.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.idwell.cloudframe.http.entity.ImCallInfo

@Dao
interface ImCallInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg user: ImCallInfo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(users: MutableList<ImCallInfo>)

    @Delete
    fun delete(vararg user: ImCallInfo)

    @Query("DELETE FROM ImCallInfo WHERE chatStartTime = :id")
    fun deleteId(id: Int)

    @Query("DELETE FROM ImCallInfo")
    fun deleteAll()

    @Update
    fun update(vararg user: ImCallInfo)

    @Query("SELECT * FROM ImCallInfo WHERE chatStartTime = :id")
    fun queryId(id: String): ImCallInfo

    @Query("SELECT * FROM ImCallInfo")
    fun queryAll(): LiveData<MutableList<ImCallInfo>>

    @Query("SELECT * FROM ImCallInfo")
    fun queryAccepted(): LiveData<MutableList<ImCallInfo>>
}