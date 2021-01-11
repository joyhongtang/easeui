package com.idwell.cloudframe.ui.tab;

import android.content.Context;


import com.hyphenate.chatuidemo.Constant;
import com.hyphenate.easeui.R;
import com.idwell.cloudframe.ui.VideoCommunicationActivity;
import com.idwell.cloudframe.ui.tab.entity.CategoryEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dasu on 2017/4/20.
 * <p>
 * CategoryActivity对应的Controller
 */
public class CategoryAController {
    private static final String TAG = CategoryAController.class.getSimpleName();

    private VideoCommunicationActivity mCategoryActivity;
    //tab 标签页
    private List<String> mCategoryList;
    private List<CategoryEntity> mCategoryEntityList;


    public CategoryAController(Context context) {
        mCategoryActivity = (VideoCommunicationActivity) context;
        initVariable();
    }

    private void initVariable() {
        mCategoryList = new ArrayList<>();
        mCategoryEntityList = new ArrayList<>();
        mCategoryEntityList.add(new CategoryEntity(R.drawable.add_f, Constant.application.getString(R.string.im_my_friend)));
        mCategoryEntityList.add(new CategoryEntity(R.drawable.add_f,Constant.application.getString(R.string.im_group_chat)));
        mCategoryEntityList.add(new CategoryEntity(R.drawable.add_f,Constant.application.getString(R.string.im_recent_call)));
    }
    public List<CategoryEntity> getCategoryEntityList() {
        if (mCategoryEntityList == null) {
            mCategoryEntityList = new ArrayList<>();
        }
        return mCategoryEntityList;
    }

    public List<String> getCategoryList() {
        if (mCategoryList == null) {
            mCategoryList = new ArrayList<>();
        }
        return mCategoryList;
    }

}
