package com.idwell.cloudframe.ui.tab.fragments;

import android.content.Intent;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConferenceMember;
import com.hyphenate.chatuidemo.conference.ConferenceActivity;
import com.hyphenate.chatuidemo.conference.utils.ActivityPageManager;
import com.hyphenate.chatuidemo.entity.ImAddFriendInfoRef;
import com.hyphenate.chatuidemo.ui.BaseActivity;
import com.hyphenate.easeui.R;
import com.hyphenate.util.DensityUtil;
import com.hyphenate.chatuidemo.entity.MessageEvent;
import com.idwell.cloudframe.data.db.MyCallDatabase;
import com.idwell.cloudframe.http.entity.ImAddFriendInfo;
import com.idwell.cloudframe.ui.tab.adapter.FriendCommAdapter;
import com.idwell.cloudframe.ui.tab.entity.Status;
import com.idwell.cloudframe.ui.tab.util.SpacesItemTopDecoration;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.hyphenate.chatuidemo.conference.ConferenceActivity.KEY_MEMBERS_INFO;

public class ConferenceInviteActivity extends BaseActivity implements FriendCommAdapter.OnItemClickLis, View.OnClickListener {
    private RecyclerView mRecyclerView;
    private FriendCommAdapter adapter;
    GridLayoutManager gridLayoutManager;
    private View baseView = null;
    SpacesItemTopDecoration spacesItemTopDecoration;
    int spanCount = 4;
    private ArrayList<ImAddFriendInfo> itemlist;
    private ArrayList<ImAddFriendInfoRef> memberOnCalling;

    @Override
    public int initLayout() {
        return R.layout.frag_universal_comm;
    }

    @Override
    public void initData() {
        mRecyclerView = findViewById(R.id.rv);
        spacesItemTopDecoration = new SpacesItemTopDecoration(DensityUtil.dip2px(this, 10));
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //---landscape mode---
            spanCount = 4;
        } else {
            spanCount = 3;
        }
        spacesItemTopDecoration.setColumeSize(spanCount);
        mRecyclerView.addItemDecoration(spacesItemTopDecoration);
        gridLayoutManager = new GridLayoutManager(this, 4);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        memberOnCalling = getIntent().getParcelableArrayListExtra(KEY_MEMBERS_INFO);
        initAdapter();
        mRecyclerView.setAdapter(adapter);
        TextView cancle = findViewById(R.id.cancle);
        cancle.setCompoundDrawables(null, null, null, null);
        cancle.setText(R.string.cancel);
        TextView sure = findViewById(R.id.sure);
        sure.setText(R.string.ok);
        sure.setCompoundDrawables(null, null, null, null);
        findViewById(R.id.comm_voice).setOnClickListener(this);
        findViewById(R.id.comm_video).setOnClickListener(this);
        MyCallDatabase.Companion.getInstance().getImDao().queryAll().observe(this, new Observer<List<ImAddFriendInfo>>() {
            @Override
            public void onChanged(List<ImAddFriendInfo> imAddFriendInfos) {
                List<EMConferenceMember> emConferenceMemberList = EMClient.getInstance().conferenceManager().getConferenceMemberList();

                if (null != emConferenceMemberList) {
                    if (emConferenceMemberList.size() > 0) {
                        for (EMConferenceMember emConferenceMember : emConferenceMemberList) {
                            for (ImAddFriendInfo imAddFriendInfo : imAddFriendInfos) {
                                if(TextUtils.equals(imAddFriendInfo.getVideoCallName().toUpperCase(),emConferenceMember.nickName.toUpperCase())){
                                    imAddFriendInfo.setCanSelected(false);
                                }
                            }
                        }
                    }
                }
                itemlist.clear();
                itemlist.addAll(0, imAddFriendInfos);
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void backEvent() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void initListener() {

    }
    private void initAdapter() {
        itemlist = new ArrayList<>();
        adapter = new FriendCommAdapter(itemlist, this);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                ImAddFriendInfo status = itemlist.get(position);
                List<EMConferenceMember> emConferenceMemberList = EMClient.getInstance().conferenceManager().getConferenceMemberList();
                status.setCanSelected(true);
                try {
                    for(EMConferenceMember emConferenceMember : emConferenceMemberList){
                        if(TextUtils.equals(status.getVideoCallName().toUpperCase(),emConferenceMember.nickName.toUpperCase())){
                            status.setCanSelected(false);
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                if (!status.isCanSelected()) {
                    ToastUtils.showLong(getResources().getString(R.string.im_call_type_sel_unable));
                    return;
                }
                int memberSize = 0;
                if(null != emConferenceMemberList){
                    memberSize = emConferenceMemberList.size();
                }
                int totalSize = getSelectMember().length + memberSize;
                if(totalSize >= 3 && !status.isSelected()){
                    ToastUtils.showLong(getResources().getString(R.string.add_friend_max_notice));
                    return;
                }
                status.setSelected(!status.isSelected());
                adapter.notifyItemChanged(position);
            }
        });
    }

    @Override
    public void OnItemClick(Status item) {
        if (item.getItemType() == 1) {
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

    private String[] getSelectMember() {
        String[] result = null;
        try {
            ArrayList<ImAddFriendInfo> memner = (ArrayList<ImAddFriendInfo>) adapter.getData();
            ArrayList<ImAddFriendInfo> memnerSelected = new ArrayList<>();
            for (ImAddFriendInfo imAddFriendInfo : memner) {
                if (imAddFriendInfo.isSelected()) {
                    memnerSelected.add(imAddFriendInfo);
                }
            }
            result = new String[memnerSelected.size()];
            for (int i = 0; i < memnerSelected.size(); i++) {
                result[i] = memnerSelected.get(i).getVideoCallName();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.comm_voice || v.getId() == R.id.iv_back_base){
            if(ActivityPageManager.getInstance().exitActivity("ConferenceActivity")) {
                setResult(RESULT_CANCELED);
                Intent intent = new Intent(this, ConferenceActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                this.startActivity(intent);
            }

            finish();
        }else if(v.getId() == R.id.comm_video){
            String[] members2 = getSelectMember();
            if (null == members2 || members2.length <= 0) {
                ToastUtils.showLong(getResources().getString(R.string.im_call_type_sel_friend));
                return;
            }
            EventBus.getDefault().post(members2);
            Intent intent2 = new Intent(this, ConferenceActivity.class);
            intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(intent2);
            finish();
        }
    }
}
