package com.idwell.cloudframe.data.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.blankj.utilcode.util.Utils
import com.idwell.cloudframe.data.db.dao.ImDao
import com.idwell.cloudframe.data.db.dao.UserDao

import com.idwell.cloudframe.data.db.dao.ImCallInfoDao
import com.idwell.cloudframe.data.db.dao.User
import com.idwell.cloudframe.http.entity.ImAddFriendInfo
import com.idwell.cloudframe.http.entity.ImCallInfo

@Database(entities = [User::class,ImAddFriendInfo::class, ImCallInfo::class], version = 1)
@TypeConverters(IntConverter::class)
abstract class MyCallDatabase : RoomDatabase() {
    abstract val userDao: UserDao
    abstract val imDao: ImDao
    abstract val imCallInfoDao: ImCallInfoDao

    private object DatabaseHolder {
        val database = Room.databaseBuilder(Utils.getApp(), MyCallDatabase::class.java, "call_cloud_frame").build()
    }

    companion object {
        val instance = DatabaseHolder.database
    }
}
