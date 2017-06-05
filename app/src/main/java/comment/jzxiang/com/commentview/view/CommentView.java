package comment.jzxiang.com.commentview.view;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.Scroller;

/**
 * Created by jzxiang on 6/3/17.
 */

public class CommentView extends ViewGroup {
    private static final String TAG = "CommentView";
    private int mLastX, mLastY, mLastXIntercept, mLastYIntercept;

    private VelocityTracker mVelocityTracker;
    private Scroller mScroller;

    private int mTouchSlop, mMinimumVelocity, mMaximumVelocity;

    private Adapter mBaseAdapter;

    private CommentRecyleBean mRecyleBins;

    private int mFirstIndex = 0, mLastIndex = 0;

    private boolean mAddFirst = false, mDataChanged = false;

    private int mLastFlingY = 0;

    public CommentView(Context context) {
        super(context);
        init();
    }

    public CommentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CommentView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CommentView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mScroller = new Scroller(getContext());
        mRecyleBins = new CommentRecyleBean(this);

        ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mBaseAdapter == null || mBaseAdapter.getCount() == 0) {
            setMeasuredDimension(0, 0);
            return;
        }
        int childCount = getChildCount();
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int totalHeight = 0;
        int totalWidth = 0;

        if (childCount == 0) {

            mLastIndex = mFirstIndex;
            int firstTop = 0;

            while (totalHeight < heightSize) {
                View view = getItemView(mLastIndex);
                if (view == null || view.getVisibility() == GONE)
                    continue;

                boolean isFirst = (mLastIndex == mFirstIndex);

                measureChild(view, widthMeasureSpec, heightMeasureSpec);
                if (mDataChanged) {

                    if (isFirst)
                        firstTop = view.getTop();

                    attachViewToParent(view, -1, view.getLayoutParams());
                } else {
                    addViewInLayout(view, -1, view.getLayoutParams());
                }

                totalHeight += isFirst ? (view.getMeasuredHeight() + firstTop) : view.getMeasuredHeight();
                if (totalHeight < heightSize) {
                    mLastIndex++;
                }
                totalWidth = Math.max(totalWidth, view.getMeasuredWidth());
            }
            mDataChanged = false;
        } else {
            for (int i = 0; i < childCount; i++) {
                View view = getChildAt(i);
                measureChild(view, widthMeasureSpec, heightMeasureSpec);
                totalHeight += view.getMeasuredHeight();
                totalWidth = Math.max(totalWidth, view.getMeasuredWidth());
            }
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            totalHeight = heightSize;
        } else {
            totalHeight = Math.min(heightSize, totalHeight);
        }

        setMeasuredDimension(totalWidth, totalHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();
        if (childCount == 0)
            return;

        View firstView = getChildAt(0);
        int preChildTop;

        if (mAddFirst) {
            preChildTop = -firstView.getMeasuredHeight();
            mAddFirst = false;
        } else {
            preChildTop = firstView.getTop();
        }

        int preChildLeft = 0;
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            if (childView.getVisibility() == GONE)
                continue;

            childView.layout(preChildLeft, preChildTop,
                    preChildLeft + childView.getMeasuredWidth(),
                    preChildTop + childView.getMeasuredHeight());
            preChildTop += childView.getMeasuredHeight();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int x = (int) ev.getX();
        int y = (int) ev.getY();

        boolean intercept = false;
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastFlingY = 0;
                mLastX = x;
                mLastY = y;
                mLastXIntercept = x;
                mLastYIntercept = y;

                intercept = false;
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                    intercept = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(y - mLastYIntercept) > mTouchSlop) {
                    intercept = true;
                } else {
                    intercept = false;
                }
                break;
            case MotionEvent.ACTION_UP:
                intercept = false;
                break;
        }

        return intercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                obtainVelocityTracker(event);
                int delayY = y - mLastY;
                trackMotionScroll(delayY);
                break;
            case MotionEvent.ACTION_UP:
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                int initialVelocity = (int) velocityTracker.getYVelocity();
                if ((Math.abs(initialVelocity) > mMinimumVelocity)
                        && getChildCount() > 0) {
                    fling(initialVelocity);
                }

                releaseVelocityTracker();
                break;
        }

        mLastX = x;
        mLastY = y;
        return true;
    }

    public Adapter getBaseAdapter() {
        return mBaseAdapter;
    }

    public int getLastIndex() {
        return mLastIndex;
    }

    public int getFirstIndex() {
        return mFirstIndex;
    }

    /**
     * @param deltaY
     * @return 是否停止惯性滚动
     */
    private boolean trackMotionScroll(int deltaY) {
        int childCount = getChildCount();
        if (childCount == 0)
            return false;

        boolean isStopScroll = false;
        if (mFirstIndex == 0) {
            int top = getChildAt(0).getTop();
            if (top + deltaY > 0) {
                deltaY = -top;
                isStopScroll = true;
            }
        }

        this.offsetChildTopAndBottom(deltaY);

        View firstView = getChildAt(0);
        View lastView = getChildAt(childCount - 1);

        int lastBottom = lastView.getBottom();
        int lastTop = lastView.getTop();

        int firstTop = firstView.getTop();
        int firstBottom = firstView.getBottom();
        int parentHeight = getMeasuredHeight();
//        Log.e(TAG, "trackMotionScroll: lastBottom = " + lastBottom +
//                "//lastTop = " + lastTop + "//firstTop = " + firstTop + "//firstBottom = " + firstBottom);
        if (deltaY > 0) {
            //down
            if (lastTop > parentHeight) {
                Log.e(TAG, "trackMotionScroll:down the view is scape .");
                mRecyleBins.recyleItem(this, lastView, childCount - 1);
                mLastIndex--;
            }

            if (firstTop >= 0 && mFirstIndex > 0) {
                Log.e(TAG, "trackMotionScroll: down the view is add .");
                mFirstIndex--;
                View view = getItemView(mFirstIndex);
                addViewInLayout(view, 0, view.getLayoutParams());
                mAddFirst = true;
                requestLayout();
            }

        } else {
            //up
            if (firstBottom <= 0) {
                Log.e(TAG, "trackMotionScroll: up the view is scape .");
                mRecyleBins.recyleItem(this, firstView, 0);
                mFirstIndex++;
            }

            if (lastBottom <= parentHeight && mLastIndex < (mBaseAdapter.getCount() - 1)) {
                Log.e(TAG, "trackMotionScroll: up the view is add .");
                mLastIndex++;
                View view = getItemView(mLastIndex);
                addViewInLayout(view, -1, view.getLayoutParams());
                requestLayout();
            }
        }
        return isStopScroll;
    }

    private void offsetChildTopAndBottom(int offset) {
        final int count = getChildCount();

        for (int i = 0; i < count; i++) {
            final View v = getChildAt(i);

            v.setTop(v.getTop() + offset);
            v.setBottom(v.getBottom() + offset);
        }

    }

    private View getItemView(int index) {
        if (mBaseAdapter == null || mBaseAdapter.getCount() == 0) {
            return null;
        }

        return mBaseAdapter.getView(index, mRecyleBins.getItem(), this);
    }

    public void setBaseAdapter(BaseAdapter baseAdapter) {
        if (mBaseAdapter != null)
            mBaseAdapter.unregisterDataSetObserver(dataObserver);

        mBaseAdapter = baseAdapter;
        if (mBaseAdapter != null)
            mBaseAdapter.registerDataSetObserver(dataObserver);

        requestLayout();
    }

    public void invalidateView(boolean clearCaches) {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }

        mRecyleBins.clearAll();
        reset();

        requestLayout();
    }

    // Adapter listener
    private DataSetObserver dataObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            Log.e(TAG, "onChanged: ");
            invalidateView(false);
        }

        @Override
        public void onInvalidated() {
            Log.e(TAG, "onInvalidated: ");
            invalidateView(true);
        }
    };

    private void obtainVelocityTracker(MotionEvent event) {

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }

        mVelocityTracker.addMovement(event);
    }

    private void releaseVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    public void fling(int velocityY) {
        Log.e(TAG, "fling: velocityY = " + velocityY);
        mScroller.fling(getScrollX(), getScrollY(), 0, velocityY, 0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
        invalidate();
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            int y = mScroller.getCurrY();
            if (mLastFlingY != 0) {
                int delatY = y - mLastFlingY;
                if (trackMotionScroll(delatY)) {
                    mScroller.abortAnimation();
                    return;
                }
            }
            mLastFlingY = y;
            postInvalidate();
        }
    }

    private void reset() {
//        mFirstIndex = 0;
        mLastIndex = 0;
        mDataChanged = true;
        mRecyleBins.recyleAllView(this);
    }

    public void detachAll() {
        detachAllViewsFromParent();
    }

}
