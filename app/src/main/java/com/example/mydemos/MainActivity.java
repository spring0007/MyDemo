package com.example.mydemos;


import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.SerialPortNames;
import android.util.SerialPortUtil;
import android.util.SerialPortUtil.ScoreCallBack;
import android.util.SerialPortUtil.PairCallBack;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements View.OnClickListener{
	private String TAG = "MainActivity";
	private String SOS_NUMBER_ACTION = "com.android.sos_number"; //SOS号码设置广播
	private SerialPortUtil mSerialPortUtil;
	private MediaPlayer mPlayer;
	private String mLogStr;
	private int mCheckASK;
	private TextView mLog;
	private EditText mPairCommandNum, mSetSlaveIdNum, mGetScoreNum, mClearScoreNum, mSosNumberNum;
	//private	EditText  et_set_phone_mode2;
	private EditText  et_set_phone_mode1;
	private EditText mSetMasterIdNum, mSetChannelNum, mMicAmpNum;

	private boolean hasSetChannel = false;
	private boolean hasSetMasterId = false;
	private boolean hasGetScore = false;
	int valueIndex = 0;
	MediaRecordingUtils recording;
    String mFilePath = Environment.getExternalStorageDirectory() +"/record/record_123.aac";
	ScoreCallBack mGetScoreCallBack = new ScoreCallBack() {
		
		@Override
		public void getScoreFinish(String scoresInfo) {
			Log.i("lqq","getScoreFinish,mScores="+scoresInfo);
		}
		
		@Override
		public void clearScoreFinish(String scoresInfo) {
			Log.i("lqq","clearScoreFinish,mScores="+scoresInfo);
		}
	};
	
	PairCallBack mPairCallBack = new PairCallBack() {
		
		@Override
		public void finished(String pairInfo) {
			Log.i("lqq","pair="+pairInfo);
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mLog = (TextView) findViewById(R.id.log);
		mPairCommandNum = (EditText) findViewById(R.id.pair_command_num);
		mSetSlaveIdNum = (EditText) findViewById(R.id.set_slave_id_num);
		mGetScoreNum = (EditText) findViewById(R.id.get_score_num);
		mClearScoreNum = (EditText) findViewById(R.id.clear_score_num);
		//setChannelRadom = (Button) findViewById(R.id.set_channel_radom);
		//setMasterIdRadom = (Button) findViewById(R.id.set_masterid_radom);
		mMicAmpNum = (EditText) findViewById(R.id.set_mic_amp_num);
		mSetMasterIdNum = (EditText) findViewById(R.id.set_device_id_num);
		mSetChannelNum = (EditText) findViewById(R.id.set_channel_num);
		mSosNumberNum = (EditText) findViewById(R.id.sos_number_num);
		et_set_phone_mode1 = (EditText) findViewById(R.id.et_set_phone_mode1);
	    //et_set_phone_mode2 = (EditText) findViewById(R.id.et_set_phone_mode2);

		findViewById(R.id.bt_set_phone_mode).setOnClickListener(this);
		findViewById(R.id.sos_number).setOnClickListener(this);
		findViewById(R.id.set_mic_amp).setOnClickListener(this);
		findViewById(R.id.set_channel).setOnClickListener(this);
		findViewById(R.id.set_device_id).setOnClickListener(this);
		findViewById(R.id.dis_pair).setOnClickListener(this);
		findViewById(R.id.ear_speaker).setOnClickListener(this);
		findViewById(R.id.open433_noear).setOnClickListener(this);
		findViewById(R.id.open433_ear).setOnClickListener(this);
		findViewById(R.id.close433).setOnClickListener(this);
		findViewById(R.id.get_score).setOnClickListener(this);
		findViewById(R.id.clear_score).setOnClickListener(this);
		findViewById(R.id.pair_command).setOnClickListener(this);
		findViewById(R.id.set_slave_id).setOnClickListener(this);
		findViewById(R.id.tx_power_5m).setOnClickListener(this);
		findViewById(R.id.tx_power_10m).setOnClickListener(this);
		findViewById(R.id.tx_power_30m).setOnClickListener(this);
		findViewById(R.id.volue_plus).setOnClickListener(this);
		findViewById(R.id.volume_minus).setOnClickListener(this);
		findViewById(R.id.volume_mute).setOnClickListener(this);
		findViewById(R.id.music_start).setOnClickListener(this);
		findViewById(R.id.music_stop).setOnClickListener(this);
		findViewById(R.id.mic_spk).setOnClickListener(this);
		findViewById(R.id.mic_no_spk).setOnClickListener(this);
		findViewById(R.id.volume_default).setOnClickListener(this);
		findViewById(R.id.record_play).setOnClickListener(this);
		findViewById(R.id.record_start).setOnClickListener(this);
		findViewById(R.id.record_stop).setOnClickListener(this);
        initSerialPortUtil(this);
		registerReceivers();
		valueIndex = Settings.System.getInt(getContentResolver(),"default_mic_volume",7);
        mSerialPortUtil.setMicValue(valueIndex);
		recording = new MediaRecordingUtils(this);
		recording.setOnAudioStatusUpdateListener(mAudioStatusUpdateListener);
	}
		
	@Override
	protected void onResume() {
		super.onResume();

		if(mSerialPortUtil.isIn433Mode){ //判断若是处于433模式，则关闭系统喇叭
			mSerialPortUtil.setPhoneMode(SerialPortNames.EXIT_SYS_SPEAK);
		}

	}
	MediaRecordingUtils.OnAudioStatusUpdateListener mAudioStatusUpdateListener = new MediaRecordingUtils.OnAudioStatusUpdateListener() {
		@Override
		public void onUpdate(double db, long time) {

		}

		@Override
		public void onStop(String filePath) {
			Log.e(TAG,"filePath="+filePath);
			//mFilePath = filePath;
		}
	};


	@Override
	protected void onPause() {
		super.onPause();
		mHandler.removeCallbacks(runnable);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// 拦截ACTION_DOWN事件，判断是否需要隐藏输入法
		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			View view = getCurrentFocus();
			if (isShouldHideInput(view, ev)) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				if (imm != null) {
					imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
				}
			}
			return super.dispatchTouchEvent(ev);
		}

		// 交由DecorView去做Touch事件的分发
		if (getWindow().superDispatchTouchEvent(ev)) {
			return true;
		}

		// Activity内没有View对这个Touch事件做处理，那么有Activity来处理
		return onTouchEvent(ev);
	}

	/**
	 * 判断是否点击控件是否为EditText。如果不是，那么就隐藏输入法。
	 * @param view
	 * @param ev
	 * @return
	 */
	private boolean isShouldHideInput(View view, MotionEvent ev) {
        //1、判断是否是EditText，如果不是，直接返回false
		if (view != null && (view instanceof EditText)) {
			int[] location = {0, 0};
			view.getLocationOnScreen(location);
			int left = location[0];
			int top = location[1];

            // 2、判断Touch的点是否在EditText外
			if (ev.getX() < left || (ev.getX() > left + view.getWidth())
					|| ev.getY() < top || (ev.getY() > top + view.getHeight())) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}
	boolean isSPK = false;
	/**
	 * Called when a view has been clicked.
	 *
	 * @param v The view that was clicked.
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.record_play:
				playRecordingFile(this);
				break;
			case R.id.record_start:
				recording.startRecord();
				break;
			case R.id.record_stop:
				recording.stopRecord();
				break;
			case R.id.volume_default:
				mSerialPortUtil.sendSerialPort("890755000000E5");
				break;
			case R.id.mic_spk: //mic外放 到游客机
				mSerialPortUtil.setMicPlayEnableMode(true);
                Settings.System.putInt(getContentResolver(),"default_mic_play_enable",1);

				isSPK = true;
				break;
			case R.id.mic_no_spk://关闭mic 外放到游客机
				mSerialPortUtil.setMicPlayEnableMode(false);
                Settings.System.putInt(getContentResolver(),"default_mic_play_enable",0);

				break;
			case R.id.music_start://播放音频
				playFromRawFile(this);
                Intent intent1 = new Intent();
                intent1.setAction("com.android.ACTION_MIC_PLAY_ENABLE");
               // intent.putExtra("enable",1);
                intent1.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
                sendBroadcast(intent1);

				break;
			case R.id.music_stop://暂停音频
				stopPlayFromRawFile();

				break;
			case R.id.volue_plus:
				mHandler.removeMessages(MSG_PLUS);
				mHandler.obtainMessage(MSG_PLUS).sendToTarget();
				break;
			case R.id.volume_minus:
				mHandler.removeMessages(MSG_MINUE);
				mHandler.obtainMessage(MSG_MINUE).sendToTarget();
				break;
			case R.id.volume_mute:
				mHandler.removeMessages(MSG_MUTE);
				mHandler.obtainMessage(MSG_MUTE).sendToTarget();
				break;
			case R.id.close433:
				mLog.setText("");
				mSerialPortUtil.setPhoneMode(SerialPortNames.ALL_CLOSE);
				break;
			case R.id.open433_ear :
				mLog.setText("");
				mSerialPortUtil.setPhoneMode(SerialPortNames.EAR_433MIC_REMOTE);
				break;
			case R.id.open433_noear:
				mLog.setText("");
				mSerialPortUtil.setPhoneMode(SerialPortNames.NO_EAR_433MIC_REMOTE);
				break;
			case R.id.ear_speaker:
				mLog.setText("");
				mSerialPortUtil.setPhoneMode(SerialPortNames.EAR_433MIC_SPEAK);
				break;
			case R.id.dis_pair:
				//if(hasSetMasterId) {
				disablePair();
				//	hasSetMasterId = false;
				//} else {
				//	Toast.makeText(MainActivity.this, "主机ID未设置", Toast.LENGTH_SHORT).show();
				//}
				break;
			case R.id.set_device_id:
				mLog.setText("");
				if (!mSetMasterIdNum.getText().toString().equals("")) {
					int masterid = Integer.parseInt(mSetMasterIdNum.getText().toString());
					if (masterid >= 1 && masterid <= 254) {
						setMasterId(masterid);
						return;
					}
				}
				Toast.makeText(MainActivity.this, "请输入范围为1-254的数字", Toast.LENGTH_SHORT).show();
				break;
			case R.id.set_channel:
				mLog.setText("");
				if (!mSetChannelNum.getText().toString().equals("")) {
					int channel = Integer.parseInt(mSetChannelNum.getText().toString());
					if (channel >= 1 && channel <= 36) {
						setChannelId(channel);
						return;
					}
				}
				Toast.makeText(MainActivity.this, "请输入范围为1-36的数字", Toast.LENGTH_SHORT).show();
				break;
			case R.id.set_mic_amp:
				mLog.setText("");
				if (!mMicAmpNum.getText().toString().equals("")) {
					int micamp = Integer.parseInt(mMicAmpNum.getText().toString());
					if (micamp >= 64 && micamp <= 91) {
						setMicAmp(micamp);
						mMicAmpNum.setText("");
						return;
					}
				}
				Toast.makeText(MainActivity.this, "请输入范围为64-91的数字", Toast.LENGTH_SHORT).show();
				break;
			case R.id.sos_number:
				if (!mSosNumberNum.getText().toString().equals("")) {
					setSosNumber(mSosNumberNum.getText().toString());
				}
				break;
			case R.id.bt_set_phone_mode:
				String text = et_set_phone_mode1.getText().toString();

				// 14 推送声音
				//15 关闭
				//13 监听
				if(!TextUtils.isEmpty(text)){
					int mode1 =  Integer.parseInt(text);
					mSerialPortUtil.setPhoneMode(SerialPortNames.NO_EAR_433MIC_REMOTE);
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					mSerialPortUtil.setPhoneMode(mode1);
					Log.v("lqq"," sucess setPhoneMode,mode1="+mode1);

				}
				break;
			case R.id.tx_power_30m:
				mLog.setText("");
				setDistance30();
				break;
			case R.id.tx_power_10m:
				mLog.setText("");
				setDistance10();
				break;
			case R.id.tx_power_5m:
				mLog.setText("");
				setDistance5();
				break;
			case R.id.set_slave_id:
				mLog.setText("");
				if (!mSetSlaveIdNum.getText().toString().equals("")) {
					int slaveid = Integer.parseInt(mSetSlaveIdNum.getText().toString());
					if (slaveid >= 1 && slaveid <= 9999) {
						setSlaveId(slaveid);
						return;
					}
				}
				Toast.makeText(MainActivity.this, "请输入范围为1-9999的数字", Toast.LENGTH_SHORT).show();
				break;
			case R.id.pair_command:
				mLog.setText("");
				//if(!hasSetChannel) {
				//	Toast.makeText(MainActivity.this, "请先设置频道", Toast.LENGTH_SHORT).show();
				//	return;
				//}
				//if(!hasSetMasterId) {
				//	Toast.makeText(MainActivity.this, "请先设置主机ID", Toast.LENGTH_SHORT).show();
				//	return;
				//}
				if (!mPairCommandNum.getText().toString().equals("")) {
					int slaveid = Integer.parseInt(mPairCommandNum.getText().toString());
					if (slaveid >= 1 && slaveid <= 9999) {
						pairCommand(slaveid);
						/*List<String> mSlaveids = new ArrayList<String>();
						mSlaveids.add("57");
						mSlaveids.add("19");
						mSlaveids.add("120");
						mSlaveids.add("05");
						mSlaveids.add(""+slaveid);
						pair(mSlaveids, mPairCallBack);*/
						return;
					}
				}
				Toast.makeText(MainActivity.this, "请输入范围为1-9999的数字", Toast.LENGTH_SHORT).show();
				break;
			case R.id.clear_score:
				mLog.setText("");

				if (!mClearScoreNum.getText().toString().equals("")) {
					int slaveid = Integer.parseInt(mClearScoreNum.getText().toString());
					if (slaveid >= 1 && slaveid <= 9999) {
						/*List<String> mSlaveids = new ArrayList<String>();
						mSlaveids.add("57");
						mSlaveids.add("19");
						mSlaveids.add("120");
						mSlaveids.add("05");
						mSlaveids.add(""+slaveid);
						clearScore(mSlaveids, mGetScoreCallBack);*/
						//if(hasGetScore) {
						clearScore(slaveid);
						//	hasGetScore = false;
						//} else {
						//	Toast.makeText(MainActivity.this, "请先获取评分", Toast.LENGTH_SHORT).show();
						//}
						return;
					}
				}
				Toast.makeText(MainActivity.this, "请输入范围为1-9999的数字", Toast.LENGTH_SHORT).show();
				break;
			case R.id.get_score:
				mLog.setText("");
				if (!mGetScoreNum.getText().toString().equals("")) {
					int slaveid = Integer.parseInt(mGetScoreNum.getText().toString());
					if (slaveid >= 1 && slaveid <= 9999) {
						//if(!hasGetScore) {
						getScore(slaveid);
						/*List<String> mSlaveids = new ArrayList<String>();
						mSlaveids.add(""+slaveid);
						mSlaveids.add("57");
						mSlaveids.add("19");
						mSlaveids.add("120");
						mSlaveids.add("05");
						getScore(mSlaveids, mGetScoreCallBack);*/
						//hasGetScore = true;
						//} else {
						//	Toast.makeText(MainActivity.this, "请先清除评分", Toast.LENGTH_SHORT).show();
						//}
						return;
					}
				}
				Toast.makeText(MainActivity.this, "请输入范围为1-9999的数字", Toast.LENGTH_SHORT).show();
				break;

		}

	}

	private void initSerialPortUtil(Context context) {
		mSerialPortUtil = SerialPortUtil.getInstance(context);
		// 串口数据监听事件
		mSerialPortUtil.setOnDataReceiveListener(new SerialPortUtil.OnDataReceiveListener() {
			@Override
			public void onDataReceive(String string) {
				Log.d("lqq", "进入数据监听事件中。。。" + string);

				mLogStr = string;
				
				mHandler.post(runnable);
			}
		});
	}

	private void setSosNumber(String num){  //设置SOS电话号码
		Intent intent = new Intent(SOS_NUMBER_ACTION);
		intent.putExtra("number", num);
		sendBroadcast(intent);
		Toast.makeText(MainActivity.this, "设置SOS号码成功", Toast.LENGTH_SHORT).show(); 
	}
	
	private void disablePair() {
		mSerialPortUtil.sendSerialPort(SerialPortNames.DISABLE_PAIR);
	}
	
	/**
     * 设置主机ID
     */
	private void setMasterId(int id) {
		mSerialPortUtil.sendSerialPort(mSerialPortUtil.getSetMasterIdCommand(id));
	}
	
	/**
     * 设置频道
     */
	private void setChannelId(int id) {
		mSerialPortUtil.sendSerialPort(mSerialPortUtil.getSetChannelCommand(id));
	}
	
	private void setMicAmp(int micamp) {
		mSerialPortUtil.sendSerialPort(mSerialPortUtil.getSetMicAmpCommand(micamp));
	}
	
	/**
     * 设置游客机ID
     */
	private void setSlaveId(int id) {
		mSerialPortUtil.sendSerialPort(mSerialPortUtil.getSetSlaveIdCommand(id));
	}
	
	/**
     * 与对应ID游客机配对
     */
	private void pairCommand(int id) {
		mSerialPortUtil.sendSerialPort(mSerialPortUtil.getPairCommand(id));
	}
	private void pair(List<String> ids, PairCallBack pairCallBack) {
		mSerialPortUtil.pair(ids, pairCallBack);
	}
	
	/**
     * 获取对应id的游客机的评分
     */
	private void getScore(int id) {
		mSerialPortUtil.sendSerialPort(mSerialPortUtil.getScoreCommand(id));
	}
	private void getScore(List<String> ids, ScoreCallBack getScoreCallBack) {
		mSerialPortUtil.getScore(ids, getScoreCallBack);
	}

	/**
     * 停止获取对应id的游客机的评分
     */
	private void stopGetScore(int id) {
		mSerialPortUtil.sendSerialPort(mSerialPortUtil.stopGetScoreCommand(id));
	}
	
	/**
     * 清除对应id的游客机的评分
     */
	private void clearScore(int id) {
		mSerialPortUtil.sendSerialPort(mSerialPortUtil.clearScoreCommand(id));
	}
	private void clearScore(List<String> ids, ScoreCallBack clearScoreCallBack) {
		mSerialPortUtil.clearScore(ids, clearScoreCallBack);
	}
	
	/**
     * 设置通信距离5米
     */
	private void setDistance5() {
		mSerialPortUtil.sendSerialPort(SerialPortNames.TX_POWER1);
	}
	
	/**
     * 设置通信距离10米
     */
	private void setDistance10() {
		mSerialPortUtil.sendSerialPort(SerialPortNames.TX_POWER2);
	}
	
	/**
     * 设置通信距离30米
     */
	private void setDistance30() {
		mSerialPortUtil.sendSerialPort(SerialPortNames.TX_POWER4);
	}
	
	
	/**
     * 麦克风增益等级1
     */
	private void setMicAmp1() {
		mSerialPortUtil.sendSerialPort(SerialPortNames.MicAmp1);
	}
	
	/**
     * 麦克风增益等级2
     */
	private void setMicAmp2() {
		mSerialPortUtil.sendSerialPort(SerialPortNames.MicAmp2);
	}
	
	/**
     * 麦克风增益等级3
     */
	private void setMicAmp3() {
		mSerialPortUtil.sendSerialPort(SerialPortNames.MicAmp3);
	}
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MUTE) {
			
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onDestroy() {
		mSerialPortUtil.closeSerialPort();
		mSerialPortUtil.setPhoneMode(SerialPortNames.ALL_CLOSE);
		super.onDestroy();
		unRegisterReveivers();  //注销监听
	}

	public static final int MSG_PLUS=1;
	public static final  int MSG_MINUE=2;
	public static final  int MSG_MUTE=3;

	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			int what = msg.what;
			switch (what){
				case MSG_PLUS:
					valueIndex ++;

					break;
				case MSG_MINUE:
					valueIndex --;

					break;
				case MSG_MUTE:
					valueIndex = 0;
					break;
			}
			if(valueIndex<=0){
				valueIndex = 0;

			}else if(valueIndex>14){
					valueIndex = 14;
			}

			Settings.System.putInt(getContentResolver(),"default_mic_volume",valueIndex);
			mSerialPortUtil.setMicValue(valueIndex);
		}
	};
	
	// 开线程更新UI
	private Runnable runnable = new Runnable() {
		@Override
		public void run() {
			mCheckASK = mSerialPortUtil.checkASKCommand(mLogStr);
			mLog.setText(mLog.getText() + mLogStr);
			Log.i("lqq","mLog.getText()="+mLog.getText()+",mLogStr="+mLogStr);
			/*if(mCheckASK == SerialPortNames.ASK_PAIR) { //配对成功即会调用此方法
				String slaveid = mLogStr.substring(8, 12);
				Toast.makeText(MainActivity.this, "编号" + slaveid + "配对成功", Toast.LENGTH_SHORT).show();
			} else if(mCheckASK == SerialPortNames.ASK_SCORE) { //获取评分成功即会调用此方法
				String score = mLogStr.substring(6, 8);
				String slaveid = mLogStr.substring(8, 12);
				Toast.makeText(MainActivity.this, "编号" + slaveid + "评分" + score, Toast.LENGTH_SHORT).show();
			} else if(mCheckASK == SerialPortNames.ASK_SET_SALVE_ID) { //游客机写码成功即会调用此方法
				String slaveid = mLogStr.substring(8, 12);
				Toast.makeText(MainActivity.this, "编号" + slaveid + "写码成功", Toast.LENGTH_SHORT).show();
			}*/
		}
	};

	private void registerReceivers() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.intent.action.HEADSET_PLUG");
		filter.addAction("com.android.ACTION_STOP_MICPLAY");
		registerReceiver(HeadsetPlugReceiver, filter);

		mbr = new MusicBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("com.android.music.metachanged");
		intentFilter.addAction("com.android.music.queuechanged");
		intentFilter.addAction("com.android.music.playbackcomplete");
		intentFilter.addAction("com.android.music.playstatechanged");
		registerReceiver(mbr, intentFilter);

	}

	private void unRegisterReveivers(){
		unregisterReceiver(mbr);
		unregisterReceiver(HeadsetPlugReceiver);
	}

	/**
	 * 监听耳机插拔
	 */
	BroadcastReceiver HeadsetPlugReceiver = new  BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
            String action  = intent.getAction();
            if(action.equals("android.intent.action.HEADSET_PLUG")){
				if (intent.hasExtra("state")) {
					if (intent.getIntExtra("state", 0) == 0) {
						AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
						//boolean ava =audioManager.isMicrophoneMute();
						//boolean ava1 = audioManager.isSpeakerphoneOn();
						Toast.makeText(context, "耳机拔出"/*,isSPK="+isSPK+",扬声器="+ava+",免提="+ava1*/, Toast.LENGTH_LONG).show();
						stopPlayFromRawFile(); //暂停播放
					/*if(isSPK){
						mSerialPortUtil.setMicPlayEnableMode(true);
					}*/

						//mHandler.postDelayed(mAicRunnable,500);
					} else if (intent.getIntExtra("state", 0) == 1) {
						boolean ava =validateMicAvailability();
						Toast.makeText(context, "耳机插入"/*,isSPK="+isSPK+",ava="+ava*/, Toast.LENGTH_LONG).show();
					/*if(isSPK){
						mSerialPortUtil.setMicPlayEnableMode(true);
					}*/

					}
				}

			}else if (action.equals("com.android.ACTION_STOP_MICPLAY")){
				stopPlayFromRawFile();
            }
		}

	};
	/***
	 * //通话时设置静音
	 * System.out.println("isMicrophoneMute =" + audioManager.isMicrophoneMute());
	 *     audioManager.setMicrophoneMute(!audioManager.isMicrophoneMute());
	 *
	 * //通话时设置免提
	 * System.out.println("isSpeakerphoneOn =" + audioManager.isSpeakerphoneOn());
	 *     audioManager.setSpeakerphoneOn(!audioManager.isSpeakerphoneOn());
	 * //别忘了修改的权限 <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS"/>
	 */
    Runnable mAicRunnable = new Runnable() {
		@Override
		public void run() {
			AudioManager audioManager = (AudioManager) MainActivity.this.getSystemService(Context.AUDIO_SERVICE);
			boolean ava =audioManager.isMicrophoneMute();
			boolean ava1 = audioManager.isSpeakerphoneOn();
			//Toast.makeText(MainActivity.this, "扬声器="+ava+",免提="+ava1, Toast.LENGTH_LONG).show();
			if(isSPK){
				mSerialPortUtil.setMicPlayEnableMode(true);
			}
		}
	};


	/**
	 * 播放来电和呼出铃声
	 *
	 * @param mContext
	 */
	private void playFromRawFile(Context mContext) {
		//1.获取模式
		AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
		final int ringerMode = am.getRingerMode();
		//2.普通模式可以呼叫普通模式： AudioManager.RINGER_MODE_NORMAL 静音模式：AudioManager.RINGER_MODE_VIBRATE 震动模式：AudioManager.RINGER_MODE_SILENT
		if (ringerMode == AudioManager.RINGER_MODE_NORMAL) {
			try {
				if(mPlayer==null) {
					mPlayer = new MediaPlayer();
				}
				AssetManager assetManager =getResources().getAssets();
				AssetFileDescriptor file = assetManager.openFd("Five_Hundred_Miles.mp3");
				try {

					mPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
					file.close();
					if (!mPlayer.isPlaying()) {
						mPlayer.prepare();
						mPlayer.start();
						mPlayer.setLooping(true);//循环播放
					}
				} catch (IOException e) {
					mPlayer = null;
				}
			} catch (Exception e) {
				e.printStackTrace();

			}
		}

	}

	private void playRecordingFile(Context mContext) {
	    Log.i(TAG,"playRecordingFile");
		if(TextUtils.isEmpty(mFilePath)){
			Toast.makeText(this,"暂未录音",Toast.LENGTH_SHORT).show();
			return ;
		}
		File mFile = new File(mFilePath);
		if(!mFile.exists()){
            Toast.makeText(this,"录音文件不存在",Toast.LENGTH_SHORT).show();
            return;
        }

		//1.获取模式
		AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
		final int ringerMode = am.getRingerMode();
		//2.普通模式可以呼叫普通模式： AudioManager.RINGER_MODE_NORMAL 静音模式：AudioManager.RINGER_MODE_VIBRATE 震动模式：AudioManager.RINGER_MODE_SILENT
		if (ringerMode == AudioManager.RINGER_MODE_NORMAL) {
			try {
				if(mPlayer==null) {
					mPlayer = new MediaPlayer();
				}
				try {
					mPlayer.setDataSource(mFilePath);
                    if (mPlayer != null && mPlayer.isPlaying()) {
                        mPlayer.stop();
                        mPlayer.release();
                    }
					if (!mPlayer.isPlaying()) {
						mPlayer.prepare();
						mPlayer.start();
						mPlayer.setLooping(false);//循环播放
					}
				} catch (IOException e) {
				    Log.e(TAG,"e1="+e.getLocalizedMessage());
					mPlayer = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
                Log.e(TAG,"e2="+e.getLocalizedMessage());

			}finally {
                Log.i(TAG,"playRecordingFile,finally");
                /*if (mPlayer != null && mPlayer.isPlaying()) {
                    mPlayer.stop();
                    mPlayer.release();
                }*/
                mPlayer = null;
            }
		}

	}


	/**
	 * 结束播放来电和呼出铃声
	 */
	private void stopPlayFromRawFile() {
		if (mPlayer != null && mPlayer.isPlaying()) {
			mPlayer.stop();
			mPlayer.release();
		}
		mPlayer = null;
	}

	MusicBroadcastReceiver mbr = null;//
	class MusicBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			boolean playing = intent.getBooleanExtra("playing",false);
			Toast.makeText(context, "playing:" + playing, Toast.LENGTH_SHORT).show();

			if(playing){

			} else{

			}

		}
	}

	/**
	 * Android判断麦克风是否被占用
	 * 返回true就是没有被占用。
	 * 返回false就是被占用。
	 * @return
	 */
	private boolean validateMicAvailability(){
		Boolean available = true;
		AudioRecord recorder =
				new AudioRecord(MediaRecorder.AudioSource.MIC, 44100,
						AudioFormat.CHANNEL_IN_MONO,
						AudioFormat.ENCODING_DEFAULT, 44100);
		try{
			if(recorder.getRecordingState() != AudioRecord.RECORDSTATE_STOPPED ){
				available = false;

			}

			recorder.startRecording();
			if(recorder.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING){
				recorder.stop();
				available = false;

			}
			recorder.stop();
		} finally{
			recorder.release();
			recorder = null;
		}

		return available;
	}

}
