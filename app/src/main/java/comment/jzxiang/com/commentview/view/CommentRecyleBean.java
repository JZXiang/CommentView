package comment.jzxiang.com.commentview.view;

import android.view.View;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by jzxiang on 6/4/17.
 */

public class CommentRecyleBean {

    private List<View> mScapeViews;

    private CommentView mCommentView;

    public CommentRecyleBean(CommentView commentView) {
        mCommentView = commentView;
    }

    public int recyleItem(CommentView commentView, View view,int index) {
        addView(view);
        commentView.removeViewInLayout(view);

        return index;
    }

    public View getItem() {
        return getCachedView();
    }

    private void addView(View view) {
        if (mScapeViews == null)
            mScapeViews = new LinkedList<>();

        mScapeViews.add(view);
    }

    private View getCachedView() {
        if (mScapeViews != null && mScapeViews.size() > 0) {
            View view = mScapeViews.get(0);
            mScapeViews.remove(0);
            return view;
        }
        return null;
    }

    public void clearAll() {
        if (mScapeViews != null) {
            mScapeViews.clear();
        }
    }

    public void recyleAllView(CommentView commentView) {
        int count = commentView.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = commentView.getChildAt(i);
            addView(view);
        }
        commentView.detachAll();
    }
}
