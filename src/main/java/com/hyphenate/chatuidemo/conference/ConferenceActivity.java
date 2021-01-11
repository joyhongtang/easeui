package com.hyphenate.chatuidemo.conference;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.blankj.utilcode.util.ToastUtils;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMConferenceListener;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConference;
import com.hyphenate.chat.EMConferenceAttribute;
import com.hyphenate.chat.EMConferenceManager;
import com.hyphenate.chat.EMConferenceMember;
import com.hyphenate.chat.EMConferenceStream;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMMirror;
import com.hyphenate.chat.EMStreamParam;
import com.hyphenate.chat.EMStreamStatistics;
import com.hyphenate.chat.EMWaterMarkOption;
import com.hyphenate.chat.EMWaterMarkPosition;
import com.hyphenate.chat.adapter.EMAError;
import com.hyphenate.chatuidemo.BaseApplication;
import com.hyphenate.chatuidemo.Constant;
import com.hyphenate.chatuidemo.DemoHelper;
import com.hyphenate.chatuidemo.HybernateChatInterface;
import com.hyphenate.chatuidemo.conference.utils.ActivityPageManager;
import com.hyphenate.chatuidemo.conference.utils.PhoneStateManager;
import com.hyphenate.chatuidemo.conference.widget.EasePageIndicator;
import com.hyphenate.chatuidemo.entity.ImAddFriendInfoRef;
import com.hyphenate.chatuidemo.entity.ImCallInfoRef;
import com.hyphenate.chatuidemo.ui.BaseActivity;
import com.hyphenate.chatuidemo.utils.ConferenceConfig;
import com.hyphenate.chatuidemo.utils.PreferenceManager;
import com.hyphenate.easeui.R;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.util.EMLog;
import com.hyphenate.util.EasyUtils;
import com.superrtc.mediamanager.ScreenCaptureManager;
import com.superrtc.sdk.VideoView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by lzan13 on 2017/8/15.
 * 多人音视频会议界面
 */
public class ConferenceActivity extends BaseActivity implements EMConferenceListener {
    private final String TAG = this.getClass().getSimpleName();

    private final int REQUEST_CODE_INVITE = 1001;
    private final int REQUEST_CODE_OVERLAY_PERMISSION = 1002;

    private ConferenceActivity activity;
    private EMConferenceListener conferenceListener;

    private AudioManager audioManager;

    private EMConference conference;
    private EMStreamParam normalParam;
    private EMStreamParam desktopParam;
    private boolean isCreator = false;
    private String confId = "";
    private String password = "";
    // 标识当前会议的创建方式
    private String groupId = null;

    // 正在显示音视频Window的stream
    private static EMConferenceStream windowStream;

    private List<EMConferenceStream> streamList = new ArrayList<>();

    private ConferenceMemberView localView;
    private IncomingCallView incomingCallView;
    private MemberViewGroup callConferenceViewGroup;

    private EasePageIndicator pageIndicator;
    // ------ tools panel relevant start ------
    // tools panel的父view
    private View toolsPanelView;
    // tools panel中显示会议成员名称的TextView
    private TextView membersTV;
    // tools panel中显示会议成员数量的TextView
    private TextView memberCountTV;
    // tools panel中显示时间的TextView
    private TextView callTimeView;
    // 麦克风开关
    private ImageButton micSwitch;
    // 摄像头开关
    private ImageButton cameraSwitch;
    // 话筒开关
    private ImageButton speakerSwitch;
    // 屏幕分享开关
    private ImageButton screenShareSwitch;
    // 前后摄像头切换
    private ImageButton changeCameraSwitch;
    // 挂断按钮
    private ImageButton hangupBtn;
    // 显示debug信息按钮
    private ImageButton debugBtn;
    // 邀请其他成员加入的按钮
    private ImageButton inviteBtn;
    // 全屏模式下改变视频显示模式的按钮,只在全屏模式下显示
    private ImageButton scaleModeBtn;
    // 显示悬浮窗的按钮
    private ImageButton closeBtn;
    // 退出全屏模式的按钮,只在全屏模式下显示
    private ImageButton zoominBtn;
    // ------ tools panel relevant end ------

    private DebugPanelView debugPanelView;

    // ------ full screen views start -------
    private View stateCoverMain;
    private View membersLayout;
    private TextView membersTVMain;
    private TextView memberCountTVMain;
    private TextView callTimeViewMain;
    private View talkingLayout;
    private ImageView talkingImage;
    private TextView talkerTV;
    // ------ full screen views end -------

    private HeadsetPlugReceiver headsetPlugReceiver;

    //水印显示bitmap
    private Bitmap watermarkbitmap;
    private EMWaterMarkOption watermark;
    //用于防止多次打开请求悬浮框页面
    private boolean requestOverlayPermission;

    public static final String KEY_MEMBERS_INFO = "KEY_MEMBERS_INFO";

    public static final String KEY_CALL_TYPE = "KEY_CALL_TYPE";
    public static final String KEY_CALL_SINGLE = "KEY_CALL_SINGLE";
    public static final String KEY_CALL_FROM = "KEY_CALL_FROM";
    /**
     * 聊天详情实体
     */
    private ImCallInfoRef imCallInfoRef;

    /**
     * 是否单聊入口
     */
    boolean isSingleCall = false;

    private Handler uiHandler;

    private final int MSG_SINGLE_CALL_NOTICE = 0x11;
    protected SoundPool soundPool;
    protected Ringtone ringtone;
    protected int outgoing, streamID;
    private SeekBar volumeSeekbar;

    /**
     * CHECK THE CONFERENCE WETHER EXIST OR NOT
     */
    private final int MSG_DETECT_CONFERENCE_STATUS = 0x11;
    private int errorTime = 0;

    private Thread conferenceThread;
    private boolean detectConference;
    /**
     * 用户点击退出会议，但是环信没有回调的时候，触发主动退出机制
     */
    private final int MSG_DESTORY_CONFERENCE_TIME_OUT = 0x12;
    // 如果groupId不为null,则表示呼叫类型为群组呼叫,显示的联系人只能是该群组中成员
    // 若groupId为null,则表示呼叫类型为联系人呼叫,显示的联系人为当前账号所有好友.
    public static void startConferenceCall(Context context, String groupId) {
        Intent i = new Intent(context, ConferenceActivity.class);
        i.putExtra(Constant.EXTRA_CONFERENCE_IS_CREATOR, true);
        i.putExtra(Constant.EXTRA_CONFERENCE_GROUP_ID, groupId);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    /**
     * @param context
     * @param groupId
     * @param members
     * @param calltype 通话类型 0 视频通话，1语音通话
     */
    public static void startConferenceCall(Context context, String groupId, ArrayList<ImAddFriendInfoRef> members, int calltype) {
        Intent i = new Intent(context, ConferenceActivity.class);
        i.putExtra(KEY_CALL_TYPE, calltype);
        i.putExtra(KEY_MEMBERS_INFO, members);
        i.putExtra(Constant.EXTRA_CONFERENCE_IS_CREATOR, true);
        i.putExtra(Constant.EXTRA_CONFERENCE_GROUP_ID, groupId);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    public static void startConferenceCall(Context context, String groupId, ArrayList<ImAddFriendInfoRef> members, int calltype, int singlecall) {
        Intent i = new Intent(context, ConferenceActivity.class);
        i.putExtra(KEY_CALL_TYPE, calltype);
        i.putExtra(KEY_CALL_SINGLE, singlecall);
        i.putExtra(KEY_MEMBERS_INFO, members);
        i.putExtra(Constant.EXTRA_CONFERENCE_IS_CREATOR, true);
        i.putExtra(Constant.EXTRA_CONFERENCE_GROUP_ID, groupId);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    public static void startConferenceCall(Context context, String groupId, ArrayList<ImAddFriendInfoRef> members, int calltype, int singlecall, int fromPhone) {
        Intent i = new Intent(context, ConferenceActivity.class);
        i.putExtra(KEY_CALL_TYPE, calltype);
        i.putExtra(KEY_CALL_SINGLE, singlecall);
        i.putExtra(KEY_CALL_FROM, fromPhone);
        i.putExtra(KEY_MEMBERS_INFO, members);
        i.putExtra(Constant.EXTRA_CONFERENCE_IS_CREATOR, true);
        i.putExtra(Constant.EXTRA_CONFERENCE_GROUP_ID, groupId);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    // 如果groupId不为null,则表示呼叫类型为群组呼叫,显示的联系人只能是该群组中成员
    // 若groupId为null,则表示呼叫类型为联系人呼叫,显示的联系人为当前账号所有好友.
    public static void receiveConferenceCall(Context context, String conferenceId, String password, String inviter, String groupId) {
        receiveConferenceCall(context, conferenceId, password, inviter, groupId, 0);
    }

    public static void receiveConferenceCall(Context context, String conferenceId, String password, String inviter, String groupId, int callType) {
        receiveConferenceCall(context, conferenceId, password, inviter, groupId, callType, 0);
    }

    public static void receiveConferenceCall(Context context, String conferenceId, String password, String inviter, String groupId, int callType, int singlecall) {
        Intent i = new Intent(context, ConferenceActivity.class);
        i.putExtra(Constant.EXTRA_CONFERENCE_ID, conferenceId);
        i.putExtra(Constant.EXTRA_CONFERENCE_PASS, password);
        i.putExtra(Constant.EXTRA_CONFERENCE_INVITER, inviter);
        i.putExtra(Constant.EXTRA_CONFERENCE_IS_CREATOR, false);
        i.putExtra(Constant.EXTRA_CONFERENCE_GROUP_ID, groupId);
        ArrayList<ImAddFriendInfoRef> members = new ArrayList<>();
        ImAddFriendInfoRef imAddFriendInfoRef = new ImAddFriendInfoRef();
        imAddFriendInfoRef.setVideoCallName(inviter);
        members.add(imAddFriendInfoRef);
        i.putExtra(KEY_MEMBERS_INFO, members);
        i.putExtra(KEY_CALL_TYPE, callType);
        i.putExtra(KEY_CALL_SINGLE, singlecall);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    @Override
    public int initLayout() {
        setShowTopBar(false);
        if (getIntent().getIntExtra(KEY_CALL_TYPE, -1) == 1) {
            return R.layout.activity_conference_voice;
        } else {
            return R.layout.activity_conference;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        checkSaveInstanceState(savedInstanceState);
    }

    @Override
    public void initData() {
        Log.i(TAG, "onCreate stopTimeRecord first");
        stopTimeRecord();
        //空会议退出
        isCreator = getIntent().getBooleanExtra(Constant.EXTRA_CONFERENCE_IS_CREATOR, false);
        if (TextUtils.isEmpty(confId) && !isCreator) {
            String confId = getIntent().getStringExtra(Constant.EXTRA_CONFERENCE_ID);
            String inviter = getIntent().getStringExtra(Constant.EXTRA_CONFERENCE_INVITER);
            if (TextUtils.isEmpty(confId) || TextUtils.isEmpty(inviter)) {
                finish();
                return;
            }
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_FULLSCREEN);
        imCallInfoRef = new ImCallInfoRef();
        uiHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MSG_SINGLE_CALL_NOTICE) {
                    destroyConference();
                }else if(msg.what == MSG_DESTORY_CONFERENCE_TIME_OUT){
                    if(!isFinishing()){
                        finish();
                    }
                }
                else if (msg.what == MSG_DETECT_CONFERENCE_STATUS) {
                    if (null != conference && DemoHelper.getInstance().destoryConferenceList.containsKey(conference.getConferenceId())) {
                        if (!isFinishing()) {
                            finish();
                            return;
                        }
                    }
                    try {
                        EMClient.getInstance().conferenceManager().getConferenceInfo(confId, password, new EMValueCallBack() {
                            @Override
                            public void onSuccess(Object o) {
                                errorTime = 0;
                                Log.i(TAG, String.valueOf(o));
                            }

                            @Override
                            public void onError(int i, String s) {
                                Log.i(TAG, String.valueOf(s));
                                if (i == EMAError.CALL_CONFERENCE_NO_EXIST) {
                                    //在用户未接听来电的情况下，直接退出，不进行容错处理
                                    if (!pickUp) {
                                        if (!isFinishing()) {
                                            finish();
                                        }
                                    } else {
//                                        if (errorTime > 1) {
                                        if (!isFinishing()) {
                                            finish();
                                        }
//                                        }
                                    }
                                }
                                errorTime++;
                            }
                        });
                        try {
                            Thread.sleep(6000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    ConferenceMemberView removeMemberExpire = null;
                    String streamID = String.valueOf(msg.obj);
                    for (int i = 0; i < callConferenceViewGroup.getChildCount(); i++) {
                        final ConferenceMemberView memberView = (ConferenceMemberView) callConferenceViewGroup.getChildAt(i);
                        if (TextUtils.equals(streamID, memberView.getStreamId())) {
                            removeMemberExpire = memberView;
                            break;
                        }
                    }
                    if (null != removeMemberExpire) {
                        callConferenceViewGroup.removeView(removeMemberExpire);
                        if (callConferenceViewGroup.getChildCount() <= 1) {
                            exitConference();
                        }
                    }
                }
            }
        };
        if (getIntent().getIntExtra(KEY_CALL_SINGLE, 0) == 1) {
            isSingleCall = true;
        }
        init();

        EMClient.getInstance().conferenceManager().addConferenceListener(conferenceListener);
        DemoHelper.getInstance().pushActivity(activity);

        //注册耳机插拔事件
        registerHeadsetPlugReceiver();
        if (isSingleCall) {
        }
    }

    @Override
    public void initListener() {

    }

    /**
     * 监听耳机插入 插出的事件
     */
    public class HeadsetPlugReceiver extends BroadcastReceiver {
        private static final String TAG = "HeadsetPlugReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("state")) {
                if (intent.getIntExtra("state", 0) == 0) {
                    EMLog.i(TAG, "HeadsetPlugReceiver:  headset not connected");
                    if (audioManager != null) {
                        if (!audioManager.isSpeakerphoneOn()) {
                            // 打开扬声器
                            audioManager.setSpeakerphoneOn(true);
                        }
                        // 开启了扬声器之后，因为是进行通话，声音的模式也要设置成通讯模式
                        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                    }
                } else if (intent.getIntExtra("state", 0) == 1) {
                    EMLog.i(TAG, "HeadsetPlugReceiver:  headset connected");
                    if (audioManager != null) {
                        if (audioManager.isSpeakerphoneOn()) {
                            // 关闭扬声器
                            audioManager.setSpeakerphoneOn(false);
                        }
                        // 设置声音模式为通讯模式
                        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                    }
                }
            }
        }
    }


    private void checkSaveInstanceState(Bundle bundle) {
        if (bundle != null) {
            confId = bundle.getString(Constant.EXTRA_CONFERENCE_ID);
            password = bundle.getString(Constant.EXTRA_CONFERENCE_PASS);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // NOTE: 下方逻辑必须放在当前方法中,放到onStart()或onResume()中当在Android 6.0以上设备中显示悬浮窗时会有问题

        // 当前Activity的launch mode为SingleTask,且有独立的activity任务栈,start当前activity时如果当前activity已经存在
        // 于当前activity所需的任务栈中,则当前activity#onCreate()方法不会被调用,Activity#onNewIntent()会被调用.
        // 当前Activity启动其他activity,若其他activity#finish()又回到当前activity,则onNewIntent()不会被调用.

        try {
            if (windowStream != null) {
                // 从window状态进入activity.
                if (!windowStream.isVideoOff()) {
                    boolean isSelf = windowStream.getUsername().equals(EMClient.getInstance().getCurrentUser());
                    if (isSelf) {
                        EMClient.getInstance().conferenceManager().updateLocalSurfaceView(localView.getSurfaceView());
                    } else {
                        EMClient.getInstance().conferenceManager().updateRemoteSurfaceView(windowStream.getStreamId(),
                                ((ConferenceMemberView) callConferenceViewGroup.getChildAt(1)).getSurfaceView());
                    }
                }
            }

            windowStream = null;

            // 防止activity在后台被start至前台导致window还存在
            CallFloatWindow.getInstance(getApplicationContext()).dismiss();
            DeskShareWindow.getInstance(getApplicationContext()).dismiss();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        EMLog.i(TAG, "onResume: ");
        super.onResume();
        // 注册home-key receiver
        registerHomeKeyWatcher();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 注销home-key receiver
        unregisterHomeKeyWatcher();
    }

    private int MAX_VOICE_CALL_VOLUME;

    /**
     * 初始化
     */
    private void init() {
        activity = this;

        incomingCallView = (IncomingCallView) findViewById(R.id.incoming_call_view);
        callConferenceViewGroup = (MemberViewGroup) findViewById(R.id.surface_view_group);
        volumeSeekbar = findViewById(R.id.volume);
        toolsPanelView = findViewById(R.id.layout_tools_panel);

        inviteBtn = (ImageButton) findViewById(R.id.btn_invite);
        membersTV = (TextView) findViewById(R.id.tv_members);
        memberCountTV = (TextView) findViewById(R.id.tv_member_count);
        callTimeView = (TextView) findViewById(R.id.tv_call_time);
        micSwitch = (ImageButton) findViewById(R.id.btn_mic_switch);
        cameraSwitch = (ImageButton) findViewById(R.id.btn_camera_switch);
        speakerSwitch = (ImageButton) findViewById(R.id.btn_speaker_switch);
        screenShareSwitch = (ImageButton) findViewById(R.id.btn_desk_share);
        changeCameraSwitch = (ImageButton) findViewById(R.id.btn_change_camera_switch);
        hangupBtn = (ImageButton) findViewById(R.id.btn_hangup);
        debugBtn = (ImageButton) findViewById(R.id.btn_debug);
        scaleModeBtn = (ImageButton) findViewById(R.id.btn_scale_mode);
        closeBtn = (ImageButton) findViewById(R.id.btn_close);
        zoominBtn = (ImageButton) findViewById(R.id.btn_zoomin);

        pageIndicator = (EasePageIndicator) findViewById(R.id.indicator);

        debugPanelView = (DebugPanelView) findViewById(R.id.layout_debug_panel);

        stateCoverMain = findViewById(R.id.state_cover_main);
        membersLayout = findViewById(R.id.layout_members);
        membersTVMain = (TextView) findViewById(R.id.tv_members_main);
        memberCountTVMain = (TextView) findViewById(R.id.tv_member_count_main);
        callTimeViewMain = (TextView) findViewById(R.id.tv_call_time_main);
        talkingLayout = findViewById(R.id.layout_talking);
        talkingImage = (ImageView) findViewById(R.id.icon_talking);
        talkerTV = (TextView) findViewById(R.id.tv_talker);

        incomingCallView.setOnActionListener(onActionListener);
        callConferenceViewGroup.setOnItemClickListener(onItemClickListener);
        callConferenceViewGroup.setOnScreenModeChangeListener(onScreenModeChangeListener);
        callConferenceViewGroup.setOnPageStatusListener(onPageStatusListener);
        inviteBtn.setOnClickListener(listener);
        micSwitch.setOnClickListener(listener);
        speakerSwitch.setOnClickListener(listener);
        cameraSwitch.setOnClickListener(listener);
        screenShareSwitch.setOnClickListener(listener);
        changeCameraSwitch.setOnClickListener(listener);
        hangupBtn.setOnClickListener(listener);
        debugBtn.setOnClickListener(listener);
        scaleModeBtn.setOnClickListener(listener);
        closeBtn.setOnClickListener(listener);
        zoominBtn.setOnClickListener(listener);

        debugPanelView.setOnButtonClickListener(onButtonClickListener);

        conferenceListener = this;
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        MAX_VOICE_CALL_VOLUME = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
        if (null != volumeSeekbar)
            volumeSeekbar.setMax(MAX_VOICE_CALL_VOLUME);
        //设置通话音量
        if (ConferenceConfig.machineType == ConferenceConfig.ConfigMachineType.DEVICE) {
            audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, volume, 0);
            if (null != volumeSeekbar)
                volumeSeekbar.setProgress(volume);
        }
        requestMediaFocus();

        normalParam = new EMStreamParam();
        normalParam.setStreamType(EMConferenceStream.StreamType.NORMAL);

        if (getIntent().getIntExtra(KEY_CALL_TYPE, -1) == 1) {
            normalParam.setVideoOff(true);
        } else {
            normalParam.setVideoOff(false);
        }
        normalParam.setAudioOff(false);

        desktopParam = new EMStreamParam();
        desktopParam.setAudioOff(true);
        desktopParam.setVideoOff(true);
        desktopParam.setStreamType(EMConferenceStream.StreamType.DESKTOP);

        micSwitch.setActivated(normalParam.isAudioOff());
        cameraSwitch.setActivated(normalParam.isVideoOff());
        speakerSwitch.setActivated(true);
        openSpeaker();
        ArrayList<ImAddFriendInfoRef> membersList = getIntent().getParcelableArrayListExtra(KEY_MEMBERS_INFO);
        allMembersList.addAll(membersList);
        if (!TextUtils.isEmpty(confId)) {
            initLocalConferenceView();
            joinConference();
        } else {
            groupId = getIntent().getStringExtra(Constant.EXTRA_CONFERENCE_GROUP_ID);
            isCreator = getIntent().getBooleanExtra(Constant.EXTRA_CONFERENCE_IS_CREATOR, false);
            if (isCreator) {

                soundPool = new SoundPool(1, AudioManager.STREAM_RING, 0);
                outgoing = soundPool.load(this, R.raw.em_outgoing, 1);
                uiHandler.postDelayed(new Runnable() {
                    public void run() {
                        streamID = playMakeCallSounds();
                    }
                }, 300);


                incomingCallView.setVisibility(View.GONE);
                String[] members = new String[membersList.size()];
                for (int i = 0; i < membersList.size(); i++) {
                    members[i] = membersList.get(i).getVideoCallName();
                }
                if (isCreator && conference == null) {
                    initLocalConferenceView();

                    createAndJoinConference(new EMValueCallBack<EMConference>() {
                        @Override
                        public void onSuccess(EMConference value) {
                            if (isFinishing()) {
                                try {
                                    if(!hasExcutedDestoryConference){
                                        hasExcutedDestoryConference = true;
                                        EMClient.getInstance().conferenceManager().destroyConference(new EMValueCallBack() {
                                            @Override
                                            public void onSuccess(Object o) {
                                            }

                                            @Override
                                            public void onError(int i, String s) {
                                            }
                                        });
                                        return;
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();

                                }
                            } else
                                inviteUserToJoinConference(members);
                        }

                        @Override
                        public void onError(int error, String errorMsg) {

                        }
                    });
                } else {
                    inviteUserToJoinConference(members);
                }
            } else {
                confId = getIntent().getStringExtra(Constant.EXTRA_CONFERENCE_ID);
                String inviter = getIntent().getStringExtra(Constant.EXTRA_CONFERENCE_INVITER);
                if (TextUtils.isEmpty(confId) || TextUtils.isEmpty(inviter)) {
                    finish();
                    return;
                }
                Uri ringUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                audioManager.setMode(AudioManager.MODE_RINGTONE);
                audioManager.setSpeakerphoneOn(true);
                ringtone = RingtoneManager.getRingtone(this, ringUri);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P || ConferenceConfig.machineType == ConferenceConfig.ConfigMachineType.DEVICE) {
//                    ringtone.setLooping(true);
                }
                ringtone.play();
                password = getIntent().getStringExtra(Constant.EXTRA_CONFERENCE_PASS);

                initLocalConferenceView();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            HybernateChatInterface h = (HybernateChatInterface) getApplication();
                            ImAddFriendInfoRef imAddFriendInfoRef = h.findImAddFriendByVideoName(inviter);
                            if (null != imAddFriendInfoRef) {
                                String tempjoinedMember = imAddFriendInfoRef.getNickerName();
                                if (!TextUtils.isEmpty(tempjoinedMember)) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            incomingCallView.setInviteInfo(String.format(getString(R.string.tips_invite_to_join), tempjoinedMember));
                                            incomingCallView.setVisibility(View.VISIBLE);
                                        }
                                    });
                                } else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            incomingCallView.setInviteInfo(String.format(getString(R.string.tips_invite_to_join), inviter));
                                            incomingCallView.setVisibility(View.VISIBLE);
                                        }
                                    });

                                }
                            }
                            //添加聊天对象
                        } catch (Exception e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    incomingCallView.setInviteInfo(String.format(getString(R.string.tips_invite_to_join), inviter));
                                    incomingCallView.setVisibility(View.VISIBLE);
                                }
                            });
                        } finally {

                        }
                    }
                }).start();

            }
        }


        //水印初始化
        if (PreferenceManager.getInstance().isWatermarkResolution()) {
            try {
                InputStream in = this.getResources().getAssets().open("watermark.png");
                watermarkbitmap = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            }
            watermark = new EMWaterMarkOption(watermarkbitmap, 75, 25, EMWaterMarkPosition.TOP_RIGHT, 8, 8);
        }

        if (!isCreator) {
            detectConference = true;
            if (null == conferenceThread) {
                conferenceThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (detectConference) {
                            if (null != conference && DemoHelper.getInstance().destoryConferenceList.containsKey(conference.getConferenceId())) {
                                if (!isFinishing()) {
                                    finish();
                                    return;
                                }
                            }
                            try {
                                EMClient.getInstance().conferenceManager().getConferenceInfo(confId, password, new EMValueCallBack() {
                                    @Override
                                    public void onSuccess(Object o) {
                                        errorTime = 0;
                                        Log.i(TAG, String.valueOf(o));
                                    }

                                    @Override
                                    public void onError(int i, String s) {
                                        Log.i(TAG, String.valueOf(s));
                                        if (i == EMAError.CALL_CONFERENCE_NO_EXIST) {
                                            //在用户未接听来电的情况下，直接退出，不进行容错处理
                                            if (!pickUp) {
                                                if (!isFinishing()) {
                                                    finish();
                                                }
                                            } else {
                                                if (errorTime > 1) {
                                                    if (!isFinishing()) {
                                                        finish();
                                                    }
                                                }
                                            }
                                        }
                                        errorTime++;
                                    }
                                });
                                try {
                                    Thread.sleep(6000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    }
                });
//                conferenceThread.start();
            }
        }
    }

    /**
     * play the incoming call ringtone
     */
    protected int playMakeCallSounds() {
        try {
            audioManager.setMode(AudioManager.MODE_RINGTONE);
            audioManager.setSpeakerphoneOn(true);

            // play
            int id = soundPool.play(outgoing, // sound resource
                    0.3f, // left volume
                    0.3f, // right volume
                    1,    // priority
                    -1,   // loop，0 is no loop，-1 is loop forever
                    1);   // playback rate (1.0 = normal playback, range 0.5 to 2.0)
            return id;
        } catch (Exception e) {
            return -1;
        }
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view == findViewById(R.id.btn_invite)) {
                if (!createConferenceSuccess) {
                    ToastUtils.showLong(getResources().getString(R.string.add_friend_notice));
                    return;
                }
                selectUserToJoinConference();
            } else if (view == findViewById(R.id.btn_mic_switch)) {
                voiceSwitch();
            } else if (view == findViewById(R.id.btn_speaker_switch)) {
                if (speakerSwitch.isActivated()) {
                    closeSpeaker();
                } else {
                    openSpeaker();
                }
            } else if (view == findViewById(R.id.btn_camera_switch)) {
                videoSwitch();
            } else if (view == findViewById(R.id.btn_desk_share)) {
                if (screenShareSwitch.isActivated()) {
                    screenShareSwitch.setActivated(false);
                    unpublish(conference.getPubStreamId(EMConferenceStream.StreamType.DESKTOP));
                } else {
                    screenShareSwitch.setActivated(true);
                    publishDesktop();
                }
            } else if (view == findViewById(R.id.btn_change_camera_switch)) {
                changeCamera();
            } else if (view == findViewById(R.id.btn_hangup)) {
                exitConference();
                uiHandler.sendEmptyMessageDelayed(MSG_DESTORY_CONFERENCE_TIME_OUT,8*1000);
            } else if (view == findViewById(R.id.btn_debug)) {
                EMLog.i(TAG, "Button debug clicked!!!");
                EMClient.getInstance().conferenceManager().enableStatistics(true);
                openDebugPanel();
            } else if (view == findViewById(R.id.btn_scale_mode)) {
                changeFullScreenScaleMode();
            } else if (view == findViewById(R.id.btn_close)) {
                showFloatWindow();
            } else if (view == findViewById(R.id.btn_zoomin)) {
                callConferenceViewGroup.performClick(100, 100);
            }
        }
    };

    /**
     * 该用户是否接听来电
     */
    private boolean pickUp = false;
    private boolean isRejectSingleCall = false;
    private IncomingCallView.OnActionListener onActionListener = new IncomingCallView.OnActionListener() {
        @Override
        public void onPickupClick(View v) {
            pickUp = true;
            incomingCallView.setVisibility(View.GONE);
            releaseBackGroundStone();
            joinConference();
        }

        @Override
        public void onRejectClick(View v) {
            if (isSingleCall) {
                isRejectSingleCall = true;
                createRejectMsg(true);
//                joinConference();
            } else {
                if (!isFinishing())
                    finish();
            }
        }
    };

    private void createRejectMsg(boolean destroyConference) {
        try {
            ArrayList<ImAddFriendInfoRef> membersList = allMembersList;
            ArrayList<EMMessage> emMessages = new ArrayList<>();
            for (ImAddFriendInfoRef members : membersList) {
                final EMMessage message = EMMessage.createTxtSendMessage("You have an incoming call", members.getVideoCallName());
                if (destroyConference) {
                    if (null != conference) {
                        message.setAttribute("destroy_conference_id", conference.getConferenceId());
                    }
                }
                // set the user-defined extension field
                message.setAttribute("reject_call", true);
                message.setAttribute("is_voice_call", (getIntent().getIntExtra(KEY_CALL_TYPE, -1) == 1 ? true : false));
                // 扩展字段对于音视频会议不是必须的,只是增加了额外用于显示或者判断音视频会议类型的信息.
                message.setAttribute(Constant.MSG_ATTR_EXTENSION, "destroy_conference_id");
                message.setMessageStatusCallback(new EMCallBack() {

                    @Override
                    public void onSuccess() {
                        Log.i(TAG, "onRemoteOffline success");
                        exitConference();
                    }

                    @Override
                    public void onError(int code, String error) {
                        Log.i(TAG, "onRemoteOffline Error");
                        exitConference();
                    }

                    @Override
                    public void onProgress(int progress, String status) {
                    }
                });
                emMessages.add(message);
            }

            // send messages
            for (EMMessage emMessage : emMessages) {
                EMClient.getInstance().chatManager().sendMessage(emMessage);
            }

        } catch (Exception e) {
            exitConference();
            e.printStackTrace();
        }
    }

    private void createRejectMsg() {
        createRejectMsg(false);
    }

    /**
     * 初始化多人音视频画面管理控件
     */
    private void initLocalConferenceView() {
        localView = new ConferenceMemberView(activity);
        if (getIntent().getIntExtra(KEY_CALL_TYPE, -1) == 1) {
            changeCameraSwitch.setVisibility(View.GONE);
            localView.setVideoOff(true);
            shutDownVideo();
        } else {
            localView.setVideoOff(normalParam.isVideoOff());
        }

        localView.setAudioOff(normalParam.isAudioOff());
        localView.setUsername(EMClient.getInstance().getCurrentUser());
        if (getIntent().getIntExtra(KEY_CALL_TYPE, -1) == 1) {
            localView.setDefaultBackgroundView(R.drawable.em_call_mic_on);
        }

        EMClient.getInstance().conferenceManager().setLocalSurfaceView(localView.getSurfaceView());

        callConferenceViewGroup.addView(localView);
    }

    /**
     * 添加一个展示远端画面的 view
     */
    private void addConferenceView(EMConferenceStream stream) {
        EMLog.d(TAG, "add conference view -start- " + stream.getMemberName());
        streamList.add(stream);
        final ConferenceMemberView memberView = new ConferenceMemberView(activity);
        callConferenceViewGroup.addView(memberView);
        String nickerName = stream.getUsername();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String joinedMember = stream.getUsername();
                    HybernateChatInterface h = (HybernateChatInterface) getApplication();
                    ImAddFriendInfoRef imAddFriendInfoRef = h.findImAddFriendByVideoName(joinedMember);
                    if (null != imAddFriendInfoRef) {
                        String tempjoinedMember = imAddFriendInfoRef.getNickerName();
                        if (!TextUtils.isEmpty(tempjoinedMember)) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    memberView.setUsername(tempjoinedMember);
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    memberView.setUsername(nickerName);
                                }
                            });
                        }
                    }
                    //添加聊天对象
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            memberView.setUsername(nickerName);
                        }
                    });
                } finally {

                }
            }
        }).start();
        memberView.setStreamId(stream.getStreamId());
        memberView.setAudioOff(stream.isAudioOff());
        memberView.setVideoOff(stream.isVideoOff());
        if (getIntent().getIntExtra(KEY_CALL_TYPE, -1) == 1) {
            memberView.setDefaultBackgroundView(R.drawable.em_call_mic_on);
        }
        memberView.setDesktop(stream.getStreamType() == EMConferenceStream.StreamType.DESKTOP);
        subscribe(stream, memberView);
        EMLog.d(TAG, "add conference view -end-" + stream.getMemberName());
        debugPanelView.setStreamListAndNotify(streamList);
    }

    private DebugPanelView.OnButtonClickListener onButtonClickListener = new DebugPanelView.OnButtonClickListener() {
        @Override
        public void onCloseClick(View v) {
            EMClient.getInstance().conferenceManager().enableStatistics(false);
            openToolsPanel();
        }
    };

    private MemberViewGroup.OnItemClickListener onItemClickListener = new MemberViewGroup.OnItemClickListener() {
        @Override
        public void onItemClick(View v, int position) {
        }
    };

    private MemberViewGroup.OnScreenModeChangeListener onScreenModeChangeListener = new MemberViewGroup.OnScreenModeChangeListener() {
        @Override
        public void onScreenModeChange(boolean isFullScreenMode, @Nullable View fullScreenView) {
            View layer1 = findViewById(R.id.layer1);
            View layer2 = findViewById(R.id.layer2);
            if (isFullScreenMode) { // 全屏模式
                if (ConferenceConfig.machineType == ConferenceConfig.ConfigMachineType.DEVICE) {
                    toolsPanelView.setBackgroundColor(getResources().getColor(R.color.bg_tools_panel));
                    layer1.setVisibility(View.VISIBLE);
                    layer2.setVisibility(View.VISIBLE);
                } else {
                    toolsPanelView.setBackgroundColor(getResources().getColor(R.color.color_transparent));
                    layer1.setVisibility(View.INVISIBLE);
                    layer2.setVisibility(View.INVISIBLE);
                }

                membersTV.setVisibility(View.INVISIBLE);
                memberCountTV.setVisibility(View.INVISIBLE);
                callTimeView.setVisibility(View.INVISIBLE);

                stateCoverMain.setVisibility(View.VISIBLE);
                membersLayout.setVisibility(View.VISIBLE);
                talkingLayout.setVisibility(View.VISIBLE);
                callTimeViewMain.setVisibility(View.VISIBLE);

                scaleModeBtn.setVisibility(View.INVISIBLE);
                closeBtn.setVisibility(View.GONE);
                zoominBtn.setVisibility(View.VISIBLE);
            } else { // 非全屏模式
                layer1.setVisibility(View.VISIBLE);
                layer2.setVisibility(View.VISIBLE);
                toolsPanelView.setBackgroundColor(getResources().getColor(R.color.bg_tools_panel));

                membersTV.setVisibility(View.VISIBLE);
                memberCountTV.setVisibility(View.VISIBLE);
                callTimeView.setVisibility(View.VISIBLE);

                scaleModeBtn.setVisibility(View.INVISIBLE);
                closeBtn.setVisibility(View.VISIBLE);
                zoominBtn.setVisibility(View.GONE);

                // invisible the full-screen mode views.
                stateCoverMain.setVisibility(View.GONE);
                membersLayout.setVisibility(View.GONE);
                talkingLayout.setVisibility(View.GONE);
                callTimeViewMain.setVisibility(View.GONE);
            }
        }
    };

    private MemberViewGroup.OnPageStatusListener onPageStatusListener = new MemberViewGroup.OnPageStatusListener() {
        @Override
        public void onPageCountChange(int count) {
            // 多于1页时显示indicator.
            pageIndicator.setup(count > 1 ? count : 0);
        }

        @Override
        public void onPageScroll(int page) {
            pageIndicator.setItemChecked(page);
        }
    };

    /**
     * 移除指定位置的 View，移除时如果已经订阅需要取消订阅
     */
    private void removeConferenceView(EMConferenceStream stream) {
        int index = streamList.indexOf(stream);
        final ConferenceMemberView memberView = (ConferenceMemberView) callConferenceViewGroup.getChildAt(index);
        streamList.remove(stream);
        callConferenceViewGroup.removeView(memberView);
        debugPanelView.setStreamListAndNotify(streamList);
    }

    /**
     * 更新指定 View
     */
    private void updateConferenceMemberView(EMConferenceStream stream) {
        int position = streamList.indexOf(stream);
        ConferenceMemberView conferenceMemberView = (ConferenceMemberView) callConferenceViewGroup.getChildAt(position);
        conferenceMemberView.setAudioOff(stream.isAudioOff());
        conferenceMemberView.setVideoOff(stream.isVideoOff());

        // 悬浮窗显示规则: 若有其他成员加入会议,则显示第一个加入会议的其他成员;若无,则显示自己.
        if (position != 1) {
            return;
        }
        CallFloatWindow.getInstance(getApplicationContext()).update(stream);
    }

    /**
     * 更新当前说话者
     */
    private void currSpeakers(List<String> speakers) {
        for (int i = 0; i < callConferenceViewGroup.getChildCount(); i++) {
            if (talkingLayout.getVisibility() == View.VISIBLE) {
                // full screen mode.
                if (speakers.size() == 0) {
                    talkingImage.setVisibility(View.GONE);
                    talkerTV.setText("");
                } else {
                    talkingImage.setVisibility(View.VISIBLE);
                    String lastStreamId = speakers.get(speakers.size() - 1);
                    EMLog.i("currSpeakers", "currSpeakers: " + lastStreamId);
                    String speaker = null;
                    for (EMConferenceStream stream : streamList) {
                        EMLog.i("currSpeakers", "stream: " + stream.getStreamId());
                        if (stream.getStreamId().equals(lastStreamId)) {
                            speaker = stream.getUsername();
                            break;
                        }
                    }
                    talkerTV.setText(speaker);
                }
            }

            ConferenceMemberView view = (ConferenceMemberView) callConferenceViewGroup.getChildAt(i);
            view.setTalking(speakers.contains(view.getStreamId()));
        }
    }

    private boolean createConferenceSuccess = false;

    /**
     * 作为创建者创建并加入会议
     */
    private void createAndJoinConference(final EMValueCallBack<EMConference> callBack) {
        boolean record = PreferenceManager.getInstance().isRecordOnServer();
        boolean merge = PreferenceManager.getInstance().isMergeStream();

        EMClient.getInstance().conferenceManager().createAndJoinConference(EMConferenceManager.EMConferenceType.SmallCommunication,
                password, true, record, merge, new EMValueCallBack<EMConference>() {
                    @Override
                    public void onSuccess(final EMConference value) {
                        createConferenceSuccess = true;
                        EMLog.e(TAG, "create and join conference success");
                        conference = value;
                        startAudioTalkingMonitor();
                        publish();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(activity, getResources().getString(R.string.create_and_join_success), Toast.LENGTH_SHORT).show();
                                if (callBack != null) {
                                    callBack.onSuccess(value);
                                }
                            }
                        });
                        //单聊开始检查通话是否接通，超过15s默认关闭会议
                        if (isSingleCall) {
                            uiHandler.sendEmptyMessageDelayed(MSG_SINGLE_CALL_NOTICE, 60 * 1000);
                        }
                    }

                    @Override
                    public void onError(final int error, final String errorMsg) {
                        EMLog.e(TAG, "Create and join conference failed errorcode == " + error + " errorMsg == " + errorMsg);
                        if(errorMsg.contains("without im account login")){
                            Constant.login_im_success = false;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(activity, getResources().getString(R.string.create_and_join_fail), Toast.LENGTH_LONG).show();
                                if (callBack != null) {
                                    callBack.onError(error, errorMsg);
                                }
                                if (error == EMAError.CALL_ALREADY_JOIN) {
                                    createConferenceSuccess = true;
                                    exitConference();
                                } else {
                                    if (!isFinishing())
                                        finish();
                                }
                            }
                        });
                    }
                });
    }

    /**
     * 作为成员直接根据 confId 和 password 加入会议
     */
    private void joinConference() {
        hangupBtn.setVisibility(View.VISIBLE);
        EMClient.getInstance().conferenceManager().joinConference(confId, password, new EMValueCallBack<EMConference>() {
            @Override
            public void onSuccess(EMConference value) {
                createConferenceSuccess = true;
                if (isRejectSingleCall) {
                    try {
                        EMClient.getInstance().conferenceManager().exitConference(new EMValueCallBack() {
                            @Override
                            public void onSuccess(Object value) {
                                //如果启动外部音频输入 停止音频录制
                                if (PreferenceManager.getInstance().isExternalAudioInputResolution()) {
                                    ExternalAudioInputRecord.getInstance().stopRecording();
                                }
                                if (!isFinishing())
                                    finish();
                                //start MainActivity
                            }

                            @Override
                            public void onError(int error, String errorMsg) {
                                EMLog.e(TAG, "exit conference failed " + error + ", " + errorMsg);
                                if (!isFinishing())
                                    finish();

                            }
                        });
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        if (!isFinishing())
                            finish();
                    }
                    return;
                }

                conference = value;
                startAudioTalkingMonitor();
                publish();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity, "Join conference success", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(final int error, final String errorMsg) {
                EMLog.e(TAG, "join conference failed error");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity, "Join conference failed ", Toast.LENGTH_SHORT).show();
                    }
                });
                if (error == EMAError.CALL_ALREADY_JOIN) {
                    createConferenceSuccess = true;
                    exitConference();
                } else {
                    if (!isFinishing())
                        finish();
                }
            }
        });
    }

    private void releaseMediaFocus() {
        if (null != audioManager)
            audioManager.abandonAudioFocus(afChangeListener);
    }

    private void requestMediaFocus() {
        if (null != audioManager) {
            // Request audio focus for playback
            int result = audioManager.requestAudioFocus(afChangeListener,
                    // Use the music stream.
                    AudioManager.STREAM_MUSIC,
                    // Request permanent focus.
                    AudioManager.AUDIOFOCUS_GAIN);
        }
    }

    AudioManager.OnAudioFocusChangeListener afChangeListener =
            new AudioManager.OnAudioFocusChangeListener() {
                public void onAudioFocusChange(int focusChange) {
                    if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                        // Permanent loss of audio focus
                    } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                        // Pause playback
                    } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                        // Lower the volume, keep playing
                    } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                        // Your app has been granted audio focus again
                        // Raise volume to normal, restart playback if necessary
                    }
                }
            };
    ArrayList<ImAddFriendInfoRef> allMembersList = new ArrayList<>();

    /**
     * 邀请他人加入会议
     */
    private void selectUserToJoinConference() {
        List<EMConferenceMember> emConferenceMemberList = EMClient.getInstance().conferenceManager().getConferenceMemberList();
        if (null != emConferenceMemberList && emConferenceMemberList.size() >= 4) {
            ToastUtils.showLong(getResources().getString(R.string.im_max_conference));
            return;
        }
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ArrayList<ImAddFriendInfoRef> membersList = getIntent().getParcelableArrayListExtra(KEY_MEMBERS_INFO);
        intent.putExtra(KEY_MEMBERS_INFO, membersList);
        intent.setClassName("com.idwell.cloudframe", "com.idwell.cloudframe.ui.tab.fragments.ConferenceInviteActivity");
        intent.putExtra(Constant.EXTRA_CONFERENCE_GROUP_ID, groupId);
        activity.startActivityForResult(intent, REQUEST_CODE_INVITE);
    }

    private void inviteUserToJoinConference(String[] contacts) {
        if (isFinishing()) {
            return;
        }
        try {
            JSONObject object = new JSONObject();
            object.put(Constant.EXTRA_CONFERENCE_INVITER, EMClient.getInstance().getCurrentUser());
            object.put(Constant.EXTRA_CONFERENCE_GROUP_ID, groupId);
            object.put(KEY_CALL_TYPE, getIntent().getIntExtra(KEY_CALL_TYPE, -1));
            object.put(KEY_CALL_SINGLE, getIntent().getIntExtra(KEY_CALL_SINGLE, 0));
            for (int i = 0; i < contacts.length; i++) {
                // 通过消息的方式邀请对方加入
                sendInviteMessage(contacts[i], object.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(String[] event) {
        final String[] members = event;
        if (isCreator && conference == null) {
            initLocalConferenceView();

            createAndJoinConference(new EMValueCallBack<EMConference>() {
                @Override
                public void onSuccess(EMConference value) {
                    if (isFinishing()) {
                        if(!hasExcutedDestoryConference){
                            hasExcutedDestoryConference = true;
                            try {
                                EMClient.getInstance().conferenceManager().destroyConference(new EMValueCallBack() {
                                    @Override
                                    public void onSuccess(Object o) {
                                    }

                                    @Override
                                    public void onError(int i, String s) {
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();

                            }
                        }

                    } else
                        inviteUserToJoinConference(members);
                }

                @Override
                public void onError(int error, String errorMsg) {

                }
            });
        } else {
            inviteUserToJoinConference(members);
        }
        for (String inviteItem : members) {
            ImAddFriendInfoRef imAddFriendInfoRef = new ImAddFriendInfoRef();
            imAddFriendInfoRef.setVideoCallName(inviteItem);
            allMembersList.add(imAddFriendInfoRef);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        EMLog.i(TAG, "onActivityResult: " + requestCode + ", result code: " + resultCode);
        if (requestCode == REQUEST_CODE_OVERLAY_PERMISSION && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestOverlayPermission = false;
            // Result of window permission request, resultCode = RESULT_CANCELED
            if (Settings.canDrawOverlays(activity)) {
                doShowFloatWindow();
            } else {
                Toast.makeText(activity, getString(R.string.alert_window_permission_denied), Toast.LENGTH_SHORT).show();
            }
            return;
        }

        if (resultCode == RESULT_OK) {
            if (requestCode == ScreenCaptureManager.RECORD_REQUEST_CODE) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (Build.VERSION.SDK_INT >= 29) {
                        Intent service = new Intent(this, SRForegroundService.class);
                        service.putExtra("code", resultCode);
                        service.putExtra("data", data);
                        startForegroundService(service);
                    } else {
                        ScreenCaptureManager.getInstance().start(resultCode, data);
                    }

                }
            } else if (requestCode == REQUEST_CODE_INVITE) {
                final String[] members = data.getStringArrayExtra("members");
                if (isCreator && conference == null) {
                    initLocalConferenceView();

                    createAndJoinConference(new EMValueCallBack<EMConference>() {
                        @Override
                        public void onSuccess(EMConference value) {
                            inviteUserToJoinConference(members);
                        }

                        @Override
                        public void onError(int error, String errorMsg) {

                        }
                    });
                } else {
                    inviteUserToJoinConference(members);
                }
            }
        } else if (resultCode == RESULT_CANCELED) {
            // 只有第一次创建会议时,若选择取消需要finish当前activity.
            boolean needFinish = (isCreator && conference == null);
            if (needFinish) finish();
        }
    }

    /**
     * 通过消息的形式邀请他人加入会议
     *
     * @param to 被邀请人
     */
    private void sendInviteMessage(String to, String extension) {
        final EMConversation conversation = EMClient.getInstance().chatManager().getConversation(to, EMConversation.EMConversationType.Chat, true);
        final EMMessage message = EMMessage.createTxtSendMessage(getString(R.string.msg_conference_invite) + " - " + conference.getConferenceId(), to);
        message.setAttribute(Constant.MSG_ATTR_CONF_ID, conference.getConferenceId());
        message.setAttribute(Constant.MSG_ATTR_CONF_PASS, conference.getPassword());
        // 扩展字段对于音视频会议不是必须的,只是增加了额外用于显示或者判断音视频会议类型的信息.
        message.setAttribute(Constant.MSG_ATTR_EXTENSION, extension);
        message.setMessageStatusCallback(new EMCallBack() {
            @Override
            public void onSuccess() {
                EMLog.d(TAG, "Invite join conference success");
                if (null != conversation)
                    conversation.removeMessage(message.getMsgId());
            }

            @Override
            public void onError(int code, String error) {
                EMLog.e(TAG, "Invite join conference error " + code + ", " + error);
                if (null != conversation)
                    conversation.removeMessage(message.getMsgId());
            }

            @Override
            public void onProgress(int progress, String status) {

            }
        });
        EMClient.getInstance().chatManager().sendMessage(message);
    }
    private boolean hasExcutedDestoryConference = false;

    private void destroyConference() {
        if(hasExcutedDestoryConference){
            return;
        }
        hasExcutedDestoryConference = true;
        try {
            Log.e(TAG,"exit conference success in destory");
            //currentConference
            EMClient.getInstance().conferenceManager().destroyConference(new EMValueCallBack() {
                @Override
                public void onSuccess(Object o) {
                    Log.e(TAG,"exit conference success destroy success");
                    createRejectMsg();
                    if (PreferenceManager.getInstance().isExternalAudioInputResolution()) {
                        ExternalAudioInputRecord.getInstance().stopRecording();
                    }
                    if (!isFinishing())
                        finish();
                }

                @Override
                public void onError(int i, String s) {
                    Log.e(TAG,"exit conference success destroy error reason  = "+s);
                    createRejectMsg();
                    if (!isFinishing())
                        finish();
                }
            });


        } catch (Exception e) {
            Log.e(TAG,"exit conference success destroy error reason  =  Exception");
            e.printStackTrace();
            createRejectMsg();
            if (!isFinishing())
                finish();

        }

    }

    public static final String STOP_TIME_HANDLER = "stop_time_handler";
    public static final String START_TIME_HANDLER = "start_time_handler";

    private boolean isExcuteExitConferenceProcess = false;

    private void stopTimeRecord() {
        Intent intent = new Intent();
        Log.e(TAG, "exit conference success 2");
        if (ConferenceConfig.machineType == ConferenceConfig.ConfigMachineType.DEVICE) {
            intent.setClassName(getPackageName(), "com.idwell.cloudframe.service.UploadService");
        } else {
            intent.setClassName(getPackageName(), "com.idwell.aluratekcloudphoto.service.UploadService");
        }
        intent.setAction(STOP_TIME_HANDLER);
        startService(intent);
    }

    /**
     * 退出会议
     */
    public void exitConference() {
        if (isExcuteExitConferenceProcess || isFinishing()) {
            return;
        }
        Log.e(TAG, "exit conference success 1");
        try {
            if (conferenceThread != null) {
                detectConference = false;
                conferenceThread = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        isExcuteExitConferenceProcess = true;
        stopAudioTalkingMonitor();
        stopTimeRecord();
        Log.e(TAG, "exit conference success 3");
        if (ScreenCaptureManager.getInstance().state == ScreenCaptureManager.State.RUNNING) {
            ScreenCaptureManager.getInstance().stop();
            stopForegroundService();
        }
        // Stop to watch the phone call state.
        Log.e(TAG, "exit conference success 4");
        //
        if (!createConferenceSuccess) {
            if (isCreator) {
                createRejectMsg(true);
            }
            if (!isFinishing())
                finish();
            Log.e(TAG, "exit conference not yet in conference");
            return;
        }
        List<EMConferenceMember> members = EMClient.getInstance().conferenceManager().getConferenceMemberList();
        if ((isSingleCall || members.size() <= 0) && isCreator) {
            destroyConference();
        } else {
            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        PhoneStateManager.get().removeStateCallback(phoneStateCallback);
                    }
                });
                EMClient.getInstance().conferenceManager().exitConference(new EMValueCallBack() {
                    @Override
                    public void onSuccess(Object value) {
                        //如果启动外部音频输入 停止音频录制
                        if (PreferenceManager.getInstance().isExternalAudioInputResolution()) {
                            ExternalAudioInputRecord.getInstance().stopRecording();
                        }
                        Log.e(TAG, "exit conference success ");
                        if (!isFinishing())
                            finish();
                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        Log.e(TAG, "exit conference failed " + error + ", " + errorMsg);
                        if (!isFinishing())
                            finish();
                    }
                });
                Log.e(TAG, "exit conference success 5");
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 停止服务
     */
    private void stopForegroundService() {
        if (Build.VERSION.SDK_INT >= 28) {
            Intent service = new Intent(this, SRForegroundService.class);
            stopService(service);
        }
    }

    private void startAudioTalkingMonitor() {
        EMClient.getInstance().conferenceManager().startMonitorSpeaker(300);
    }

    private void stopAudioTalkingMonitor() {
        EMClient.getInstance().conferenceManager().stopMonitorSpeaker();
    }
    List<Camera.Size> supportedVideoSizes,previewSizes;
    public String getCameraInfo(){
        int cameracount = 0;//摄像头数量
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();  //获取摄像头信息
        cameracount = Camera.getNumberOfCameras();
        Log.i("CameraTest","摄像头数量"+String.valueOf(cameracount));
        for(int cameraId=0; cameraId<Camera.getNumberOfCameras(); cameraId++)
        {
            Camera.getCameraInfo( cameraId, cameraInfo);
            Camera camera = Camera.open(cameraId); //开启摄像头获得一个Camera的实例
            Camera.Parameters params = camera.getParameters();  //通过getParameters获取参数
            supportedVideoSizes = params.getSupportedPictureSizes();
            previewSizes = params.getSupportedPreviewSizes();
            camera.release();//释放摄像头

            //重新排列后设下摄像头预设分辨率在所有分辨率列表中的地址，用以选择最佳分辨率（保证适配不出错）
            int index=bestVideoSize(previewSizes.get(0).width);
            Log.i("CameraTest", "预览分辨率地址: " + index );
            if (null != previewSizes && previewSizes.size() > 0){  //判断是否获取到值，否则会报空对象
                Log.i("CameraTest", "摄像头最高分辨率宽: " + String.valueOf(supportedVideoSizes.get(0).width) );  //降序后取最高值，返回的是int类型
                Log.i("CameraTest", "摄像头最高分辨率高: " + String.valueOf(supportedVideoSizes.get(0).height) );
                Log.i("CameraTest", "摄像头分辨率全部: " + cameraSizeToSting( supportedVideoSizes) );
            }else{
                Log.i("CameraTest", "没取到值啊什么鬼" );
                Log.i("CameraTest", "摄像头预览分辨率: " + String.valueOf(previewSizes.get(0).width) );
            }
        }
        return cameraSizeToSting( supportedVideoSizes );
    }
    //重新排列获取到的分辨率列表
    public int bestVideoSize(int _wid) {

        //降序排列
        Collections.sort(supportedVideoSizes, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size lhs, Camera.Size rhs) {
                if (lhs.width > rhs.width) {
                    return -1;
                } else if (lhs.width == rhs.width) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
        for (int i = 0; i < supportedVideoSizes.size(); i++) {
            if (supportedVideoSizes.get(i).width < _wid) {
                return i;
            }
        }
        return 0;
    }
    //分辨率格式化输出String值
    public static String cameraSizeToSting(Iterable<Camera.Size> sizes)
    {
        StringBuilder s = new StringBuilder();
        for (Camera.Size size : sizes)
        {
            if (s.length() != 0)
                s.append(",\n");
            s.append(size.width).append('x').append(size.height);
        }
        return s.toString();
    }
    /**
     * 开始推自己的数据
     */
    private void publish() {
        getCameraInfo();
        addSelfToList();

        //推流时设置水印图片
//        if (PreferenceManager.getInstance().isWatermarkResolution()) {
//            //推流时设置水印图片
//            EMClient.getInstance().conferenceManager().setWaterMark(watermark);
//            //设置水印时取消本地镜像显示
//            EMClient.getInstance().conferenceManager().setLocalVideoViewMirror(EMMirror.OFF);
//        } else {
//            EMClient.getInstance().conferenceManager().setLocalVideoViewMirror(EMMirror.ON);
//        }
        //清晰度优先
        EMClient.getInstance().conferenceManager().setLocalVideoViewMirror(EMMirror.OFF);
        if (ConferenceConfig.machineType == ConferenceConfig.ConfigMachineType.DEVICE) {
            normalParam.setVideoWidth(1280);
            normalParam.setVideoHeight(1092);
        } else {
            normalParam.setVideoWidth(1280);
            normalParam.setVideoHeight(720);
        }
//        normalParam.setMinVideoKbps(500);
//        normalParam.setClarityFirst(false);


        EMClient.getInstance().conferenceManager().publish(normalParam, new EMValueCallBack<String>() {
            @Override
            public void onSuccess(String value) {

                //如果启动外部音频输入 ，启动音频录制
                if (PreferenceManager.getInstance().isExternalAudioInputResolution()) {
                    ExternalAudioInputRecord.getInstance().startRecording();
                }
                conference.setPubStreamId(value, EMConferenceStream.StreamType.NORMAL);
                localView.setStreamId(value);

                streamList.get(0).setStreamId(value);
                debugPanelView.setStreamListAndNotify(streamList);

                // Start to watch the phone call state.
                PhoneStateManager.get().addStateCallback(phoneStateCallback);
            }

            @Override
            public void onError(int error, String errorMsg) {
                EMLog.e(TAG, "publish failed: error");
            }
        });
    }

    private void startScreenCapture3() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (ScreenCaptureManager.getInstance().state == ScreenCaptureManager.State.IDLE) {
                ScreenCaptureManager.getInstance().init(activity);
                ScreenCaptureManager.getInstance().setScreenCaptureCallback(new ScreenCaptureManager.ScreenCaptureCallback() {
                    @Override
                    public void onBitmap(Bitmap bitmap) {
                        EMClient.getInstance().conferenceManager().inputExternalVideoData(bitmap);
                    }
                });
            }
        }
    }

    public void publishDesktop() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            desktopParam.setShareView(null);
        } else {
            desktopParam.setShareView(activity.getWindow().getDecorView());
        }
        EMClient.getInstance().conferenceManager().publish(desktopParam, new EMValueCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                conference.setPubStreamId(value, EMConferenceStream.StreamType.DESKTOP);
                startScreenCapture3();
            }

            @Override
            public void onError(int error, String errorMsg) {

            }
        });
    }

    /**
     * 停止推自己的数据
     */
    private void unpublish(String publishId) {
        if (ScreenCaptureManager.getInstance().state == ScreenCaptureManager.State.RUNNING) {
            if (!TextUtils.isEmpty(conference.getPubStreamId(EMConferenceStream.StreamType.DESKTOP))
                    && publishId.equals(conference.getPubStreamId(EMConferenceStream.StreamType.DESKTOP))) {
                ScreenCaptureManager.getInstance().stop();
                stopForegroundService();
            }
        }
        EMClient.getInstance().conferenceManager().unpublish(publishId, new EMValueCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                //如果启动外部音频输入 停止音频录制
                if (PreferenceManager.getInstance().isExternalAudioInputResolution()) {
                    ExternalAudioInputRecord.getInstance().stopRecording();
                }
            }

            @Override
            public void onError(int error, String errorMsg) {
                EMLog.e(TAG, "unpublish failed: error=" + error + ", msg=" + errorMsg);
            }
        });
    }

    /**
     * 订阅指定成员 stream
     */
    private void subscribe(EMConferenceStream stream, final ConferenceMemberView memberView) {
        EMClient.getInstance().conferenceManager().subscribe(stream, memberView.getSurfaceView(), new EMValueCallBack<String>() {
            @Override
            public void onSuccess(String value) {
            }

            @Override
            public void onError(int error, String errorMsg) {

            }
        });
    }

    /**
     * 取消订阅指定成员 stream
     */
    private void unsubscribe(EMConferenceStream stream) {
        EMClient.getInstance().conferenceManager().unsubscribe(stream, new EMValueCallBack<String>() {
            @Override
            public void onSuccess(String value) {
            }

            @Override
            public void onError(int error, String errorMsg) {

            }
        });
    }

    private int volume = 3;

    /**
     * 打开扬声器
     * 主要是通过扬声器的开关以及设置音频播放模式来实现
     * 1、MODE_NORMAL：是正常模式，一般用于外放音频
     * 2、MODE_IN_COMMUNICATION：
     * 3、MODE_IN_COMMUNICATION：这个和 CALL 都表示通讯模式，不过 CALL 在华为上不好使，故使用 COMMUNICATION
     * 4、MODE_RINGTONE：铃声模式
     */
    public void openSpeaker() {
//        List<EMConferenceMember> emConferenceMemberList = EMClient.getInstance().conferenceManager().getConferenceMemberList();
//        if (null != emConferenceMemberList && emConferenceMemberList.size() > 0) {
//            for (EMConferenceMember emConferenceMember : emConferenceMemberList) {
//                EMClient.getInstance().conferenceManager().unmuteMember(emConferenceMember.memberId);
//            }
//        }
        if (ConferenceConfig.machineType == ConferenceConfig.ConfigMachineType.DEVICE) {
            audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, volume, 0);
            if (null != volumeSeekbar)
                volumeSeekbar.setProgress(volume);
        } else {
            // 检查是否已经开启扬声器
            if (!audioManager.isSpeakerphoneOn()) {
                // 打开扬声器
                audioManager.setSpeakerphoneOn(true);
            }
            // 开启了扬声器之后，因为是进行通话，声音的模式也要设置成通讯模式
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        }
        speakerSwitch.setActivated(true);
    }

    int currentVoiceCall = 0;
    /**
     * Maximum volume index values for audio streams
     */
    private static int[] MAX_STREAM_VOLUME = new int[]{
            5,  // STREAM_VOICE_CALL
            7,  // STREAM_SYSTEM
            7,  // STREAM_RING
            15, // STREAM_MUSIC
            7,  // STREAM_ALARM
            7,  // STREAM_NOTIFICATION
            15, // STREAM_BLUETOOTH_SCO
            7,  // STREAM_SYSTEM_ENFORCED
            15, // STREAM_DTMF
            15, // STREAM_TTS
            15  // STREAM_ACCESSIBILITY
    };

    /**
     * 关闭扬声器，即开启听筒播放模式
     * 更多内容看{@link #openSpeaker()}
     */
    public void closeSpeaker() {
//        List<EMConferenceMember> emConferenceMemberList = EMClient.getInstance().conferenceManager().getConferenceMemberList();
//        if (null != emConferenceMemberList && emConferenceMemberList.size() > 0) {
//            for (EMConferenceMember emConferenceMember : emConferenceMemberList) {
//                EMClient.getInstance().conferenceManager().muteMember(emConferenceMember.memberId);
//            }
//        }
        int minvolume = audioManager.getStreamMinVolume(AudioManager.STREAM_VOICE_CALL);
        if (ConferenceConfig.machineType == ConferenceConfig.ConfigMachineType.DEVICE) {
            audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, 0, 0);
            if (null != volumeSeekbar)
                volumeSeekbar.setProgress(0);
        } else {
            // 检查是否已经开启扬声器
            if (audioManager.isSpeakerphoneOn()) {
                // 关闭扬声器
                audioManager.setSpeakerphoneOn(false);
            }
            // 设置声音模式为通讯模式
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        }
        speakerSwitch.setActivated(false);
    }

    /**
     * 语音开关
     */
    private void voiceSwitch() {
        if (normalParam.isAudioOff()) {
            normalParam.setAudioOff(false);
            EMClient.getInstance().conferenceManager().openVoiceTransfer();
        } else {
            normalParam.setAudioOff(true);
            EMClient.getInstance().conferenceManager().closeVoiceTransfer();
        }
        micSwitch.setActivated(normalParam.isAudioOff());
        localView.setAudioOff(normalParam.isAudioOff());
    }

    private void shutDownVideo() {
        normalParam.setVideoOff(true);
        EMClient.getInstance().conferenceManager().closeVideoTransfer();
        localView.setVideoOff(true);
        localView.setDefaultBackgroundView(R.drawable.em_call_mic_on);
    }

    /**
     * 视频开关
     */
    private void videoSwitch() {
        if (normalParam.isVideoOff()) {
            normalParam.setVideoOff(false);
            EMClient.getInstance().conferenceManager().openVideoTransfer();
        } else {
            normalParam.setVideoOff(true);
            EMClient.getInstance().conferenceManager().closeVideoTransfer();
        }
        cameraSwitch.setActivated(normalParam.isVideoOff());
        localView.setVideoOff(normalParam.isVideoOff());
    }

    /**
     * 切换摄像头
     */
    private void changeCamera() {
        EMClient.getInstance().conferenceManager().switchCamera();
    }

    // 当前设备通话状态监听器
    PhoneStateManager.PhoneStateCallback phoneStateCallback = new PhoneStateManager.PhoneStateCallback() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:   // 电话响铃
                    break;
                case TelephonyManager.CALL_STATE_IDLE:      // 电话挂断
                    // resume current voice conference.
                    if (normalParam.isAudioOff()) {
                        try {
                            EMClient.getInstance().callManager().resumeVoiceTransfer();
                        } catch (HyphenateException e) {
                            e.printStackTrace();
                        }
                    }
                    if (normalParam.isVideoOff()) {
                        try {
                            EMClient.getInstance().callManager().resumeVideoTransfer();
                        } catch (HyphenateException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:   // 来电接通 或者 去电，去电接通  但是没法区分
                    // pause current voice conference.
                    if (!normalParam.isAudioOff()) {
                        try {
                            EMClient.getInstance().callManager().pauseVoiceTransfer();
                        } catch (HyphenateException e) {
                            e.printStackTrace();
                        }
                    }
                    if (!normalParam.isVideoOff()) {
                        try {
                            EMClient.getInstance().callManager().pauseVideoTransfer();
                        } catch (HyphenateException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }
    };

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // 保留非正常退出的会议相关信息
        if (conference != null) {
            outState.putString(Constant.EXTRA_CONFERENCE_ID, conference.getConferenceId());
            outState.putString(Constant.EXTRA_CONFERENCE_PASS, conference.getPassword());
        }

    }

    @Override
    public void onBackPressed() {
        if (incomingCallView.getVisibility() == View.VISIBLE) { // 来电提醒界面
            super.onBackPressed();
            return;
        }
        // 已经在通话过程中
        showFloatWindow();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 用于判断app是否进入后台
        BaseApplication baseApplication = (BaseApplication) getApplication();
        if (!isFinishing() && baseApplication.isForeground()) {
            showFloatWindow();
        }
    }

    /**
     * 防止多次进入ondestroy方法
     */
    private boolean isFinishing = false;

    @Override
    protected void onDestroy() {
        //用户进行过视频通话则记录相关信息
        Log.e("GGGGGG", "onDestroy");
        releaseMediaFocus();
        releaseBackGroundStone();
        if (CallFloatWindow.getInstance(getApplicationContext()).isShowing()) {
            CallFloatWindow.getInstance(getApplicationContext()).dismiss();
        }
        if (null != uiHandler)
            uiHandler.removeCallbacksAndMessages(null);
        if (startConference) {
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0; i < joinedMembers.size(); i++) {
                String joinedMember = joinedMembers.get(i);
                stringBuffer.append(joinedMember);
                if (i < joinedMembers.size() - 1) {
                    stringBuffer.append(" , ");
                }
            }
            imCallInfoRef.setChatters(stringBuffer.toString());
            imCallInfoRef.setChatDur(System.currentTimeMillis() + "");
            if (getIntent().getIntExtra(KEY_CALL_TYPE, -1) == 1) {
                imCallInfoRef.setChatType(getResources().getString(R.string.im_voice_call_tag));
            } else {
                imCallInfoRef.setChatType(getResources().getString(R.string.im_video_call_tag));
            }
            EventBus.getDefault().post(imCallInfoRef);

        }
//        EventBus.getDefault().unregister(this);
        if (null != conferenceListener)
            EMClient.getInstance().conferenceManager().removeConferenceListener(conferenceListener);
        conferenceListener = null;
        DemoHelper.getInstance().popActivity(activity);

        if (conferenceThread != null) {
            detectConference = false;
            conferenceThread = null;
        }
        super.onDestroy();
        if (null != audioManager) {
            audioManager.setMode(AudioManager.MODE_NORMAL);
            audioManager.setMicrophoneMute(false);
        }
        if (null != headsetPlugReceiver)
            unregisterReceiver(headsetPlugReceiver);
    }

    private void releaseBackGroundStone() {
        try {
            if (soundPool != null) {
                soundPool.stop(streamID);
                soundPool.release();
                soundPool = null;
            }
            EMLog.d("EMCallManager", "soundPool stop ACCEPTED");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (ringtone != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P || ConferenceConfig.machineType == ConferenceConfig.ConfigMachineType.DEVICE) {
//                    ringtone.setLooping(false);
                }
                ringtone.stop();
                ringtone = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * --------------------------------------------------------------------
     * 多人音视频会议回调方法
     */

    @Override
    public void onMemberJoined(final EMConferenceMember member) {
        releaseBackGroundStone();
        if (null != uiHandler && uiHandler.hasMessages(MSG_SINGLE_CALL_NOTICE)) {
            uiHandler.removeMessages(MSG_SINGLE_CALL_NOTICE);
        }
        Intent intent = new Intent();
        if (ConferenceConfig.machineType == ConferenceConfig.ConfigMachineType.DEVICE) {
            intent.setClassName(getPackageName(), "com.idwell.cloudframe.service.UploadService");
        } else {
            intent.setClassName(getPackageName(), "com.idwell.aluratekcloudphoto.service.UploadService");
        }
        intent.setAction(START_TIME_HANDLER);
        startService(intent);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isSingleCall) {
//                    Toast.makeText(activity, member.memberName + " joined conference!", Toast.LENGTH_SHORT).show();
                }
                updateConferenceMembers();
            }
        });
    }

    @Override
    public void onMemberExited(final EMConferenceMember member) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                List<EMConferenceMember> members = EMClient.getInstance().conferenceManager().getConferenceMemberList();
                if (isSingleCall && (null != members && members.size() <= 0)) {
                    exitConference();
                    return;
                }
//                Toast.makeText(activity, member.memberName + " removed conference!", Toast.LENGTH_SHORT).show();
                updateConferenceMembers();
            }
        });
    }

    public boolean startConference = false;
    private ArrayList<String> joinedMembers = new ArrayList<>();

    @Override
    public void onStreamAdded(final EMConferenceStream stream) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                Toast.makeText(activity, stream.getUsername() + " stream add!", Toast.LENGTH_SHORT)
//                        .show();
                addConferenceView(stream);

                if (CallFloatWindow.getInstance(getApplicationContext()).isShowing()) { // 通话悬浮窗显示中...
                    int position = streamList.indexOf(stream);
                    if (position == 1) { // 会议中加入第一个成员,需要把正在显示的悬浮窗从自己更新到这个第一个加入会议的成员.
                        showFloatWindow();
                    }
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String joinedMember = stream.getUsername();
                        try {
                            HybernateChatInterface h = (HybernateChatInterface) getApplication();
                            ImAddFriendInfoRef imAddFriendInfoRef = h.findImAddFriendByVideoName(joinedMember);
                            if (null != imAddFriendInfoRef) {
                                String tempjoinedMember = imAddFriendInfoRef.getNickerName();
                                if (!TextUtils.isEmpty(tempjoinedMember)) {
                                    joinedMember = tempjoinedMember;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            joinedMembers.add(joinedMember);
                        }
                    }
                }).start();
                if (!startConference) {
                    imCallInfoRef.setChatStartTime(System.currentTimeMillis() + "");
                    startConference = true;
                }
            }
        });
    }

    @Override
    public void onStreamRemoved(final EMConferenceStream stream) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                Toast.makeText(activity, stream.getUsername() + " stream removed!", Toast.LENGTH_SHORT).show();
                List<EMConferenceMember> members = EMClient.getInstance().conferenceManager().getConferenceMemberList();
                if (isSingleCall && (null != members && members.size() <= 0)) {
                    exitConference();
                    return;
                }
                if (streamList.contains(stream)) {
                    int position = streamList.indexOf(stream);
                    removeConferenceView(stream);

                    if (CallFloatWindow.getInstance(getApplicationContext()).isShowing()) { // 通话悬浮窗显示中...
                        if (position == 1) { // 正在显示悬浮窗的成员退出聊天室
                            showFloatWindow();
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onStreamUpdate(final EMConferenceStream stream) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                Toast.makeText(activity, stream.getUsername() + " stream update!", Toast.LENGTH_SHORT).show();
                updateConferenceMemberView(stream);
            }
        });
    }

    @Override
    public void onMute(String s, String s1) {
        muteSound();
    }

    @Override
    public void onUnMute(String s, String s1) {
        unmuteSound();
    }

    @Override
    public void onPassiveLeave(final int error, final String message) { // 当前用户被踢出会议
        if (!isFinishing()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // 隐藏悬浮窗
                    CallFloatWindow.getInstance(getApplicationContext()).dismiss();
                    DeskShareWindow.getInstance(getApplicationContext()).dismiss();
                    if (ScreenCaptureManager.getInstance().state == ScreenCaptureManager.State.RUNNING) {
                        ScreenCaptureManager.getInstance().stop();
                        stopForegroundService();
                    }
                    // 退出当前界面
                    finish();
                }
            });
        }
    }

    @Override
    public void onConferenceState(final ConferenceState state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                Toast.makeText(activity, "State=" + state, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onStreamStateUpdated(String streamID, StreamState streamState) {
        EMLog.i(TAG, "onStreamStateUpdated   streamId：" + streamID + "  " +
                "state: " + streamState.name());
        for (int i = 0; i < callConferenceViewGroup.getChildCount(); i++) {
            final ConferenceMemberView memberView = (ConferenceMemberView) callConferenceViewGroup.getChildAt(i);
            if (TextUtils.equals(streamID, memberView.getStreamId())) {
                //when no singal,send message to handler
                if (streamState == StreamState.STREAM_NO_AUDIO_DATA || streamState == StreamState.STREAM_NO_VIDEO_DATA) {
                    if (!uiHandler.hasMessages(streamID.hashCode())) {
                        Message message = new Message();
                        message.what = streamID.hashCode();
                        message.obj = streamID;
                        uiHandler.sendMessageDelayed(message, 30 * 1000);
                    }
                } else if (streamState == StreamState.STREAM_HAS_AUDIO_DATA || streamState == StreamState.STREAM_HAS_VIDEO_DATA) {
                    uiHandler.removeMessages(streamID.hashCode());
                }
                if (streamState == StreamState.STREAM_NO_VIDEO_DATA) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            memberView.setVisibleNetStateUi(View.VISIBLE);
                            memberView.setNetState("Unstable network");
                        }
                    });

                } else if (streamState == StreamState.STREAM_HAS_VIDEO_DATA) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            memberView.setVisibleNetStateUi(View.INVISIBLE);
                        }
                    });

                }

                break;
            }
        }

    }

    @Override
    public void onFirstFrameSent(String s, StreamFrameType streamFrameType) {

    }

    @Override
    public void onFirstFrameRecived(String s, StreamFrameType streamFrameType) {

    }

    public void muteSound() {
        normalParam.setAudioOff(true);
    }

    public void unmuteSound() {
        normalParam.setAudioOff(false);
    }

    @Override
    public void onStreamStatistics(EMStreamStatistics statistics) {
        EMLog.i(TAG, "onStreamStatistics" + statistics.toString());
        debugPanelView.onStreamStatisticsChange(statistics);
    }

    @Override
    public void onStreamSetup(final String streamId) {
        if (null == conference || TextUtils.isEmpty(conference.getConferenceId())) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (streamId.equals(conference.getPubStreamId(EMConferenceStream.StreamType.NORMAL))
                        || streamId.equals(conference.getPubStreamId(EMConferenceStream.StreamType.DESKTOP))) {
//                    Toast.makeText(activity, "Publish setup streamId=" + streamId, Toast.LENGTH_SHORT).show();
                } else {
//                    Toast.makeText(activity, "Subscribe setup streamId=" + streamId, Toast.LENGTH_SHORT).show();
                }
            }
        });

        streamList.get(0).setStreamId(streamId);
        debugPanelView.setStreamListAndNotify(streamList);
    }

    @Override
    public void onSpeakers(final List<String> speakers) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                currSpeakers(speakers);
            }
        });
    }

    /**
     * 收到其他人的会议邀请
     *
     * @param confId    会议 id
     * @param password  会议密码
     * @param extension 邀请扩展内容
     */
    @Override
    public void onReceiveInvite(final String confId, String password, String extension) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, "Receive invite " + confId, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onRoleChanged(EMConferenceManager.EMConferenceRole role) {
    }

    @Override
    public void onAttributesUpdated(EMConferenceAttribute[] attributes) {

    }

    private void openDebugPanel() {
        Animator animator = ObjectAnimator.ofFloat(toolsPanelView, "translationY", 0, toolsPanelView.getHeight());
        animator.setDuration(300).start();
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                toolsPanelView.setVisibility(View.GONE);

                debugPanelView.setVisibility(View.VISIBLE);
                Animator animator = ObjectAnimator.ofFloat(debugPanelView, "translationY", debugPanelView.getHeight(), 0);
                animator.setDuration(150).start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
    }

    private void openToolsPanel() {
        Animator animator = ObjectAnimator.ofFloat(debugPanelView, "translationY", 0, debugPanelView.getHeight());
        animator.setDuration(300).start();
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                debugPanelView.setVisibility(View.GONE);

                toolsPanelView.setVisibility(View.VISIBLE);
                Animator animator = ObjectAnimator.ofFloat(toolsPanelView, "translationY", toolsPanelView.getHeight(), 0);
                animator.setDuration(150).start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
    }

    private void updateConferenceMembers() {
        List<EMConferenceMember> members = EMClient.getInstance().conferenceManager().getConferenceMemberList();
        String count = members.size() > 0 ? "(" + members.size() + ")" : "";
        String membersStr = getMembersStr(members);

        membersTV.setText(membersStr);
        memberCountTV.setText(count);

        membersTVMain.setText(membersStr);
        memberCountTVMain.setText(count);

    }

    private String getMembersStr(List<EMConferenceMember> members) {
        String result = "";
        for (int i = 0; i < members.size(); i++) {
            result += EasyUtils.useridFromJid(members.get(i).memberName);
            if (i < members.size() - 1) {
                result += ", ";
            }
        }
        return result;
    }

    public void updateConferenceTime(String time) {
        callTimeView.setText(time);
        callTimeViewMain.setText(time);
    }

    private void changeFullScreenScaleMode() {
        if (!callConferenceViewGroup.isFullScreenMode()) {
            return;
        }

        ConferenceMemberView fullScreenView = (ConferenceMemberView) callConferenceViewGroup.getFullScreenView();
        if (fullScreenView.getScaleMode() == VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFit) {
            fullScreenView.setScaleMode(VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFill);
            scaleModeBtn.setImageResource(R.drawable.em_call_scale_fit);
        } else {
            fullScreenView.setScaleMode(VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFit);
            scaleModeBtn.setImageResource(R.drawable.em_call_scale_fill);
        }
    }

    public void showFloatWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(activity)) {
                doShowFloatWindow();
            } else { // To reqire the window permission.
                if (!requestOverlayPermission) {
                    try {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                        // Add this to open the management GUI specific to this app.
                        intent.setData(Uri.parse("package:" + activity.getPackageName()));
                        activity.startActivityForResult(intent, REQUEST_CODE_OVERLAY_PERMISSION);
                        requestOverlayPermission = true;
                        // Handle the permission require result in #onActivityResult();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            doShowFloatWindow();
        }
    }

    private void registerHeadsetPlugReceiver() {
        headsetPlugReceiver = new HeadsetPlugReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.HEADSET_PLUG");
        registerReceiver(headsetPlugReceiver, intentFilter);
    }

    private void doShowFloatWindow() {
        if (screenShareSwitch.isActivated()) { // 已开启桌面共享,显示桌面共享window
            DeskShareWindow.getInstance(getApplicationContext()).show();
        } else { // 显示通话悬浮窗
            CallFloatWindow.getInstance(getApplicationContext()).show();

            if (streamList.size() > 1) { // 如果会议中有其他成员,则显示第一个成员
                windowStream = streamList.get(1);
            } else { // 会议中无其他成员,显示自己信息
                windowStream = new EMConferenceStream();
                windowStream.setUsername(EMClient.getInstance().getCurrentUser());
                if (getIntent().getIntExtra(KEY_CALL_TYPE, -1) == 1) {
                    windowStream.setVideoOff(true);
                } else
                    windowStream.setVideoOff(normalParam.isVideoOff());
                windowStream.setAudioOff(normalParam.isAudioOff());
            }
            CallFloatWindow.getInstance(getApplicationContext()).update(windowStream);
        }

        ConferenceActivity.this.moveTaskToBack(true);
    }

    private void addSelfToList() {
        EMConferenceStream localStream = new EMConferenceStream();
        localStream.setUsername(EMClient.getInstance().getCurrentUser());
        localStream.setStreamId("local-stream");
        streamList.add(localStream);
    }

    private void registerHomeKeyWatcher() {
        registerReceiver(homeKeyWatcher, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
    }

    private void unregisterHomeKeyWatcher() {
        unregisterReceiver(homeKeyWatcher);
    }

    private BroadcastReceiver homeKeyWatcher = new BroadcastReceiver() {
        private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
        private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "onReceive: ");
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                Log.i(TAG, "onReceive, reason: " + reason);
                if (SYSTEM_DIALOG_REASON_HOME_KEY.equals(reason)) {
                    EMLog.i(TAG, "Home key clicked.");
                    showFloatWindow();
                }
            }
        }
    };


    @Override
    public void onGetLocalStreamId(String s, String s1) {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showFloatWindow();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void showVolumeSeekBar(boolean visible, boolean raiseVolume) {
        if (null == volumeSeekbar) return;
        if (!visible) {
            volumeSeekbar.setVisibility(View.INVISIBLE);
            return;
        }
        volumeSeekbar.setVisibility(View.VISIBLE);
        if (raiseVolume) {
            volume++;
            if (volume >= MAX_VOICE_CALL_VOLUME) {
                volume = MAX_VOICE_CALL_VOLUME;
            }
            audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, volume, 0);
            volumeSeekbar.setProgress(volume);
        } else {
            volume--;
            if (volume <= 0) {
                volume = 0;
            }
            audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, volume, 0);
            volumeSeekbar.setProgress(volume);
        }

    }

    public void showVolumeSeekBar(boolean raiseVolume) {
        showVolumeSeekBar(true, raiseVolume);
    }
}
