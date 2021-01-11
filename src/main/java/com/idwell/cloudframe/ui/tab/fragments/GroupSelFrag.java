package com.idwell.cloudframe.ui.tab.fragments;

import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.hyphenate.chatuidemo.conference.ConferenceActivity;
import com.hyphenate.chatuidemo.entity.ImAddFriendInfoRef;
import com.hyphenate.easeui.R;
import com.hyphenate.util.DensityUtil;
import com.idwell.cloudframe.data.db.MyCallDatabase;
import com.idwell.cloudframe.http.entity.ImAddFriendInfo;
import com.idwell.cloudframe.ui.tab.adapter.FriendCommAdapter;
import com.idwell.cloudframe.ui.tab.entity.Status;
import com.idwell.cloudframe.ui.tab.util.BridgeImCallInfoUtil;
import com.idwell.cloudframe.ui.tab.util.SpacesItemTopDecoration;

import java.util.ArrayList;
import java.util.List;

public class GroupSelFrag extends Fragment implements FriendCommAdapter.OnItemClickLis, View.OnClickListener {
    private RecyclerView mRecyclerView;
    private FriendCommAdapter adapter;
    private static final int PAGE_SIZE = 3;
    GridLayoutManager gridLayoutManager;
    private View baseView = null;
    SpacesItemTopDecoration spacesItemTopDecoration;
    int spanCount = 4;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        baseView = inflater.inflate(R.layout.frag_universal_comm, container, false);
        return baseView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = baseView.findViewById(R.id.rv);
//        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//        mRecyclerView.addItemDecoration(new MediaGridInset(4, spacing, false));
        spacesItemTopDecoration = new SpacesItemTopDecoration(DensityUtil.dip2px(getActivity(), 10));
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //---landscape mode---
            spanCount = 4;
        } else {
            spanCount = 3;
        }
        spacesItemTopDecoration.setColumeSize(spanCount);
        mRecyclerView.addItemDecoration(spacesItemTopDecoration);
        gridLayoutManager = new GridLayoutManager(getActivity(), 4);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        initAdapter();
        mRecyclerView.setAdapter(adapter);
        baseView.findViewById(R.id.comm_voice).setOnClickListener(this);
        baseView.findViewById(R.id.comm_video).setOnClickListener(this);
        MyCallDatabase.Companion.getInstance().getImDao().queryAll().observe(getViewLifecycleOwner(), new Observer<List<ImAddFriendInfo>>() {
            @Override
            public void onChanged(List<ImAddFriendInfo> imAddFriendInfos) {
                itemlist.clear();
                List<ImAddFriendInfo> unInvitedFriendInfos =new ArrayList<>();
                for (ImAddFriendInfo imAddFriendInfo : imAddFriendInfos) {
                    if (TextUtils.equals("2",imAddFriendInfo.getIsAccepted())) {
                        unInvitedFriendInfos.add(imAddFriendInfo);
                    }
                }
                imAddFriendInfos.removeAll(unInvitedFriendInfos);
                itemlist.addAll(0, imAddFriendInfos);
                adapter.notifyDataSetChanged();
            }
        });
    }


    private View.OnClickListener getRemoveHeaderListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.removeHeaderView(v);
            }
        };
    }

    ArrayList<ImAddFriendInfo> itemlist;

    private void initAdapter() {
        itemlist = new ArrayList<>();
        adapter = new FriendCommAdapter(itemlist, this);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                ImAddFriendInfo status = itemlist.get(position);
                if(getSelectMember().size() >= 3 && !status.isSelected()){
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

    private ArrayList<ImAddFriendInfoRef> getSelectMember() {
        String[] result = null;
        ArrayList<ImAddFriendInfoRef> memnerSelected = new ArrayList<>();
        try {
            ArrayList<ImAddFriendInfo> memner = (ArrayList<ImAddFriendInfo>) adapter.getData();
            for (ImAddFriendInfo imAddFriendInfo : memner) {
                if (imAddFriendInfo.isSelected()) {
                    ImAddFriendInfoRef  imAddFriendInfoRef =  BridgeImCallInfoUtil.convertImCallInfo(imAddFriendInfo);
                    memnerSelected.add(imAddFriendInfoRef);
                }
            }
            result = new String[memnerSelected.size()];
            for (int i = 0; i < memnerSelected.size(); i++) {
                result[i] = memnerSelected.get(i).getVideoCallName();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return memnerSelected;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.comm_voice) {
            ArrayList members = getSelectMember();
            if (null == members || members.size() <= 0) {
                ToastUtils.showLong(getResources().getString(R.string.im_call_type_sel_friend));
                return;
            }
            ConferenceActivity.startConferenceCall(getActivity(), null, members, 1);
        } else if (id == R.id.comm_video) {
            ArrayList members2 = getSelectMember();
            if (null == members2 || members2.size() <= 0) {
                ToastUtils.showLong(getResources().getString(R.string.im_call_type_sel_friend));
                return;
            }
            ConferenceActivity.startConferenceCall(getActivity(), null, members2, 0);
        }
    }
}
