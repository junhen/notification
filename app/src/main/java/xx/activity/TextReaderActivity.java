package xx.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import xx.util.FileUtil;
import xx.util.SharePreferencesUtil;

import com.leui.notification.test.R;

public class TextReaderActivity extends Activity {
    
    private static final String TAG = "TextReaderActivity_TAG";

    //对应手机中“学习资料的位置”，需要收到加压到sdcard目录下新建子目录studio中
    private static final String STUDIO_PATH = Environment.getExternalStorageDirectory() + "/studio";
    private static final int REQUEST_CODE = 0x01;

    private ListView listView;
    private ImageButton imageButton;
    private TextView textView;
    private ScrollView mScroll;
    private TextView mText;
    private Button mDirectStudio;
    private ImageView mImageView;

    private List<String> list;
    private MyAdapter adapter;
    private String mFilePath;
    private Thread mThread;
    //当需要退出子线程时，置为true
    private boolean mStopThread = true;
    //当此值为2时，点击返回键退出activity,第一次点击返回键是弹出一个对话框
    private int mbackIndex = 0;

    //TextView的设置相关
    private float mTextScale;
    private float mTextSize;
    private float mSpacingMultiplier;
    private int mTextBackgroundColor;

    //加载txt文件或图片时使用子线程加载
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0x01) {
                listView.setVisibility(View.GONE);
                mScroll.setVisibility(View.VISIBLE);
                mText.setText((String) msg.obj);
            } else if (msg.what == 0x02) {
                listView.setVisibility(View.GONE);
                mImageView.setVisibility(View.VISIBLE);
                mImageView.setImageBitmap((Bitmap) msg.obj);
            }
        }
    };

    private final Runnable mLoadTextRunnable = new Runnable() {
        @Override
        public void run() {
            mStopThread = false;
            File file = new File(mFilePath);
            String str;
            InputStreamReader input = null;
            BufferedReader reader = null;
            try {
                input = new InputStreamReader(new FileInputStream(file), "UTF-8");
                reader = new BufferedReader(input);
                //mHandler.obtainMessage(0x01).sendToTarget();
                StringBuilder sb = new StringBuilder();
                while ((str = reader.readLine()) != null) {
                    if (mStopThread) return;
                    sb.append(str);
                    sb.append("\n");
                }
                Message message = mHandler.obtainMessage(0x01, sb.toString());
                message.sendToTarget();

            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Toast.makeText(TextReaderActivity.this, "不是文档文件", Toast.LENGTH_LONG).show();
            } finally {
                try {
                    if (input != null)
                        input.close();
                    if (reader != null)
                        reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };


    private final Runnable mLoadPictureRunnable = new Runnable() {
        @Override
        public void run() {
            mStopThread = false;
            Bitmap bitmap = null;
            try {
                bitmap = BitmapFactory.decodeFile(mFilePath);
                Message message = mHandler.obtainMessage(0x02, bitmap);
                message.sendToTarget();

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Toast.makeText(TextReaderActivity.this, "找不到图片", Toast.LENGTH_LONG).show();
            } finally {
                if (bitmap != null) {
                    //bitmap.recycle();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_reader);
        init();
        list = new ArrayList<String>();
        //File path = Environment.getRootDirectory();
        File path = Environment.getExternalStorageDirectory();
        textView.setText(path.toString());
        getAllFile(path);
        adapter = new MyAdapter(TextReaderActivity.this, list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                handleLoadText(position);
            }
        });
        imageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                handleFallback();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.text_reader_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.scale_up:
                mTextScale = (float)(mText.getScaleX() * 1.2);
                mText.setScaleX(mTextScale);
                mText.setScaleY(mTextScale);
                SharePreferencesUtil.putSP(this, SharePreferencesUtil.TEXT_SCALE, mTextScale);
                //mText.setTextScaleX((float)(mText.getTextScaleX() * 1.1));
                return true;
            case R.id.scale_down:
                mTextScale = (float)(mText.getScaleX() / 1.2);
                mText.setScaleX(mTextScale);
                mText.setScaleY(mTextScale);
                SharePreferencesUtil.putSP(this, SharePreferencesUtil.TEXT_SCALE, mTextScale);
                //mText.setTextScaleX((float)(mText.getTextScaleX() * 0.9));
                return true;

            case R.id.size_up:
                mTextSize = (float)(mText.getTextSize() * 1.2 / getResources().getDisplayMetrics().scaledDensity);
                mText.setTextSize(mTextSize);
                SharePreferencesUtil.putSP(this, SharePreferencesUtil.TEXT_SIZE, mTextSize);
                return true;

            case R.id.size_down:
                mTextSize = (float)(mText.getTextSize() / 1.2 / getResources().getDisplayMetrics().scaledDensity);
                mText.setTextSize(mTextSize);
                SharePreferencesUtil.putSP(this, SharePreferencesUtil.TEXT_SIZE, mTextSize);
                return true;

            case R.id.line_multiplier_up:
                mSpacingMultiplier = (float)(mText.getLineSpacingMultiplier() * 1.2);
                mText.setLineSpacing(0, mSpacingMultiplier);
                SharePreferencesUtil.putSP(this, SharePreferencesUtil.TEXT_LINE_MULTIPLIER, mSpacingMultiplier);
                return true;

            case R.id.line_multiplier_down:
                mSpacingMultiplier = (float)(mText.getLineSpacingMultiplier() / 1.2);
                mText.setLineSpacing(0, mSpacingMultiplier);
                SharePreferencesUtil.putSP(this, SharePreferencesUtil.TEXT_LINE_MULTIPLIER, mSpacingMultiplier);
                return true;

            case R.id.background_color:
                //Intent colorPicker = new Intent(this, ColorPickerDialogActivity.class);
                Intent colorPicker = new Intent("xx.activity.ColorPickerDialogActivity");
                startActivityForResult(colorPicker, REQUEST_CODE);
                return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG,"onActivityResult    "+",  requestCode: "+requestCode+",   resultCode: "+resultCode);
        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            mTextBackgroundColor = data.getIntExtra("color", 0x88888888);
            mText.setBackgroundColor(mTextBackgroundColor);
            SharePreferencesUtil.putSP(this, SharePreferencesUtil.TEXT_BACKGROUND_COLOR, mTextBackgroundColor);
            Log.d(TAG,"onActivityResult colorResult : "+Integer.toHexString(mTextBackgroundColor));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mStopThread = true;
    }

    private void init() {
        listView = (ListView) findViewById(R.id.direction_list);
        imageButton = (ImageButton) findViewById(R.id.text_reader_fallback);
        textView = (TextView) findViewById(R.id.text_reader_path);
        mScroll = (ScrollView) findViewById(R.id.text_reader_scroll);
        mText = (TextView) findViewById(R.id.text_reader_text);
        mDirectStudio = (Button) findViewById(R.id.direct_studio);
        mDirectStudio.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                listView.setVisibility(View.VISIBLE);
                mScroll.setVisibility(View.GONE);
                mImageView.setVisibility(View.GONE);
                imageButton.setClickable(true);
                String studioPath = STUDIO_PATH;
                textView.setText(studioPath);
                getAllFile(new File(studioPath));
                adapter.setList(list);
                listView.setAdapter(adapter);
            }
        });
        mImageView = (ImageView) findViewById(R.id.image_view);
        mbackIndex = 0;

        mTextScale = SharePreferencesUtil.getSP(this, SharePreferencesUtil.TEXT_SCALE, textView.getScaleX());
        mTextSize = SharePreferencesUtil.getSP(this, SharePreferencesUtil.TEXT_SIZE, textView.getTextSize());
        mSpacingMultiplier = SharePreferencesUtil.getSP(this, SharePreferencesUtil.TEXT_LINE_MULTIPLIER, mText.getLineSpacingMultiplier());
        mTextBackgroundColor = SharePreferencesUtil.getSP(this, SharePreferencesUtil.TEXT_BACKGROUND_COLOR, 0x888888);
        mText.setScaleX(mTextScale);
        mText.setScaleY(mTextScale);
        mText.setTextSize(mTextSize);
        mText.setLineSpacing(0, mSpacingMultiplier);
        mText.setBackgroundColor(mTextBackgroundColor);
        Log.d(TAG,"init    "+",   mTextScale: " +mTextScale
                +",   mTextSize: " +mTextSize
                +",   mSpacingMultiplier: " +mSpacingMultiplier
                +",   mTextBackgroundColor: "+mTextBackgroundColor);
    }

    private void loaderTextWithoutTyep(String path) {
        StringBuffer sb = new StringBuffer();
        File file = new File(path);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            mText.setText(sb.toString());
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //系统返回键添加监听事件
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mbackIndex < 1) {
            handleFallback();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void handleFallback() {
        if (textView.getText().equals(Environment.getExternalStorageDirectory().toString())) {
            mbackIndex++;
            new AlertDialog.Builder(TextReaderActivity.this)
                    .setMessage("确认退出吗?")
                    .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //点击确认后退出程序
                            finish();
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .create().show();
        } else {
            listView.setVisibility(View.VISIBLE);
            mScroll.setVisibility(View.GONE);
            mImageView.setVisibility(View.GONE);
            imageButton.setClickable(true);
            File fileParentPath = new File(textView.getText().toString()).getParentFile();
            textView.setText(fileParentPath.toString());
            getAllFile(fileParentPath);
            adapter.setList(list);
            listView.setAdapter(adapter);
        }
        if (mThread != null && mThread.isAlive())
            mStopThread = true;
    }

    private void handleLoadText(int position) {
        String path = list.get(position);
        textView.setText(path);
        File file = new File(path);
        if (file.isDirectory()) {
            getAllFile(file);
            adapter.setList(list);
            listView.setAdapter(adapter);
        } else {
            String postfix = path.substring(path.lastIndexOf(".")+1);
            Log.d(TAG,"handleLoadText    "+",   postfix: " +postfix);
            if(postfix.equalsIgnoreCase("png") 
                    || postfix.equalsIgnoreCase("jpeg") 
                    || postfix.equalsIgnoreCase("bpm")
                    || postfix.equalsIgnoreCase("jpg")) {
                loaderPicture(path);
            }else {
                loaderTextWithTyep(path);
            }
        }
    }

    //遍历文件夹
    public void getAllFile(File dir) {
        FileUtil.getAllFile(dir, list);
    }

    //加载带格式文本文件
    private void loaderTextWithTyep(String path) {
        mFilePath = path;
        mThread = new Thread(mLoadTextRunnable);
        mThread.start();
    }

    //加载图片
    private void loaderPicture(String path) {
        mFilePath = path;
        mThread = new Thread(mLoadPictureRunnable);
        mThread.start();
    }

    static class MyAdapter extends BaseAdapter {

        private Context context;
        private List<String> list;

        public MyAdapter(Context context, List<String> list) {
            this.context = context;
            this.list = list;
        }

        public void setList(List<String> list) {
            this.list = list;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder vh;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.text_reder_list_item, null);
                vh = new ViewHolder();
                vh.imageView = (ImageView) convertView.findViewById(R.id.list_item_direction);
                vh.tv_path = (TextView) convertView.findViewById(R.id.list_item_file);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }
            String path = list.get(position);
            File file = new File(path);
            String subPath = path.substring(path.lastIndexOf("/") + 1);
            if (file.isDirectory()) {
                vh.imageView.setImageResource(R.drawable.direction);
                vh.tv_path.setText(subPath);
            } else {
                vh.imageView.setImageResource(R.drawable.file);
                vh.tv_path.setText(subPath);
            }
            return convertView;
        }

        class ViewHolder {
            private ImageView imageView;
            private TextView tv_path;
        }
    }
}
