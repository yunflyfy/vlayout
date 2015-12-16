package com.alibaba.android.vlayout.layout;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import com.alibaba.android.vlayout.LayoutManagerHelper;
import com.alibaba.android.vlayout.Range;
import com.alibaba.android.vlayout.VirtualLayoutManager;
import com.alibaba.android.vlayout.VirtualLayoutManager.LayoutStateWrapper;

import static com.alibaba.android.vlayout.VirtualLayoutManager.VERTICAL;


/**
 *
 */
public abstract class AbstractFullFillLayoutHelper extends BaseLayoutHelper {

    private static final String TAG = AbstractFullFillLayoutHelper.class.getSimpleName();

    protected int getAllChildren(View[] toFill,
                                 RecyclerView.Recycler recycler, LayoutStateWrapper layoutState,
                                 LayoutChunkResult result, LayoutManagerHelper helper) {

        final boolean layingOutInPrimaryDirection = layoutState.getItemDirection() == LayoutStateWrapper.ITEM_DIRECTION_TAIL;

        int count = 0;
        int firstPos = layingOutInPrimaryDirection ? getRange().getLower() : getRange().getUpper();
        final int curPos = layoutState.getCurrentPosition();

        if (layingOutInPrimaryDirection ? (curPos > firstPos) : (curPos > firstPos)) {
            // do ugly bug fix now
            Log.w(TAG, "Please handle strange order views carefully");
        }

        while (count < toFill.length) {
            if (isOutOfRange(layoutState.getCurrentPosition()))
                break;

            View view = nextView(recycler, layoutState, helper, result);
            if (view == null) {
                break;
            }

            toFill[count] = view;

            // normalize layout params
            LayoutParams layoutParams = view.getLayoutParams();
            if (layoutParams == null) {
                view.setLayoutParams(generateDefaultLayoutParams());
            } else if (!checkLayoutParams(layoutParams)) {
                view.setLayoutParams(generateLayoutParams(layoutParams));
            }

            count++;
        }

        if (count > 0 && !layingOutInPrimaryDirection) {
            // reverse array
            int s = 0, e = count - 1;
            while (s < e) {
                View temp = toFill[s];
                toFill[s] = toFill[e];
                toFill[e] = temp;
                s++;
                e--;
            }
        }

        return count;
    }

    private LayoutManagerHelper mTempLayoutHelper;

    @Override
    public void layoutViews(RecyclerView.Recycler recycler, RecyclerView.State state,
                            LayoutStateWrapper layoutState, LayoutChunkResult result, LayoutManagerHelper helper) {
        mTempLayoutHelper = helper;

        doLayoutView(recycler, state, layoutState, result, helper);

        mTempLayoutHelper = null;
    }

    protected void doLayoutView(RecyclerView.Recycler recycler, RecyclerView.State state,
                                LayoutStateWrapper layoutState, LayoutChunkResult result, LayoutManagerHelper helper) {

    }

    // Mock measure/layout process in ViewGroup
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }

    protected final void setMeasuredDimension(int measuredWidth, int measuredHeight) {

    }

    @Override
    public void checkAnchorInfo(RecyclerView.State state, VirtualLayoutManager.AnchorInfoWrapper anchorInfo, LayoutManagerHelper helper) {
        if (anchorInfo.layoutFromEnd) {
            anchorInfo.position = getRange().getUpper();
        } else
            anchorInfo.position = getRange().getLower();
    }

    @Override
    public int computeAlignOffset(int offset, boolean isLayoutEnd, boolean useAnchor, LayoutManagerHelper helper) {
        final boolean layoutInVertical = helper.getOrientation() == VERTICAL;
        if (layoutInVertical) {
            if (isLayoutEnd) {
                return mMarginBottom;
            } else {
                return -mMarginTop;
            }
        } else {
            if (isLayoutEnd) {
                return mMarginRight;
            } else {
                return -mMarginLeft;
            }
        }
    }

    protected boolean checkLayoutParams(LayoutParams p) {
        return true;
    }

    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }

    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new LayoutParams(p);
    }


    @Override
    public boolean isRecyclable(int childPos, int startIndex, int endIndex, LayoutManagerHelper helper, boolean fromStart) {
        Range<Integer> range = getRange();
        if (range.contains(childPos)) {
            return Range.create(startIndex, endIndex).contains(range);
        } else {
            Log.w(TAG, "Child item not match");
            return true;
        }
    }
}
