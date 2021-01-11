package com.idwell.cloudframe.http.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.chad.library.adapter.base.entity.MultiItemEntity;

@Entity
public class ImCallInfo implements MultiItemEntity {
    @PrimaryKey
    @NonNull
    String chatStartTime = "";
    int itemType;
    String chatters;
    String chatEndTime;
    String chatType;

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    private boolean checked;
    public ImCallInfo(){}
    /**
     *
     * @param chatStartTime
     * @param itemType
     * @param chatters
     * @param chatDur
     * @param chatType
     */
    public ImCallInfo(String chatStartTime, int itemType, String chatters, String chatDur, String chatType) {
        this.chatStartTime = chatStartTime;
        this.itemType = itemType;
        this.chatters = chatters;
        this.chatEndTime = chatDur;
        this.chatType = chatType;
    }

    @NonNull
    public String getChatStartTime() {
        return chatStartTime;
    }

    public void setChatStartTime(@NonNull String chatStartTime) {
        this.chatStartTime = chatStartTime;
    }

    public String getChatters() {
        return chatters;
    }

    public void setChatters(String chatters) {
        this.chatters = chatters;
    }

    public String getChatType() {
        return chatType;
    }

    public void setChatType(String chatType) {
        this.chatType = chatType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    @Override
    public int getItemType() {
        return itemType;
    }

    public String getChatEndTime() {
        return chatEndTime;
    }

    public void setChatEndTime(String chatEndTime) {
        this.chatEndTime = chatEndTime;
    }
}
