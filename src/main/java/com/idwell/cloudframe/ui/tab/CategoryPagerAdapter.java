package com.idwell.cloudframe.ui.tab;

import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.idwell.cloudframe.ui.tab.entity.CategoryEntity;
import com.idwell.cloudframe.ui.tab.fragments.CommDetailFrag;
import com.idwell.cloudframe.ui.tab.fragments.GroupSelFrag;
import com.idwell.cloudframe.ui.tab.fragments.FriendFrag;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dasu on 2017/4/20.
 * <p>
 * ViewPager的适配器，用于根据List的tab名来生成对应的Fragment，每个Fragment必须传入其type的参数
 */

public class CategoryPagerAdapter extends FragmentPagerAdapter {

    private List<Fragment> mFragmentList;
    private List<CategoryEntity> mTabNameList;
    private Fragment mCurFragment;

    public CategoryPagerAdapter(FragmentManager fm, List<CategoryEntity> categoryList) {
        super(fm);
        mFragmentList = new ArrayList<>();
        mTabNameList = categoryList;
        for (int i = 0; i < categoryList.size(); i++) {
            Fragment f = null;
            if (i == 1) {
                f = new GroupSelFrag();
            } else if (i == 0) {
                f = new FriendFrag();
            } else if (i == 2) {
                f = new CommDetailFrag();
            } else {
                f = new Fragment();
            }
            mFragmentList.add(f);
        }
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList != null ? mFragmentList.size() : 0;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTabNameList.get(position).desp;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        mCurFragment = (Fragment) object;
    }

    Fragment getCurrentFragment() {
        return mCurFragment;
    }
}
