package com.idwell.cloudframe.ui.tab;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.hyphenate.easeui.R;
import com.idwell.cloudframe.ui.tab.entity.CategoryEntity;


public class TabPageAdapter extends FragmentPagerAdapter {
    final int PAGE_COUNT=4;
    private Context context;
    CategoryPagerAdapter categoryPagerAdapter;
    CategoryAController c;
    public TabPageAdapter(FragmentManager fm, Context context, CategoryPagerAdapter categoryPagerAdapter, CategoryAController c) {
        super(fm);
        this.categoryPagerAdapter = categoryPagerAdapter;
        this.context=context;
        this.c = c;
    }

    @Override
    public Fragment getItem(int position) {
        return categoryPagerAdapter.getItem(position);
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }
    //注意！！！这里就是我们自定义的布局tab_item
    public View getCustomView(int position){
        View view= LayoutInflater.from(context).inflate(R.layout.tab_item,null);
        ImageView iv= (ImageView) view.findViewById(R.id.tab_iv);
        TextView tab_tv= (TextView) view.findViewById(R.id.tab_tv);
        CategoryEntity entity = c.getCategoryEntityList().get(position);
        iv.setImageResource(entity.getImageID());
        tab_tv.setText(entity.desp);
        return view;
    }
}