package com.idwell.cloudframe.ui.tab.fragments;

import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;
import com.hyphenate.chatuidemo.Constant;
import com.hyphenate.chatuidemo.conference.ConferenceActivity;
import com.hyphenate.chatuidemo.entity.ImAddFriendInfoRef;
import com.hyphenate.chatuidemo.widget.MyDialog;
import com.hyphenate.easeui.R;
import com.hyphenate.util.DensityUtil;
import com.idwell.cloudframe.data.db.MyCallDatabase;
import com.idwell.cloudframe.data.db.dao.User;
import com.idwell.cloudframe.http.CallBaseHttpObserver;
import com.idwell.cloudframe.http.CallRetrofitManager;
import com.idwell.cloudframe.http.entity.DeviceStatus;
import com.idwell.cloudframe.http.entity.ImAddFriendInfo;
import com.idwell.cloudframe.http.entity.ImFriend;
import com.idwell.cloudframe.http.entity.ImFriendList;
import com.idwell.cloudframe.http.service.ImAddFriendService;
import com.idwell.cloudframe.http.service.ImDelFriendService;
import com.idwell.cloudframe.http.service.ImGetFriendService;
import com.idwell.cloudframe.http.service.StatusService;
import com.idwell.cloudframe.ui.tab.adapter.HeaderAndFooterAdapter;
import com.idwell.cloudframe.ui.tab.util.BridgeImCallInfoUtil;
import com.idwell.cloudframe.ui.tab.util.DeviceUtil;
import com.idwell.cloudframe.ui.tab.util.SpacesItemTopDecoration;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class FriendFrag extends FreindManagerFrag implements HeaderAndFooterAdapter.OnItemClickLis, MyDialog.OnCenterItemClickListener {
    private RecyclerView mRecyclerView;
    private HeaderAndFooterAdapter adapter;
    private GridLayoutManager gridLayoutManager;
    private View baseView = null;
    private ArrayList<ImAddFriendInfo> itemlist = new ArrayList<>();
    private MyDialog addFriendDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        baseView = inflater.inflate(R.layout.activity_universal_recycler, container, false);
        return baseView;
    }

    int spanCount = 4;
    SpacesItemTopDecoration spacesItemTopDecoration;

    private User adminUser;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        adminUser = MyCallDatabase.Companion.getInstance().getUserDao().queryIsAdmin();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mRecyclerView = baseView.findViewById(R.id.rv);
        spacesItemTopDecoration = new SpacesItemTopDecoration(DensityUtil.dip2px(getActivity(), 10));
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //---landscape mode---
            spanCount = 4;
        } else {
            spanCount = 3;
        }
        spacesItemTopDecoration.setColumeSize(spanCount);
        mRecyclerView.addItemDecoration(spacesItemTopDecoration);
        gridLayoutManager = new GridLayoutManager(getActivity(), spanCount);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        initAdapter();
        mRecyclerView.setAdapter(adapter);
        MyCallDatabase.Companion.getInstance().getImDao().queryAll().observe(getViewLifecycleOwner(), new Observer<List<ImAddFriendInfo>>() {
            @Override
            public void onChanged(List<ImAddFriendInfo> imAddFriendInfos) {
                List<ImAddFriendInfo> unInvitedFriendInfos = new ArrayList<>();
                itemlist.clear();
                itemlist.addAll((ArrayList<ImAddFriendInfo>) getSampleData());

                for (ImAddFriendInfo imAddFriendInfo : imAddFriendInfos) {
                    if (null != adminUser && imAddFriendInfo.getId() == adminUser.getId()) {
                        imAddFriendInfo.setAdmin(true);
                    }
                    if (TextUtils.equals("2", imAddFriendInfo.getIsAccepted())) {
                        unInvitedFriendInfos.add(imAddFriendInfo);
                    }
                }
                imAddFriendInfos.removeAll(unInvitedFriendInfos);
                itemlist.addAll(0, imAddFriendInfos);
                adapter.notifyDataSetChanged();
            }
        });

//        if(!Device.INSTANCE.isVisitedImService()){
        getIMFreindList();
//        }

    }

    private void initAdapter() {
        itemlist.addAll((ArrayList<ImAddFriendInfo>) getSampleData());
        adapter = new HeaderAndFooterAdapter(itemlist, this);
    }

    public static List<ImAddFriendInfo> getSampleData() {
        List<ImAddFriendInfo> list = new ArrayList<>();
        ImAddFriendInfo status = new ImAddFriendInfo();
        status.setItemType(1);
        list.add(status);
        return list;
    }

    @Override
    public void OnItemClick(ImAddFriendInfo item, HeaderAndFooterAdapter.OperateType type) {
        if (type == HeaderAndFooterAdapter.OperateType.ADD) {
            addFriend();
        } else if (type == HeaderAndFooterAdapter.OperateType.VOICE_CALL) {
            ImAddFriendInfoRef imAddFriendInfoRef = BridgeImCallInfoUtil.convertImCallInfo(item);
            ArrayList<ImAddFriendInfoRef> members = new ArrayList<>();
            members.add(imAddFriendInfoRef);
            ConferenceActivity.startConferenceCall(getActivity(), null, members, 1, 1);
        } else if (type == HeaderAndFooterAdapter.OperateType.VIDEO_CALL) {
            ImAddFriendInfoRef imAddFriendInfoRef = BridgeImCallInfoUtil.convertImCallInfo(item);
            ArrayList<ImAddFriendInfoRef> members = new ArrayList<>();
            members.add(imAddFriendInfoRef);
            ConferenceActivity.startConferenceCall(getActivity(), null, members, 0, 1);
        } else if (type == HeaderAndFooterAdapter.OperateType.DELETE) {

            deleteFriend(item, new MyDialog.OnCenterItemClickListener() {
                @Override
                public void OnCenterItemClick(MyDialog dialog, int view) {
                    if (TextUtils.equals(item.getType(), "device")) {
                        removeFriendByFrameID(item);
                    } else {
                        removePhoneFriend(item);
                    }
                }
            });
        } else if (type == HeaderAndFooterAdapter.OperateType.UPDATE_NICKER_NAME) {
            updateNickerName(item);
        }

    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        // 当新设置中，屏幕布局模式为横排时
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == 2) {
            spanCount = 4;
            gridLayoutManager.setSpanCount(spanCount);
            spacesItemTopDecoration.setColumeSize(spanCount);
            adapter.notifyDataSetChanged();
        } else {
            spanCount = 3;
            gridLayoutManager.setSpanCount(spanCount);
            spacesItemTopDecoration.setColumeSize(spanCount);
            adapter.notifyDataSetChanged();
        }
    }

    void addFriend() {
        //定义一个自己的dialog
//实例化自定义的dialog
        addFriendDialog = new MyDialog(this.getActivity(), R.layout.activity_add_freind_dialog);
        addFriendDialog.setRemarkNameVisible(View.VISIBLE);
        //绑定点击事件
        addFriendDialog.setOnCenterItemClickListener(this);
        //显示
        addFriendDialog.show();
        //调用点击函数
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                mRecyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        addFriendDialog.showKeyboard();
                    }
                });
            }
        };
        timer.schedule(timerTask, 600);
    }

    @Override
    public void OnCenterItemClick(MyDialog dialog, int view) {
        int id = view;
        if (id == R.id.btn_cancle) {

        } else if (id == R.id.btn_sure) {
            if (dialog != null) {
                if (TextUtils.isEmpty(dialog.getInputContent())) {
                    ToastUtils.showLong(getResources().getString(R.string.add_friend_empty_notice));
                } else {
                    if(TextUtils.equals(dialog.getInputContent().toUpperCase().trim(), Constant.token)){
                        ToastUtils.showLong(getResources().getString(R.string.add_friend_empty_notice));
                        return;
                    }
                    addFriendByFrameID(dialog.getInputContent());
                }
            }

        }
    }

    /**
     * 移除绑定的手机端设备
     *
     * @param imdelFriendInfo
     */
    private void removePhoneFriend(ImAddFriendInfo imdelFriendInfo) {
        CallRetrofitManager.INSTANCE.getService(StatusService.class)
                .status(
                        imdelFriendInfo.getId(), Constant.ID, "unbind"
                ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new CallBaseHttpObserver<DeviceStatus>() {
            public void onSuccess(DeviceStatus data) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MyCallDatabase.Companion.getInstance().getImDao().delete(imdelFriendInfo);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.getData().remove(imdelFriendInfo);
//                                adapter.notifyItemRemoved(imdelFriendInfo.getPostion());
                                if (null != addFriendDialog && addFriendDialog.isShowing()) {
                                    addFriendDialog.dismiss();
                                }
                            }
                        });

                    }
                }).start();

            }
        });
    }

    /**
     * 移除绑定的相框设备
     *
     * @param imdelFriendInfo
     */
    private void removeFriendByFrameID(ImAddFriendInfo imdelFriendInfo) {
        CallRetrofitManager.INSTANCE.getService(ImDelFriendService.class)
                .deleteFriend(
                        Constant.ID,
                        imdelFriendInfo.getVideoCallName()
                ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new CallBaseHttpObserver<ImAddFriendInfo>() {
            public void onSuccess(ImAddFriendInfo data) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MyCallDatabase.Companion.getInstance().getImDao().delete(imdelFriendInfo);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.getData().remove(imdelFriendInfo);
//                                adapter.notifyItemRemoved(imdelFriendInfo.getPostion());
                                if (null != addFriendDialog && addFriendDialog.isShowing()) {
                                    addFriendDialog.dismiss();
                                }
                            }
                        });

                    }
                }).start();

            }
        });
    }

    private void addFriendByFrameID(String connectCode) {
        CallRetrofitManager.INSTANCE.getService(ImAddFriendService.class)
                .addFriend(
                        Constant.ID,
                        connectCode
                ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new CallBaseHttpObserver<ImAddFriendInfo>() {
            public void onSuccess(ImAddFriendInfo data) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (null != data && !TextUtils.isEmpty(data.getVideoCallName())) {
                            String remarkName = addFriendDialog.getRemarkContent();
                            if (!TextUtils.isEmpty(remarkName)) {
                                remarkName = remarkName.toUpperCase();
                                data.setNickerName(remarkName);
                            }
                            data.setIsAccepted("2");
                            MyCallDatabase.Companion.getInstance().getImDao().insert(data);

                        } else {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ToastUtils.showLong(getResources().getString(R.string.add_friend_error_notice));
                                }
                            });
                        }
                    }
                }).start();
//                adapter.addData(adapter.getItemCount() - 1, data);
//                mRecyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
                if (null != addFriendDialog && addFriendDialog.isShowing()) {
                    addFriendDialog.dismiss();
                }
            }
        });
    }

    private void getIMFreindList() {
        CallRetrofitManager.INSTANCE.getService(ImGetFriendService.class)
                .getVideoSingleUserFriends(
                        Constant.ID
                ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new CallBaseHttpObserver<ImFriendList>() {
            public void onSuccess(ImFriendList data) {
                try {
                    DeviceUtil.INSTANCE.setVisitedImService(true);
                    ArrayList<ImFriend> memberList = null;
                    try {
                        memberList = data.getList();
                    } catch (Exception e) {
                        e.printStackTrace();
                        adapter.getData().clear();
                        itemlist.addAll((ArrayList<ImAddFriendInfo>) getSampleData());
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                MyCallDatabase.Companion.getInstance().getImDao().deleteAll();
                            }
                        }).start();
                        return;
                    }
                    if (null == memberList || memberList.isEmpty()) {
                        adapter.getData().clear();
                        itemlist.addAll((ArrayList<ImAddFriendInfo>) getSampleData());
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                MyCallDatabase.Companion.getInstance().getImDao().deleteAll();
                            }
                        }).start();
                        return;
                    }
                    ArrayList<ImAddFriendInfo> allMembers = new ArrayList<>();
                    for (ImFriend item : memberList) {
                        ImAddFriendInfo imAddFriendInfo = new ImAddFriendInfo();
                        imAddFriendInfo.setVideoCallName(item.getVideoCallName().toUpperCase());
                        imAddFriendInfo.setId(item.getId());
                        imAddFriendInfo.setIsAccepted("1");
                        imAddFriendInfo.setType(item.getType());
                        allMembers.add(imAddFriendInfo);
                    }
                    adapter.getData().clear();
                    itemlist.addAll((ArrayList<ImAddFriendInfo>) getSampleData());
                    if (!allMembers.isEmpty()) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                for (ImAddFriendInfo imAddFriendInfo : allMembers) {
                                    ImAddFriendInfo imAddFriendInfoDB = MyCallDatabase.Companion.getInstance().getImDao().queryId(imAddFriendInfo.getVideoCallName());
                                    if (null != imAddFriendInfoDB && !TextUtils.isEmpty(imAddFriendInfoDB.getVideoCallName())) {
                                        imAddFriendInfo.setNickerName(imAddFriendInfoDB.getNickerName());
                                    }
                                }
                                MyCallDatabase.Companion.getInstance().getImDao().deleteAll();
                                MyCallDatabase.Companion.getInstance().getImDao().insert(allMembers);
                            }
                        }).start();
                    } else {
                        MyCallDatabase.Companion.getInstance().getImDao().deleteAll();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void updateUI(int updatePos) {
        adapter.notifyItemChanged(updatePos);
    }
}
