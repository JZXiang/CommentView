package comment.jzxiang.com.commentview;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import comment.jzxiang.com.commentview.adapter.DataAdapter;
import comment.jzxiang.com.commentview.view.CommentView;

public class ListViewActivity extends AppCompatActivity {
    private static final String TAG = "ListViewActivity";
    private List<String> mStrings;

    private String mBaseContent = "今天天气不错哈!";
    private StringBuilder mStringBuilder;
    int i = 1;

    public static void toActivity(Context context) {
        Intent intent = new Intent(context, ListViewActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);
        mStringBuilder = new StringBuilder();

        final CommentView commentview = (CommentView) findViewById(R.id.commentview);
        initData();

        final DataAdapter adapter = new DataAdapter(this, mStrings);
        commentview.setBaseAdapter(adapter);

        findViewById(R.id.btn_change_data).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBaseContent = "第" + i + "次更改数据.";
                mStringBuilder.setLength(0);
                initData();
                adapter.notifyDataSetChanged();
                i++;
            }
        });
    }

    void initData() {
        if (mStrings == null)
            mStrings = new ArrayList<>();

        mStrings.clear();
        for (int i = 0; i < 100; i++) {
            mStrings.add(createItemContent(i));
        }
    }

    String createItemContent(int position) {
        if (position != 0)
            mStringBuilder.append("\n");

        mStringBuilder.append(mBaseContent);
        mStringBuilder.append(position);
        return mStringBuilder.toString();
    }

}
