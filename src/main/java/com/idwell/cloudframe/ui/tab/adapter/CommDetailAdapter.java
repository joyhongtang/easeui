package com.idwell.cloudframe.ui.tab.adapter;

import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.hyphenate.easeui.R;
import com.idwell.cloudframe.http.entity.ImCallInfo;

import java.util.ArrayList;

import static com.idwell.cloudframe.ui.tab.util.DataUtil.getFormatedDateTime;
import static com.idwell.cloudframe.ui.tab.util.DataUtil.getYearMonthDayHourMinuteSecond;
import static com.idwell.cloudframe.ui.tab.util.DataUtil.parseDuration;
import static com.idwell.cloudframe.ui.tab.util.DataUtil.timeParse;

/**
 * https://github.com/CymChad/BaseRecyclerViewAdapterHelper
 */
public class CommDetailAdapter extends BaseMultiItemQuickAdapter<ImCallInfo, BaseViewHolder> {
    OnItemClickLis  onItemClickLis;
    boolean showChecked = false;
    public ArrayList<ImCallInfo> checkedList = new ArrayList<>();
    public CommDetailAdapter(ArrayList<ImCallInfo> dataSize, OnItemClickLis  onItemClickLis) {
        super(dataSize);
        this.onItemClickLis = onItemClickLis;
        addItemType(0, R.layout.home_comm_detail);  //1
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, ImCallInfo item) {
        helper.setText(R.id.chatters,item.getChatters());
        helper.setText(R.id.chat_start_time,getFormatedDateTime("yyyy-MM-dd HH:mm:ss",Long.parseLong(item.getChatStartTime())));
        CheckBox cb_item = helper.getView(R.id.cb_item);
        if(showChecked){
            cb_item.setVisibility(View.VISIBLE);
            cb_item.setChecked(item.isChecked());
        }else {
            checkedList.clear();
            item.setChecked(false);
            cb_item.setVisibility(View.INVISIBLE);
            cb_item.setChecked(item.isChecked());
        }
        cb_item.setTag(item);
        helper.itemView.setTag(helper.getLayoutPosition());
        String durTime = "";
        try {
            long dur = Long.parseLong(item.getChatEndTime() )- Long.parseLong(item.getChatStartTime());
            durTime = parseDuration(dur);
        }catch (Exception e){
            e.printStackTrace();
        }
        cb_item.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    ((ImCallInfo) buttonView.getTag()).setChecked(true);
                }else{
                    ((ImCallInfo) buttonView.getTag()).setChecked(false);
                }
            }
        });
        helper.setText(R.id.chat_during,durTime);
        helper.setText(R.id.chat_type,item.getChatType());
        helper.getView(R.id.main_view).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showChecked = !showChecked;
                onItemClickLis.onLongClickLis(showChecked);
                notifyDataSetChanged();
                return false;
            }
        });

    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }
    public interface OnItemClickLis{
        public void OnItemClick(ImCallInfo item);
        public void onLongClickLis(boolean clicked);
    }
}
