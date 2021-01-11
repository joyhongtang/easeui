package com.hyphenate.chatuidemo.conference.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.annotation.NonNull;

import com.hyphenate.chatuidemo.ui.BaseActivity;

import java.util.List;
import java.util.Stack;

/**
 * Created by Administrator on 2016/11/24.
 */
public class ActivityPageManager {
    private static Stack<BaseActivity> activityStack;
    private static ActivityPageManager instance;

    /**
     * constructor
     */
    private ActivityPageManager() {

    }

    /**
     * get the AppManager instance, the AppManager is singleton.
     */
    public static ActivityPageManager getInstance() {
        if (instance == null) {
            instance = new ActivityPageManager();
        }
        return instance;
    }

    /**
     * add Activity to Stack
     */
    public void addActivity(BaseActivity activity) {
        if (activityStack == null) {
            activityStack = new Stack<BaseActivity>();
        }
        activityStack.add(activity);
    }

    public boolean exitActivity(String activityName) {
        boolean exist = false;
        if (activityStack == null || activityStack.isEmpty()) {
            return false;
        }
        for (Activity activity : activityStack) {
            if (activity.getComponentName().getClassName().contains(activityName)) {
                return true;
            }
        }
        return exist;
    }

    public BaseActivity findActivity(String activityName) {
        boolean exist = false;
        if (activityStack == null || activityStack.isEmpty()) {
            return null;
        }
        for (Activity activity : activityStack) {
            if (activity.getComponentName().getClassName().contains(activityName)) {
                return (BaseActivity) activity;
            }
        }
        return null;
    }

    /**
     * remove Activity from Stack
     */
    public void removeActivity(BaseActivity activity) {
        if (activityStack == null) {
            activityStack = new Stack<BaseActivity>();
        }
        activityStack.remove(activity);
    }

    /**
     * get current activity from Stack
     */
    public BaseActivity currentActivity() {
        if (null == activityStack || activityStack.isEmpty()) {
            return null;
        }
        BaseActivity activity = activityStack.lastElement();
        return activity;
    }

    /**
     * finish current activity from Stack
     */
    public void finishActivity() {
        Activity activity = activityStack.lastElement();
        finishActivity(activity);
    }

    /**
     * finish the Activity
     */
    public void finishActivity(Activity activity) {
        if (activity != null) {
            activityStack.remove(activity);
            activity.finish();
            activity = null;
        }
    }

    /**
     * finish the Activity
     */
    public void finishActivity(Class<?> cls) {
        for (Activity activity : activityStack) {
            if (activity.getClass().equals(cls)) {
                finishActivity(activity);
            }
        }
    }

    /**
     * finish all Activity
     */
    public void finishAllActivity() {
        if (activityStack != null && activityStack.size() > 0) {
            for (int i = 0, size = activityStack.size(); i < size; i++) {
                if (null != activityStack.get(i)) {
                    activityStack.get(i).finish();
                }
            }
            activityStack.clear();
        }

    }


    /**
     * release all resourse for view
     *
     * @param view
     */
    public static void unbindReferences(View view) {
        try {
            if (view != null) {
                view.destroyDrawingCache();
                unbindViewReferences(view);
                if (view instanceof ViewGroup) {
                    unbindViewGroupReferences((ViewGroup) view);
                }
            }
        } catch (Throwable e) {
            // whatever exception is thrown just ignore it because a crash is
            // always worse than this method not doing what it's supposed to do
        }
    }

    private static void unbindViewGroupReferences(ViewGroup viewGroup) {
        int nrOfChildren = viewGroup.getChildCount();
        for (int i = 0; i < nrOfChildren; i++) {
            View view = viewGroup.getChildAt(i);
            unbindViewReferences(view);
            if (view instanceof ViewGroup)
                unbindViewGroupReferences((ViewGroup) view);
        }
        try {
            viewGroup.removeAllViews();
        } catch (Throwable mayHappen) {
            // AdapterViews, ListViews and potentially other ViewGroups don't
            // support the removeAllViews operation
        }
    }

    @SuppressWarnings("deprecation")
    private static void unbindViewReferences(View view) {
        // set all listeners to null (not every view and not every API level
        // supports the methods)
        try {
            view.setOnClickListener(null);
            view.setOnCreateContextMenuListener(null);
            view.setOnFocusChangeListener(null);
            view.setOnKeyListener(null);
            view.setOnLongClickListener(null);
            view.setOnClickListener(null);
        } catch (Throwable mayHappen) {

        }

        // set background to null
        Drawable d = view.getBackground();
        if (d != null) {
            d.setCallback(null);
        }

        if (view instanceof ImageView) {
            ImageView imageView = (ImageView) view;
            d = imageView.getDrawable();
            if (d != null) {
                d.setCallback(null);
            }
            imageView.setImageDrawable(null);
            imageView.setBackgroundDrawable(null);
        }

        // destroy WebView
        if (view instanceof WebView) {
            WebView webview = (WebView) view;
            webview.stopLoading();
            webview.clearFormData();
            webview.clearDisappearingChildren();
            webview.setWebChromeClient(null);
            webview.setWebViewClient(null);
            webview.destroyDrawingCache();
            webview.destroy();
            webview = null;
        }

        if (view instanceof ListView) {
            ListView listView = (ListView) view;
            try {
                listView.removeAllViewsInLayout();
            } catch (Throwable mayHappen) {
            }
            ((ListView) view).destroyDrawingCache();
        }
    }

    /**
     * exit System
     *
     * @param context
     */
    public void exit(Context context) {
        exit(context, true);
    }

    /**
     * exit System
     *
     * @param context
     * @param isClearCache
     */
    @SuppressWarnings("deprecation")
    public void exit(Context context, boolean isClearCache) {
        try {
            finishAllActivity();
            if (context != null) {
                ActivityManager activityMgr = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                activityMgr.restartPackage(context.getPackageName());
            }
//            if(isClearCache){
//                LruCacheManager.getInstance().evictAll();
//                CacheManager.clearAll();
//            }
            System.exit(0);
            android.os.Process.killProcess(android.os.Process.myPid());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Finish to the activity.
     *
     * @param clz           The activity class.
     * @param isIncludeSelf True to include the activity, false otherwise.
     * @param isLoadAnim    True to use animation for the outgoing activity, false otherwise.
     */
    public boolean finishToActivity(@NonNull final Class<? extends Activity> clz,
                                    final boolean isIncludeSelf,
                                    final boolean isLoadAnim) {
        if (null == activityStack || activityStack.size() <= 0) {
            return false;
        }
        for (Activity act : activityStack) {
            if (act.getClass().equals(clz)) {
                if (isIncludeSelf) {
                    finishActivity(act, isLoadAnim);
                }
                return true;
            }
            finishActivity(act, isLoadAnim);
        }
        return false;
    }

    /**
     * Finish the activity.
     *
     * @param activity   The activity.
     * @param isLoadAnim True to use animation for the outgoing activity, false otherwise.
     */
    public static void finishActivity(@NonNull final Activity activity, final boolean isLoadAnim) {
        activity.finish();
        if (!isLoadAnim) {
            activity.overridePendingTransition(0, 0);
        }
    }
}