package comment.jzxiang.com.commentview.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import comment.jzxiang.com.commentview.view.TestTextView;

/**
 * Created by jzxiang on 6/3/17.
 */

public class DataAdapter extends BaseAdapter {
    private static final String TAG = "DataAdapter";
    private Context mContext;

    private List<String> mStrings;

    public DataAdapter(Context context, List<String> strings) {
        mContext = context;
        mStrings = strings;
    }

    @Override
    public int getCount() {
        return mStrings.size();
    }

    @Override
    public Object getItem(int position) {
        return mStrings.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHoler holer = null;
        if (convertView == null) {
            holer = new ViewHoler();
            holer.createView(mContext);
            convertView = holer.mTextView;
            convertView.setTag(holer);
        } else {
            holer = (ViewHoler) convertView.getTag();
        }
        holer.bindView(mContext, mStrings.get(position));

        switch (position % 2) {
            case 0:
                holer.mTextView.setBackgroundColor(0xff00ffff);
                break;
            case 1:
                holer.mTextView.setBackgroundColor(0xffff00ff);
                break;
        }
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "onClick: " + position);
                Toast.makeText(mContext, "position = " + position, Toast.LENGTH_SHORT).show();
            }
        });
        return convertView;
    }

    static class ViewHoler {
        TextView mTextView;

        public ViewHoler() {
        }

        void createView(Context context) {
            if (mTextView != null)
                return;

            mTextView = new TestTextView(context);
            AbsListView.LayoutParams params = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            mTextView.setLayoutParams(params);
        }

        void bindView(Context context, String content) {
            if (mTextView == null)
                createView(context);

            mTextView.setText(content);
        }

    }

}
