package com.idwell.cloudframe.ui.tab.adapter;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;


import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.hyphenate.easeui.R;
import com.idwell.cloudframe.http.entity.ImAddFriendInfo;
import com.idwell.cloudframe.ui.tab.entity.Status;

import java.util.ArrayList;

/**
 * https://github.com/CymChad/BaseRecyclerViewAdapterHelper
 */
public class HeaderAndFooterAdapter extends BaseMultiItemQuickAdapter<ImAddFriendInfo, BaseViewHolder> {
    OnItemClickLis onItemClickLis;
    ArrayList<ImAddFriendInfo> mdataSize = new ArrayList<>();

    public HeaderAndFooterAdapter(ArrayList<ImAddFriendInfo> dataSize, OnItemClickLis onItemClickLis) {
        super(dataSize);
        mdataSize.addAll(dataSize);
        this.onItemClickLis = onItemClickLis;
        addItemType(0, R.layout.home_item_view);  //1
        addItemType(1, R.layout.footer_view);//3
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, ImAddFriendInfo item) {
        if (item.getItemType() == 1) {
            helper.getView(R.id.iv).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickLis.OnItemClick(item, OperateType.ADD);
                }
            });
        } else if (item.getItemType() == 0) {
            TextView account = helper.getView(R.id.account);
            TextView account_id = helper.getView(R.id.account_id);
            TextView admin = helper.getView(R.id.admin);
            String hintContent = TextUtils.isEmpty(item.getNickerName()) ? item.getVideoCallName() : item.getNickerName();
            account.setText(hintContent.toUpperCase());
            if(!TextUtils.isEmpty(item.getNickerName())){
                account_id.setVisibility(View.VISIBLE);
                account_id.setText("("+item.getVideoCallName()+")");
            }else{
                account_id.setVisibility(View.GONE);
            }
            if (item.isAdmin()) {
                helper.setVisible(R.id.admin, true);
                helper.setVisible(R.id.delete, false);
            } else {
                helper.setVisible(R.id.admin, false);
                helper.setVisible(R.id.delete, true);
            }
            helper.getView(R.id.delete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    item.setPostion(helper.getLayoutPosition());
                    onItemClickLis.OnItemClick(item, OperateType.DELETE);
                }
            });
            helper.getView(R.id.comm_voice).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    item.setPostion(helper.getLayoutPosition());
                    onItemClickLis.OnItemClick(item, OperateType.VOICE_CALL);
                }
            });
            helper.getView(R.id.comm_video).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    item.setPostion(helper.getLayoutPosition());
                    onItemClickLis.OnItemClick(item, OperateType.VIDEO_CALL);
                }
            });
            helper.getView(R.id.icon).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    item.setPostion(helper.getLayoutPosition());
                    onItemClickLis.OnItemClick(item, OperateType.UPDATE_NICKER_NAME);
                }
            });
            helper.getView(R.id.account).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    item.setPostion(helper.getLayoutPosition());
                    onItemClickLis.OnItemClick(item, OperateType.UPDATE_NICKER_NAME);
                }
            });
        }
    }

    public enum OperateType {
        DELETE, ADD, VIDEO_CALL, VOICE_CALL, UPDATE_NICKER_NAME
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public interface OnItemClickLis {
        public void OnItemClick(ImAddFriendInfo item, OperateType type);
    }
}
