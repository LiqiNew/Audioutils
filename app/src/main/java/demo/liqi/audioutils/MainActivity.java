package demo.liqi.audioutils;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;

/**
 * 主页面
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button button;
    private TextView text;
    /**
     * 声音是录制还是播放标识
     */
    private int objTag = VoiceActivity.RECORD_TAG;
    private File mFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);
        text = (TextView) findViewById(R.id.text);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                Intent intent = new Intent(this, VoiceActivity.class);
                intent.putExtra("layoutTag", objTag+"");
                intent.putExtra("obj", mFile);
                startActivityForResult(intent, objTag);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == VoiceActivity.PLAY_RECORD_TAG) {
            if (null != data) {
                mFile = (File) data.getSerializableExtra("file");
                String time = data.getStringExtra("time");
                text.setText("录音时长：" + time + "\n如需重新录制,请点击'声音'界面中的'重录'");
                button.setText("进入播放界面");
                objTag = resultCode;
            }
        }
        if (resultCode == VoiceActivity.RECORD_TAG) {
            text.setText("录音完毕保存之后，才能播放录音");
            button.setText("进入录音界面");
            mFile = null;
            objTag = resultCode;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
