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
public class FriendCommAdapter extends BaseMultiItemQuickAdapter<ImAddFriendInfo, BaseViewHolder> {
    OnItemClickLis  onItemClickLis;
    public FriendCommAdapter(ArrayList<ImAddFriendInfo> dataSize, OnItemClickLis  onItemClickLis) {
        super(dataSize);
        this.onItemClickLis = onItemClickLis;
        addItemType(0, R.layout.home_friend_comm);  //1
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, ImAddFriendInfo item) {
        TextView account = helper.getView(R.id.account);
        String hintContent = TextUtils.isEmpty(item.getNickerName())?item.getVideoCallName():item.getNickerName();
        account.setText(hintContent.toUpperCase());
        TextView account_id = helper.getView(R.id.account_id);
        account.setText(hintContent.toUpperCase());
        if(!TextUtils.isEmpty(item.getNickerName())){
            account_id.setVisibility(View.VISIBLE);
            account_id.setText("("+item.getVideoCallName()+")");
        }else{
            account_id.setVisibility(View.GONE);
        }
        if(!item.isCanSelected()){
            helper.getView(R.id.voice_comm_video_sel).setVisibility(View.INVISIBLE);
        }else {
            if (item.isSelected()) {
                helper.setImageResource(R.id.voice_comm_video_sel, R.drawable.sel);
            } else {
                helper.setImageResource(R.id.voice_comm_video_sel, R.drawable.unsel);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }
    public interface OnItemClickLis{
        public void OnItemClick(Status item);
    }
}
