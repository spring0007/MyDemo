package com.example.mydemos;


import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.hardware.input.InputManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.android.rd_jar.RDDevice;
import com.android.rdaar.RdDeviceFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DemoMainActivity extends Activity implements  MediaRecordingUtils.OnAudioStatusUpdateListener, View.OnClickListener {

    MediaRecordingUtils mediaRecordingUtils;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mediaRecordingUtils = new MediaRecordingUtils(this);
        mediaRecordingUtils.setOnAudioStatusUpdateListener(this);
        findViewById(R.id.start_media).setOnClickListener(this);
        findViewById(R.id.stop_media).setOnClickListener(this);
        findViewById(R.id.sos_key).setOnClickListener(this);
        findViewById(R.id.camera_key).setOnClickListener(this);
        findViewById(R.id.home_key).setOnClickListener(this);
        findViewById(R.id.back_key).setOnClickListener(this);
        findViewById(R.id.menu_key).setOnClickListener(this);
        findViewById(R.id.pair_key).setOnClickListener(this);
        findViewById(R.id.volume_minus_key).setOnClickListener(this);
        findViewById(R.id.volume_plus_key).setOnClickListener(this);
        findViewById(R.id.shutdown).setOnClickListener(this);
        findViewById(R.id.reboot).setOnClickListener(this);
        findViewById(R.id.mic_close).setOnClickListener(this);
        findViewById(R.id.mic_open).setOnClickListener(this);
        Context mContext = getApplicationContext();
        WindowManager wm = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);


    }


    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    public void onUpdate(double db, long time) {
        Log.i("lqq","db="+db+",time");
    }

    @Override
    public void onStop(String filePath) {
        Log.i("lqq","filePath="+filePath);
    }

    int index = 75;
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.start_media:
                Root root = new Root();
                Boolean bool = root.checkRootMethod1();
                Boolean bool2 = root.checkRootMethod2();
                Boolean bool3 = root.checkRootMethod2();
                Toast.makeText(this,"bool="+bool+"bool2="+bool2+"bool3="+bool3,Toast.LENGTH_LONG).show();
               // mediaRecordingUtils.startRecord();
                break;
            case R.id.stop_media:
                mediaRecordingUtils.stopRecord();
                break;

            case R.id.sos_key:
                RdDeviceFactory.getInstance().sentKeyEvent(this,RdDeviceFactory.KEYCODE_SOS);
                break;
            case R.id.camera_key:
                RdDeviceFactory.getInstance().sentKeyEvent(this,RdDeviceFactory.KEYCODE_CAMERA);
                break;
            case R.id.home_key:
                RdDeviceFactory.getInstance().sentKeyEvent(this,RdDeviceFactory.KEYCODE_HOME);
                break;
            case R.id.menu_key:
                RdDeviceFactory.getInstance().sentKeyEvent(this,RdDeviceFactory.KEYCODE_MENU);
                break;
            case R.id.pair_key:
                RdDeviceFactory.getInstance().sentKeyEvent(this,RdDeviceFactory.KEYCODE_PARI);
                break;
            case R.id.volume_minus_key:
                index--;
                RdDeviceFactory.getInstance().setMicValue(this,index);
                break;
            case R.id.volume_plus_key:
                index++;
                RdDeviceFactory.getInstance().setMicValue(this,index);
                break;
            case R.id.back_key:
                RdDeviceFactory.getInstance().sentKeyEvent(this,RdDeviceFactory.KEYCODE_BACK);
                break;
            case R.id.shutdown:
                RdDeviceFactory.getInstance().enableShutDown(this);
                break;
            case R.id.reboot:
                RdDeviceFactory.getInstance().enableReboot(this);
                break;
            case R.id.mic_open:
                index--;
                RdDeviceFactory.getInstance().setMicPlayEnableMode(this,true);
                break;
            case R.id.mic_close:
                index++;
                RdDeviceFactory.getInstance().setMicPlayEnableMode(this,false);
                break;
        }
        Looper.loop();
    }



    public void pingwifi() {

        String result = null;
        try {
            Process p = Runtime.getRuntime().exec("input keyevent 24" );
            // 读取ping的内容，可以不加
            InputStream input = p.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            StringBuffer stringBuffer = new StringBuffer();
            String content = "";
            while ((content = in.readLine()) != null) {
                stringBuffer.append(content);
            }

        } catch (Exception e) {
            result = "IOException";
        } finally {
            Log.d("lqq", "wifi,ping result = " + result);
        }

    }


    /**
     * 执行shell 命令， 命令中不必再带 adb shell
     *
     * @param cmd
     * @return Sting  命令执行在控制台输出的结果
     */
    public static String execByRuntime(String cmd) {
        Process process = null;
        BufferedReader bufferedReader = null;
        InputStreamReader inputStreamReader = null;
        try {
            process = Runtime.getRuntime().exec(cmd);
            inputStreamReader = new InputStreamReader(process.getInputStream());
            bufferedReader = new BufferedReader(inputStreamReader);

            int read;
            char[] buffer = new char[4096];
            StringBuilder output = new StringBuilder();
            while ((read = bufferedReader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
            return output.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (null != inputStreamReader) {
                try {
                    inputStreamReader.close();
                } catch (Throwable t) {
                    //
                }
            }
            if (null != bufferedReader) {
                try {
                    bufferedReader.close();
                } catch (Throwable t) {
                    //
                }
            }
            if (null != process) {
                try {
                    process.destroy();
                } catch (Throwable t) {
                    //
                }
            }
        }
    }


    private Runnable mKeyRemappingVolumeDownLongPress = new Runnable() {
        public void run() {
            // Emulate clicking Menu key
            //keyRemappingSendFakeKeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MENU);
            //keyRemappingSendFakeKeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MENU);

        }
    };


   /* private long mKeyRemappingSendFakeKeyDownTime;
    private void keyRemappingSendFakeKeyEvent(int action, int keyCode) {
        long eventTime = SystemClock.uptimeMillis();
        if (action == KeyEvent.ACTION_DOWN) {
            mKeyRemappingSendFakeKeyDownTime = eventTime;
        }

        KeyEvent keyEvent = new KeyEvent(mKeyRemappingSendFakeKeyDownTime, eventTime, action, keyCode, 0);
        InputManager inputManager = (InputManager) this.getSystemService(Context.INPUT_SERVICE);
        inputManager.injectInputEvent(keyEvent, InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
    }*/

    /**
     * Sends an up and down key event sync to the currently focused window.
     *
     * @param key The integer keycode for the event.
     */
    private void sendKeyDownUpSync(int key) {

        Log.i("lqq", "sendKeyDownUpSync,key=" + key);
    }
}
