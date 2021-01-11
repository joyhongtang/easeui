package com.hyphenate.chatuidemo;

import android.content.Context;

import com.hyphenate.chatuidemo.entity.ImAddFriendInfoRef;

public interface HybernateChatInterface {
    void receiveConferenceCall(Context context, String conferenceId, String password, String inviter, String groupId);
    ImAddFriendInfoRef findImAddFriendByVideoName(String videoName);
}
