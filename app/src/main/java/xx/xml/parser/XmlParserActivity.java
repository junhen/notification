package xx.xml.parser;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.leui.notification.test.R;

public class XmlParserActivity extends Activity {
    private static final String TAG = "XmlParserActivity";

    private static final String TYPE_SAX = "sax";
    private static final String TYPE_DOM = "dom";
    private static final String TYPE_PULL = "pull";

    private BookParser parser;
    private List<Book> books;
    private String mType = TYPE_SAX;
    
    private TextView mTypeText;
    private TextView mParserReadText;
    private TextView mParserWriteText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parser);

        Button readBtn = (Button) findViewById(R.id.readBtn);
        Button writeBtn = (Button) findViewById(R.id.writeBtn);

        readBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    InputStream is = getAssets().open("books.xml");
                    switch (mType) {
                        case TYPE_SAX :
                            parser = new SaxBookParser();
                            break;
                        case TYPE_DOM :
                            parser = new DomBookParser();
                            break;
                        case TYPE_PULL :
                            parser = new PullBookParser();
                            break;
                    } //创建SaxBookParser实例
                    books = parser.parse(is);  //解析输入流
                    StringBuilder sb = new StringBuilder();
                    for (Book book : books) {
                        Log.i(TAG, book.toString());
                        sb.append(book.toString()).append("\n");
                    }
                    mParserReadText.setText(sb.toString());
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        });
        writeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String xml = parser.serialize(books);  //序列化
                    FileOutputStream fos = openFileOutput("books.xml", Context.MODE_PRIVATE);
                    fos.write(xml.getBytes("UTF-8"));
                    mParserWriteText.setText(xml);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        });

        Button saxBn = (Button) findViewById(R.id.parser_sax);
        saxBn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mType = TYPE_SAX;
                setTypeText();
            }
        });
        Button domBn = (Button) findViewById(R.id.parser_dom);
        domBn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mType = TYPE_DOM;
                setTypeText();
            }
        });
        Button pullBn = (Button) findViewById(R.id.parser_pull);
        pullBn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mType = TYPE_PULL;
                setTypeText();
            }
        });
        Button cleanBn = (Button) findViewById(R.id.cleanBn);
        cleanBn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mParserReadText.setText("");
                mParserWriteText.setText("");
            }
        });
        mTypeText = (TextView)findViewById(R.id.parser_type_text);
        setTypeText();
        mParserReadText = (TextView)findViewById(R.id.parser_read_text);
        mParserWriteText = (TextView)findViewById(R.id.parser_write_text);
    }
    
    private void setTypeText() {
        mTypeText.setText(new StringBuilder().append("type: ").append(mType).toString());
    }

}
