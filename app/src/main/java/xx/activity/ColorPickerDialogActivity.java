package xx.activity;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.leui.notification.test.R;

import xx.view.ColorPickerView;
import xx.view.ColorPickerViewTwo;

public class ColorPickerDialogActivity extends Activity {

    private ColorPickerView myView;
    private ColorPickerViewTwo myViewTwo;
    private Button mColorSure;
    private Button mChangeColorPanel;

    private int mColor = 0x88880000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_picker_dialog);

        /*myView = (ColorPickerView) findViewById(R.id.color_picker_view);
        myView.setOnColorChangedListenner(new ColorPickerView.OnColorChangedListener() {
            @Override
            public void onColorChanged(int color, int originalColor, float saturation) {
                Log.d("XINX","onColorChanged   color : "+Integer.toHexString(color)+",  originalColor: "+Integer.toHexString(originalColor)+",  saturation: "+saturation);
                setResult(RESULT_OK, new Intent().putExtra("color", color));
            }
        });*/

        myViewTwo = (ColorPickerViewTwo) findViewById(R.id.color_picker_view_two);
        myViewTwo.setOnColorChangedListener(new ColorPickerViewTwo.OnColorChangedListener() {
            @Override
            public void onColorChanged(int color) {
                Log.d("XINX","onColorChanged   color : "+Integer.toHexString(color));
                mColor = color;
            }
        });
        mColorSure = (Button) findViewById(R.id.color_sure);
        mColorSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK, new Intent().putExtra("color", mColor));
                finish();
            }
        });
        mChangeColorPanel = (Button) findViewById(R.id.change_color_panel);
        mChangeColorPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeColorPanel();
            }
        });
    }

    private void changeColorPanel() {
        myViewTwo.changeColorPanel();
    }

}
