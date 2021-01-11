package com.idwell.cloudframe.ui.tab.fragments;

import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.hyphenate.easeui.R;
import com.idwell.cloudframe.data.db.MyCallDatabase;
import com.idwell.cloudframe.http.entity.ImCallInfo;
import com.idwell.cloudframe.ui.tab.adapter.CommDetailAdapter;
import com.idwell.cloudframe.ui.tab.widget.HWRecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommDetailFrag extends Fragment implements View.OnClickListener,CommDetailAdapter.OnItemClickLis {
    private HWRecyclerView mRecyclerView;
    private CommDetailAdapter adapter;
    LinearLayoutManager gridLayoutManager;
    private View baseView = null;
    ArrayList<ImCallInfo> itemlist = new ArrayList<>();
    View ic_delete;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        baseView = inflater.inflate(R.layout.activity_comm_detail, container, false);
        return baseView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = baseView.findViewById(R.id.rv);
        ic_delete = baseView.findViewById(R.id.delete);
        ic_delete.setOnClickListener(this::onClick);
        gridLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(gridLayoutManager);
        initAdapter();
        mRecyclerView.setAdapter(adapter);
        MyCallDatabase.Companion.getInstance().getImCallInfoDao().queryAll().observe(getViewLifecycleOwner(), new Observer<List<ImCallInfo>>() {
            @Override
            public void onChanged(List<ImCallInfo> imAddFriendInfos) {
                itemlist.clear();
                itemlist.addAll(imAddFriendInfos);
                Collections.reverse(itemlist);
                adapter.notifyDataSetChanged();
                if(adapter.getData().size() <= 0){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onLongClickLis(false);
                        }
                    });
                }
            }
        });
        mRecyclerView.setOnMoveCheckListener(new HWRecyclerView.OnMoveCheckListener() {

            @Override
            public void onMoveCheckListener(SparseArray<Boolean> positions) {
                for (int i = 0; i < positions.size(); i++) {
                    itemlist.get(positions.keyAt(i)).setChecked(positions.get(positions.keyAt(i)));
                    adapter.notifyItemChanged(positions.keyAt(1));
                }
                mRecyclerView.clecrCheckPositions();
            }
        });
    }

    private void initAdapter() {
        adapter = new CommDetailAdapter(itemlist, this);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
            }
        });
    }

    @Override
    public void OnItemClick(ImCallInfo item) {
        if (item.getItemType() == 1) {
        }
    }

    @Override
    public void onLongClickLis(boolean clicked) {
        if(clicked){
            ic_delete.setVisibility(View.VISIBLE);
        }else{
            ic_delete.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        if(v == ic_delete){
            ArrayList<ImCallInfo> checkedList = itemlist;
            if(!checkedList.isEmpty()){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for(ImCallInfo imCallInfo : checkedList) {
                            if(imCallInfo.isChecked())
                            MyCallDatabase.Companion.getInstance().getImCallInfoDao().delete(imCallInfo);
                        }
                    }
                }).start();
            }
        }
    }
}
