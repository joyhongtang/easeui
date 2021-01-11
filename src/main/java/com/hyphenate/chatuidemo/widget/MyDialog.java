package com.hyphenate.chatuidemo.widget;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.hyphenate.easeui.R;

import java.util.ArrayList;


public class MyDialog extends Dialog implements View.OnClickListener {
    //在构造方法里提前加载了样式
    private Context context;//上下文
    private int layoutResID;//布局文件id

    public EditText getEditText() {
        return editText;
    }

    public void setEditText(EditText editText) {
        this.editText = editText;
    }

    private EditText editText,et_item_icon_ramark_name;
    String title, notice, inputHint;
    private TextView welcome_tag,notice_tag;
    boolean inputVisible;
    int ramarkNameVisible = View.GONE;
    private View et_item_icon_ramark_name_main;
    public MyDialog(Context context, int layoutResID) {
        super(context, R.style.EaseMyDialog);//加载dialog的样式
        this.context = context;
        this.layoutResID = layoutResID;
    }
    public void initInfo(String title, String notice, String inputHint,boolean inputVisible) {
        this.title = title;
        this.notice = notice;
        this.inputHint = inputHint;
        this.inputVisible = inputVisible;
        if(null != welcome_tag && !TextUtils.isEmpty(title)){
            welcome_tag.setText(title);
            notice_tag.setText(notice);
            editText.setText(inputHint);
            editText.setSelection(inputHint.length());
            if(!inputVisible){
                editText.setVisibility(View.GONE);
            }
        }
    }
    public void setRemarkNameVisible(int visible){
        ramarkNameVisible = visible;
        if(null !=et_item_icon_ramark_name_main){
            et_item_icon_ramark_name_main.setVisibility(ramarkNameVisible);
        }
    }

    public void initInfo(String title, String notice, String inputHint) {
        initInfo(title,notice,inputHint,true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //提前设置Dialog的一些样式
        Window dialogWindow = getWindow();
        dialogWindow.setGravity(Gravity.CENTER);//设置dialog显示居中
        //dialogWindow.setWindowAnimations();设置动画效果
        setContentView(layoutResID);


        WindowManager windowManager = ((Activity) context).getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = display.getWidth() * 2 / 5;// 设置dialog宽度为屏幕的4/5
        lp.height = lp.width * 3 / 5;
        getWindow().setAttributes(lp);
        setCanceledOnTouchOutside(true);//点击外部Dialog消失
        //遍历控件id添加点击注册
//        for(int id:listenedItem){
//            findViewById(id).setOnClickListener(this);
//        }
        dataList.add("Account");
        editText = findViewById(R.id.et_item_icon_edit_text);
        welcome_tag = findViewById(R.id.welcome_tag);
        notice_tag = findViewById(R.id.notice);
        et_item_icon_ramark_name = findViewById(R.id.et_item_icon_ramark_name);
        et_item_icon_ramark_name_main = findViewById(R.id.et_item_icon_ramark_name_main);
        et_item_icon_ramark_name_main.setVisibility(ramarkNameVisible);
        findViewById(R.id.scan_qr).setOnClickListener(this);
        findViewById(R.id.btn_cancle).setOnClickListener(this);
        findViewById(R.id.btn_sure).setOnClickListener(this);
        initInfo(title,notice,inputHint,inputVisible);
    }

    ArrayList<String> dataList = new ArrayList<String>();
    int selePopPositon = 0;
    private OnCenterItemClickListener listener;

    public interface OnCenterItemClickListener {
        void OnCenterItemClick(MyDialog dialog, int view);
    }

    //很明显我们要在这里面写个接口，然后添加一个方法
    public void setOnCenterItemClickListener(OnCenterItemClickListener listener) {
        this.listener = listener;
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_cancle) {
            dismiss();//注意：我在这里加了这句话，表示只要按任何一个控件的id,弹窗都会消失，不管是确定还是取消。
        }
        listener.OnCenterItemClick(this, v.getId());
    }

    public void showKeyboard() {
        if (editText != null) {
            //设置可获得焦点
            editText.setFocusable(true);
            editText.setFocusableInTouchMode(true);
            //请求获得焦点
            editText.requestFocus();
            //调用系统输入法
            InputMethodManager inputManager = (InputMethodManager) editText
                    .getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.showSoftInput(editText, 0);
        }
    }

    public void setInputContent(String content) {
        if (editText != null) {
            editText.setText(content);
        }
    }

    public String getInputContent() {
        if (editText != null) {
            String result = editText.getText().toString();
//            result = result.replace(" ", "");
            return result;
        }
        return "";
    }
    public String getRemarkContent() {
        if (et_item_icon_ramark_name != null) {
            String result = et_item_icon_ramark_name.getText().toString();
//            result = result.replace(" ", "");
            return result;
        }
        return "";
    }
    PopupWindow popupWindow;

    /**
     * 数据适配器
     *
     * @author caizhiming
     */
    class XCDropDownListAdapter extends BaseAdapter {

        Context mContext;
        ArrayList<String> mData;
        LayoutInflater inflater;

        public XCDropDownListAdapter(Context ctx, ArrayList<String> data) {
            mContext = ctx;
            mData = data;
            inflater = LayoutInflater.from(mContext);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            // 自定义视图
            ListItemView listItemView = null;
            if (convertView == null) {
            } else {
                listItemView = (ListItemView) convertView.getTag();
            }

            // 设置数据
            listItemView.tv.setText(mData.get(position).toString());
            final String text = mData.get(position).toString();
            listItemView.layout.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    try {
                        RelativeLayout itemView = (RelativeLayout) v;
                        View child = itemView.findViewById(R.id.text);
                        selePopPositon = Integer.parseInt(String.valueOf(child.getTag()));
                        editText.setText("");
                        editText.setHint(text);
                        closePopWindow();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
            return convertView;
        }

    }

    private static class ListItemView {
        TextView tv;
        RelativeLayout layout;
    }

    /**
     * 关闭下拉列表弹窗
     */
    private void closePopWindow() {
        popupWindow.dismiss();
        popupWindow = null;
    }

}