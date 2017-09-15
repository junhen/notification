package test.mvp;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.leui.notification.test.R;

/**
 * Created by xiaoxin on 17-9-12.
 * 让activity继承v接口，并且初始化p，让p持有m和v
 * http://blog.csdn.net/a243981326/article/details/73556892
 */

public class TestActivity extends Activity implements TestContract.View,View.OnClickListener {
    private TestContract.Presenter  presenter;
    private Button mBn;
    private TextView mTv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_mvp_activity);
        mBn = (Button) findViewById(R.id.tv_test);
        mBn.setOnClickListener(this);
        mTv = (TextView) findViewById(R.id.tv_test_text);
        presenter = new TestPresenter (TestModel.getInstance(), this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_test: // 回到顶部按钮
                presenter.getData();
                break;
        }
    }

    @Override
    public void showData(String str) {
        mTv.setText(str);
    }
}