package com.idwell.cloudframe.ui.tab.fragments;

import android.text.TextUtils;

import androidx.fragment.app.Fragment;

import com.hyphenate.chatuidemo.widget.MyDialog;
import com.hyphenate.easeui.R;
import com.idwell.cloudframe.data.db.MyCallDatabase;
import com.idwell.cloudframe.http.entity.ImAddFriendInfo;

import java.util.Timer;
import java.util.TimerTask;

public abstract class FreindManagerFrag extends Fragment {
    public MyDialog updateNickerNameDialog;
    public abstract void updateUI(int updatePos);

    void updateNickerName(ImAddFriendInfo item) {
        //定义一个自己的dialog
        String hintContent = TextUtils.isEmpty(item.getNickerName())?item.getVideoCallName():item.getNickerName();
        updateNickerNameDialog = new MyDialog(this.getActivity(), R.layout.activity_add_freind_dialog);
        updateNickerNameDialog.initInfo(getResources().getString(R.string.update_account_title),
                getResources().getString(R.string.update_account_title_notice),hintContent);
        //绑定点击事件
        updateNickerNameDialog.setOnCenterItemClickListener(new MyDialog.OnCenterItemClickListener() {
            @Override
            public void OnCenterItemClick(MyDialog dialog, int view) {
                int id = view;
                if (id == R.id.btn_cancle) {

                } else if (id == R.id.btn_sure) {
                    if (dialog != null) {
                        item.setNickerName(dialog.getInputContent());
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                MyCallDatabase.Companion.getInstance().getImDao().update(item);
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        updateUI(item.getPostion());
                                        if(null != updateNickerNameDialog && updateNickerNameDialog.isShowing()){
                                            updateNickerNameDialog.dismiss();
                                        }
                                    }
                                });
                            }
                        }).start();
                    }

                }
            }
        });
        //显示
        updateNickerNameDialog.show();
        //调用点击函数
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateNickerNameDialog.showKeyboard();  
                    }
                });
            }
        };
        timer.schedule(timerTask, 600);
    }

    void deleteFriend(ImAddFriendInfo item, MyDialog.OnCenterItemClickListener onCenterItemClickListener) {
        //定义一个自己的dialog
        String hintContent = TextUtils.isEmpty(item.getNickerName())?item.getVideoCallName():item.getNickerName();
        updateNickerNameDialog = new MyDialog(this.getActivity(), R.layout.activity_add_freind_dialog);
        updateNickerNameDialog.initInfo(getResources().getString(R.string.delete_friend),
                getResources().getString(R.string.delete_friend_notice),hintContent,false);
        //绑定点击事件
        updateNickerNameDialog.setOnCenterItemClickListener(new MyDialog.OnCenterItemClickListener() {
            @Override
            public void OnCenterItemClick(MyDialog dialog, int view) {
                int id = view;
                if (id == R.id.btn_cancle) {

                } else if (id == R.id.btn_sure) {
                    if(null != onCenterItemClickListener){
                        //delete friend
                        onCenterItemClickListener.OnCenterItemClick(updateNickerNameDialog,1);
                    }
                    updateNickerNameDialog.dismiss();
                }
            }
        });
        //显示
        updateNickerNameDialog.show();
    }

}
