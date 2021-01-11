package com.hyphenate.chatuidemo.conference.utils;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.hyphenate.chatuidemo.Constant;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PhoneStateManager {
    public interface PhoneStateCallback {
        void onCallStateChanged(int state, String incomingNumber);
    }

    private static final String TAG = "PhoneStateManager";

    private static PhoneStateManager INSTANCE = null;

    private TelephonyManager telephonyManager;
    private List<PhoneStateCallback> stateCallbacks = new ArrayList<>();

//    public static PhoneStateManager get() {
//        if (INSTANCE == null) {
//            synchronized (PhoneStateManager.class) {
//                if (INSTANCE == null) {
//                    INSTANCE = new PhoneStateManager(Constant.application);
//                }
//            }
//        }
//        return INSTANCE;
//    }
    private static class PhoneStateManagerHolder {
        private static final PhoneStateManager INSTANCE = new PhoneStateManager();
    }

    private PhoneStateManager (){

    }

    public static final PhoneStateManager get() {
        Log.e("ConferenceActivity", "exit conference success 222");
        return PhoneStateManagerHolder.INSTANCE;
    }

    @Override
    protected void finalize() throws Throwable {
        if (telephonyManager != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
        super.finalize();
    }

    public void addStateCallback(PhoneStateCallback callback) {
        if (!stateCallbacks.contains(callback)) {
            stateCallbacks.add(callback);
        }
    }

    public void removeStateCallback(PhoneStateCallback callback) {
        Log.e("ConferenceActivity", "exit conference success 22");
        if (stateCallbacks.contains(callback)) {
            stateCallbacks.remove(callback);
        }
    }

    public PhoneStateManager(Context context) {
        Context appContext = Constant.application.getApplicationContext();

        telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }

        stateCallbacks = new CopyOnWriteArrayList<>();
    }

    private PhoneStateListener phoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);

            for (PhoneStateCallback callback : stateCallbacks) {
                callback.onCallStateChanged(state, incomingNumber);
            }
        }
    };
}
