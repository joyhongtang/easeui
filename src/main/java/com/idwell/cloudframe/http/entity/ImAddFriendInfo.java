package com.idwell.cloudframe.http.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.chad.library.adapter.base.entity.MultiItemEntity;

@Entity
public class ImAddFriendInfo implements MultiItemEntity {
    @PrimaryKey
    @NonNull
    String videoCallName = "";
    String message;
    int itemType;
    String nickerName;
    int id;
    String type;

    public String getIsAccepted() {
        return isAccepted;
    }

    public void setIsAccepted(String isAccepted) {
        this.isAccepted = isAccepted;
    }

    String isAccepted;

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    boolean isAdmin;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNickerName() {
        return nickerName;
    }

    public void setNickerName(String nickerName) {
        this.nickerName = nickerName;
    }

    public boolean isCanSelected() {
        return canSelected;
    }

    public void setCanSelected(boolean canSelected) {
        this.canSelected = canSelected;
    }

    boolean canSelected = true;

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    private boolean isSelected = false;

    public int getPostion() {
        return postion;
    }

    public void setPostion(int postion) {
        this.postion = postion;
    }

    int postion;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getVideoCallName() {
        return videoCallName;
    }

    public void setVideoCallName(String videoCallName) {
        this.videoCallName = videoCallName;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    @Override
    public int getItemType() {
        return itemType;
    }
}
