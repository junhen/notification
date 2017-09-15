package xx.json.parser;

import android.content.Context;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.leui.notification.test.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import xx.util.FileUtil;
import xx.util.SharePreferencesUtil;

public class JsonParserActivity extends Activity implements View.OnClickListener{

    private static final String TAG = "JsonParserActivity";
    public static final String TEXT_PATH = "json.js";
    //常量，为编码格式
    public static final String ENCODING = "UTF-8";


    private Button mPersonBn;
    private Button mPersonsBn;
    private Button mStringsBn;
    private Button mMapsBn;

    private Button mCreateBn;
    private Button mParserBn;
    private Button mParserDirectBn;

    private TextView mJsonText;
    private TextView mCreateText;
    private TextView mParserText;

    private String mJsonStr;
    private String mActionStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_json_parser);


        mPersonBn = (Button) findViewById(R.id.json_action_person);
        mPersonBn.setOnClickListener(this);
        mPersonsBn = (Button) findViewById(R.id.json_action_persons);
        mPersonsBn.setOnClickListener(this);
        mStringsBn = (Button) findViewById(R.id.json_action_strings);
        mStringsBn.setOnClickListener(this);
        mMapsBn = (Button) findViewById(R.id.json_action_maps);
        mMapsBn.setOnClickListener(this);

        mActionStr = SharePreferencesUtil.getSP(this, SharePreferencesUtil.JSON_TEST, "person");
        mCreateBn = (Button) findViewById(R.id.json_create);
        mCreateBn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createJsonStr(mActionStr);
            }
        });
        mParserBn = (Button) findViewById(R.id.json_parser);
        mParserBn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mParserText.setText(parserJsonStr(mActionStr));
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        });
        mParserDirectBn = (Button) findViewById(R.id.json_parser_direct);
        mParserDirectBn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //mParserText.setText(parserJsonStrDirect(mActionStr));
                    String result="";
                    try {
                        InputStream fin = getAssets().open("test.js");
                        //获取文件长度
                        int lenght = fin.available();
                        byte[] buffer = new byte[lenght];
                        fin.read(buffer);
                        fin.close();
                        //将byte数组转换成指定格式的字符串
                        result = new String(buffer, ENCODING);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    JSONObject jsonObject = new JSONObject(result);
                    String resultcode = jsonObject.getString("resultcode");
                    String reason = jsonObject.getString("reason");
                    int error_code = jsonObject.getInt("error_code");
                    JSONArray ja = jsonObject.getJSONArray("result");
                    StringBuilder sb = new StringBuilder();
                    sb.append(resultcode).append("\n")
                            .append(reason).append("\n")
                            .append(error_code+"").append("\n")
                            .append(ja.toString()).append("\n");
                    mParserText.setText(sb.toString());
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        });
        mCreateText = (TextView) findViewById(R.id.json_create_text);
        mParserText = (TextView) findViewById(R.id.json_parser_text);
        mJsonText = (TextView) findViewById(R.id.json_text);
        mJsonText.setText("json_text: " + mActionStr);
    }

    private void createJsonStr(String actionString) {
        if (actionString.equals("person")) {
            mJsonStr = JsonTools.createJsonString("person", DataUtil.getPerson());
        } else if (actionString.equals("persons")) {
            mJsonStr = JsonTools.createJsonString("persons", DataUtil.getPersons());
        } else if (actionString.equals("strings")) {
            mJsonStr = JsonTools.createJsonString("strings", DataUtil.getStrings());
        } else if (actionString.equals("maps")) {
            mJsonStr = JsonTools.createJsonString("maps", DataUtil.getMaps());
        }
        try {
            String json = mJsonStr;  //序列化
            FileOutputStream fos = openFileOutput(TEXT_PATH, Context.MODE_PRIVATE);
            fos.write(json.getBytes(ENCODING));
            mCreateText.setText(json);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private String parserJsonStr(String actionString) {

        String result="";
        try {
            InputStream fin = openFileInput("json.js");
            //获取文件长度
            int lenght = fin.available();
            byte[] buffer = new byte[lenght];
            fin.read(buffer);
            fin.close();
            //将byte数组转换成指定格式的字符串
            result = new String(buffer, ENCODING);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e(TAG, "parserJsonStr,  result = "+result);

        if (actionString.equals("person")) {
            try {
                return JsonTools.getPerson("person", result).toString();

            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                return null;
            }
        } else if (actionString.equals("persons")) {
            return JsonTools.getPersons(actionString, result).toString();
        } else if (actionString.equals("strings")) {
            /*List<String> list = JsonTools.getStrings(actionString, result);
            if(list.size() > 0){
                StringBuilder sb = new StringBuilder();
                for(String str : list) {
                    sb.append(str).append('\n');
                }
                return sb.toString();
            }
            return "is null";*/
            return JsonTools.getStrings(actionString, result).toString();
        } else if (actionString.equals("maps")) {
            return JsonTools.getMaps(actionString, result).toString();
        } else {
            return null;
        }
    }

    private String parserJsonStrDirect(String actionString) {
        Log.e(TAG, "parserJsonStrDirect,  mJsonStr = "+mJsonStr);
        if (actionString.equals("person")) {
            return JsonTools.getPerson("person", mJsonStr).toString();
        } else if (actionString.equals("persons")) {
            return JsonTools.getPersons(actionString, mJsonStr).toString();
        } else if (actionString.equals("strings")) {
            return JsonTools.getStrings(actionString, mJsonStr).toString();
        } else if (actionString.equals("maps")) {
            return JsonTools.getMaps(actionString, mJsonStr).toString();
        } else {
            return null;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.json_action_person :
                mActionStr = "person";
                break;
            case R.id.json_action_persons :
                mActionStr = "persons";
                break;
            case R.id.json_action_strings :
                mActionStr = "strings";
                break;
            case R.id.json_action_maps :
                mActionStr = "maps";
                break;
        }
        mJsonText.setText("json_text: " + mActionStr);
        SharePreferencesUtil.putSP(this, SharePreferencesUtil.JSON_TEST, mActionStr);
    }
}
