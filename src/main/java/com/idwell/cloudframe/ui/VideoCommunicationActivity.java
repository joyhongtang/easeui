package com.idwell.cloudframe.ui;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.hyphenate.chatuidemo.entity.MessageEvent;
import com.hyphenate.easeui.R;
import com.idwell.cloudframe.ui.tab.CategoryAController;
import com.idwell.cloudframe.ui.tab.CategoryPagerAdapter;
import com.idwell.cloudframe.ui.tab.TabPageAdapter;

import org.jetbrains.annotations.NotNull;

public class VideoCommunicationActivity extends com.hyphenate.chatuidemo.ui.BaseActivity{
    View tab_bottom;
    RelativeLayout root;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private SwipeRefreshLayout mRefreshLayout;
    private TabPageAdapter tabAdapter;
    private CategoryPagerAdapter mPagerAdapter;
    private CategoryAController mCategoryController;

    private void initView() {
        root = findViewById(R.id.root);
        mCategoryController = new CategoryAController(this);
        mPagerAdapter = new CategoryPagerAdapter(getSupportFragmentManager(), mCategoryController.getCategoryEntityList());
        mTabLayout = (TabLayout) findViewById(R.id.layout_category_title);
        tab_bottom = findViewById(R.id.tab_bottom);
//        mTabLayout.setSelectedTabIndicatorHeight(0);
        mViewPager = (ViewPager) findViewById(R.id.vp_category_content);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);
        mTabLayout.setupWithViewPager(mViewPager);
        tabAdapter = new TabPageAdapter(getSupportFragmentManager(), this, mPagerAdapter, mCategoryController);
        findViewById(R.id.iv_back_e).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        for (int i = 0; i < mCategoryController.getCategoryEntityList().size(); i++) {
            TabLayout.Tab tab = mTabLayout.getTabAt(i);
            //注意！！！这里就是添加我们自定义的布局
            tab.setCustomView(tabAdapter.getCustomView(i));
            //这里是初始化时，默认item0被选中，setSelected（true）是为了给图片和文字设置选中效果，代码在文章最后贴出
            if (i == 0) {
                ((TextView) tab.getCustomView().findViewById(R.id.tab_tv)).setTextColor(getResources().getColor(R.color.white));
                ((ImageView) tab.getCustomView().findViewById(R.id.tab_iv)).setSelected(true);
            }
        }
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                ((ImageView) tab.getCustomView().findViewById(R.id.tab_iv)).setSelected(true);
                ((TextView) tab.getCustomView().findViewById(R.id.tab_tv)).setTextColor(getResources().getColor(R.color.white));
                mViewPager.setCurrentItem(tab.getPosition());
                if (null != tab_bottom) {
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                ((ImageView) tab.getCustomView().findViewById(R.id.tab_iv)).setSelected(false);
                ((TextView) tab.getCustomView().findViewById(R.id.tab_tv)).setTextColor(getResources().getColor(R.color.background));
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.layout_category_content);
        mRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.red),
                getResources().getColor(R.color.orange),
                getResources().getColor(R.color.pink));
        mRefreshLayout.setOnRefreshListener(onPullDownRefresh());
        mRefreshLayout.setEnabled(false);
    }
    private SwipeRefreshLayout.OnRefreshListener onPullDownRefresh() {
        return new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

            }
        };
    }

    @Override
    public int initLayout() {
        setShowTopBar(false);
        return R.layout.activity_category;
    }

    @Override
    public void initData() {
        initView();
    }

    @Override
    public void initListener() {
    }
}
