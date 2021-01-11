package com.hyphenate.chatuidemo.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class ImAddFriendInfoRef implements Parcelable {
    String videoCallName="";
    String message;
    int itemType;
    String nickerName;

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

    public int getItemType() {
        return itemType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(videoCallName);
        dest.writeString(message);
        dest.writeInt(itemType);
        dest.writeString(nickerName);
    }
    public static final Creator<ImAddFriendInfoRef> CREATOR = new Creator<ImAddFriendInfoRef>() {
        @Override
        public ImAddFriendInfoRef createFromParcel(Parcel in) {
            ImAddFriendInfoRef params = new ImAddFriendInfoRef();
            params.videoCallName = in.readString();
            params.message = in.readString();
            params.itemType = in.readInt();
            params.nickerName = in.readString();
            return params;
        }

        @Override
        public ImAddFriendInfoRef[] newArray(int size) {
            return new ImAddFriendInfoRef[size];
        }
    };

}
