package com.idwell.cloudframe.ui.tab.util;

import com.hyphenate.chatuidemo.entity.ImAddFriendInfoRef;
import com.idwell.cloudframe.http.entity.ImAddFriendInfo;

public class BridgeImCallInfoUtil {
    public static ImAddFriendInfoRef convertImCallInfo(ImAddFriendInfo imAddFriendInfo){
        if(null == imAddFriendInfo){
            return null;
        }
        ImAddFriendInfoRef imAddFriendInfoRef = new ImAddFriendInfoRef();
        imAddFriendInfoRef.setCanSelected(imAddFriendInfo.isCanSelected());
        imAddFriendInfoRef.setItemType(imAddFriendInfo.getItemType());
        imAddFriendInfoRef.setMessage(imAddFriendInfo.getMessage());
        imAddFriendInfoRef.setNickerName(imAddFriendInfo.getNickerName());
        imAddFriendInfoRef.setPostion(imAddFriendInfo.getPostion());
        imAddFriendInfoRef.setSelected(imAddFriendInfo.isSelected());
        imAddFriendInfoRef.setVideoCallName(imAddFriendInfo.getVideoCallName());
        return imAddFriendInfoRef;
    }
}
