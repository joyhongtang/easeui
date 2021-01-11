package com.hyphenate.chatuidemo.entity;

import androidx.annotation.NonNull;

public class ImCallInfoRef{
    String chatStartTime="";
    int itemType;
    String chatters;
    String chatDur;
    String chatType;

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

    public String getChatDur() {
        return chatDur;
    }

    public void setChatDur(String chatDur) {
        this.chatDur = chatDur;
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

    public int getItemType() {
        return itemType;
    }
}
