package com.idwell.cloudframe.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(users: MutableList<User>)

    @Delete
    fun delete(vararg user: User)

    @Query("DELETE FROM User WHERE id = :id")
    fun deleteId(id: Int)

    @Query("DELETE FROM User")
    fun deleteAll()

    @Update
    fun update(vararg user: User)

    @Query("SELECT * FROM User WHERE id = :id")
    fun queryId(id: Int): User

    @Query("SELECT * FROM User")
    fun queryAll(): LiveData<MutableList<User>>

    @Query("SELECT * FROM User WHERE isAccepted = '1'")
    fun queryAccepted(): LiveData<MutableList<User>>

    @Query("SELECT * FROM User WHERE isAdmin = 'mydevice'")
    fun queryIsAdmin(): User
}