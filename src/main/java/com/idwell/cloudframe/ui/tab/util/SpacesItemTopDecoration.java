package com.idwell.cloudframe.ui.tab.util;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class SpacesItemTopDecoration extends RecyclerView.ItemDecoration {
    private int space;

    public int getColumeSize() {
        return columeSize;
    }

    public void setColumeSize(int columeSize) {
        this.columeSize = columeSize;
    }

    public int columeSize = 0;

    public SpacesItemTopDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {
        outRect.left = space;
        outRect.right = space;
        outRect.bottom = space;

        // Add top margin only for the first item to avoid double space between items
        if (parent.getChildPosition(view) == 0 || parent.getChildPosition(view) == 1 || parent.getChildPosition(view) == 2) {
            outRect.top = space;
        }
        if (columeSize == 4) {
            if (parent.getChildPosition(view) == 3) {
                outRect.top = space;
            }
        }
    }
}