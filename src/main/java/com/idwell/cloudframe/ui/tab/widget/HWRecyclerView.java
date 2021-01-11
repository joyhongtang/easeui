package com.idwell.cloudframe.ui.tab.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.hyphenate.easeui.R;


/*
* 
* 描    述：仿华为文件管理RecyclerView
*/
public class HWRecyclerView extends RecyclerView {
    private float x;
    private float y;
    private View view;
    private CheckBox cbFind;
    private View viewFind;
    private int pos;
    private SparseArray<Boolean> positions = new SparseArray<>();


    public interface OnMoveCheckListener {
        void onMoveCheckListener(SparseArray<Boolean> positions);
    }

    private OnMoveCheckListener onMoveCheckListener;

    public void setOnMoveCheckListener(OnMoveCheckListener onMoveCheckListener) {
        this.onMoveCheckListener = onMoveCheckListener;
    }

    public void clecrCheckPositions() {
        positions.clear();
    }

    //    private CheckBox cbFind;


    public HWRecyclerView(Context context) {
        super(context, null);
    }

    public HWRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public HWRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getAction()) {

            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                x = e.getX();
                y = e.getY();

                if (x >= (getMeasuredWidth() - 150)) {

                    if (view != findChildViewUnder(x, y)) {

                        viewFind = findChildViewUnder(x, y);
                        if(viewFind  == null){
                            return false;
                        }
                        pos = (int) viewFind.getTag();

                        if (viewFind != null) {

                            cbFind = (CheckBox) viewFind.findViewById(R.id.cb_item);

                            if (viewFind != null) {
                                if (cbFind != null) {
                                    boolean checked = cbFind.isChecked();
                                    if (checked) {
                                        cbFind.setChecked(!checked);
                                    } else {
                                        cbFind.setChecked(!checked);
                                    }
                                    positions.put(pos, !checked);
                                    view = viewFind;
                                }
                            }
                        }
                    }
                    return false;
                }
                break;
            case MotionEvent.ACTION_UP:
                x = e.getX();
                if (x >= getMeasuredWidth() - 200) {
                    if (onMoveCheckListener != null) {
                        onMoveCheckListener.onMoveCheckListener(positions);
                    }
                }
                break;
            default:
                break;
        }

        return super.onTouchEvent(e);
    }
}
