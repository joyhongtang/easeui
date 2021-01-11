package com.hyphenate.chatuidemo.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.hyphenate.chatuidemo.Constant;
import com.hyphenate.easeui.R;

import static com.hyphenate.chatuidemo.conference.ConferenceActivity.KEY_CALL_SINGLE;
import static com.hyphenate.chatuidemo.conference.ConferenceActivity.KEY_CALL_TYPE;
import static com.hyphenate.chatuidemo.utils.ConferenceConfig.ConfigMachineType.DEVICE;
import static com.hyphenate.chatuidemo.utils.ConferenceConfig.ConfigMachineType.PHONE;

public class ConferenceConfig {
    public static ConfigMachineType machineType = DEVICE;
    public enum ConfigMachineType{
        PHONE,DEVICE
    }
    public static void setLockScreenStatus(Context appContext, String confId, String password, String inviter, String groupId, int callType, int singlecall){
    }
    public static void showNotification(Context context, Intent intent, String contentText) {
    }
}
