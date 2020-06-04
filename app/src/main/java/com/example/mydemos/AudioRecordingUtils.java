package com.example.mydemos;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 录音
 * 用法：1-init,filePath文件的后缀为.pcm 2-start  3-stop
 * stop之后，所有的音频数据会以pcm的格式写入到filePath这个文件内，并且是末尾添加的方式，而非覆盖（以达到暂停录音继续录音的效果），需要转换为其他格式才能让系统播放器直接播放
 */
public class AudioRecordingUtils {/*

    String TAG ="AudioRecordingUtils";

    //指定音频源 这个和MediaRecorder是相同的 MediaRecorder.AudioSource.MIC指的是麦克风
    private static final int mAudioSource = MediaRecorder.AudioSource.MIC;
    //指定采样率 （MediaRecoder 的采样率通常是8000Hz AAC的通常是44100Hz。 设置采样率为44100，目前为常用的采样率，官方文档表示这个值可以兼容所有的设置）
    private static final int mSampleRateInHz = 44100;
    //指定捕获音频的声道数目。在AudioFormat类中指定用于此的常量
    private static final int mChannelConfig = AudioFormat.CHANNEL_IN_STEREO; //立体声
    //指定音频量化位数 ,在AudioFormaat类中指定了以下各种可能的常量。通常我们选择ENCODING_PCM_16BIT和ENCODING_PCM_8BIT PCM代表的是脉冲编码调制，它实际上是原始音频样本。
    //因此可以设置每个样本的分辨率为16位或者8位，16位将占用更多的空间和处理能力,表示的音频也更加接近真实。
    private static final int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;
    //指定缓冲区大小。调用AudioRecord类的getMinBufferSize方法可以获得。


    private AudioRecord audioRecord = null;  // 声明 AudioRecord 对象
    private int recordBufSize = 0; // 声明recoordBufffer的大小字段

    private boolean isRecording = false;

    private String saveFilePath;
    //    private FileOutputStream os = null;
    private File mRecordingFile;

    private OnAudioRecordingListener onAudioRecordingListener;

    public void init(String filePath, OnAudioRecordingListener onAudioRecordingListener) {
        this.onAudioRecordingListener = onAudioRecordingListener;
        saveFilePath = filePath;
        recordBufSize = AudioRecord.getMinBufferSize(mSampleRateInHz, mChannelConfig, mAudioFormat);//计算最小缓冲区
        audioRecord = new AudioRecord(mAudioSource, mSampleRateInHz, mChannelConfig,
                mAudioFormat, recordBufSize);//创建AudioRecorder对象

        //创建一个流，存放从AudioRecord读取的数据
        mRecordingFile = new File(saveFilePath);
        if (mRecordingFile.exists()) {//音频文件保存过了删除
            mRecordingFile.delete();
        }
        try {
            mRecordingFile.createNewFile();//创建新文件
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("lu", "创建储存音频文件出错");
        }

    }
    public static double bytes2Double(byte[] arr) {
        long value = 0;
        for (int i = 0; i < 8; i++) {
            value |= ((long) (arr[i] & 0xff)) << (8 * i);
        }
        return Double.longBitsToDouble(value);
    }
    public void startRecording() {
        //判断AudioRecord的状态是否初始化完毕
        //在AudioRecord对象构造完毕之后，就处于AudioRecord.STATE_INITIALIZED状态了。
        if (audioRecord == null || audioRecord.getState() == AudioRecord.STATE_UNINITIALIZED) {
            Log.e(TAG,"尚未初始化完成");
            return;
        }

        XyObservable.addTask(new XyCallBack() {//开一个子线程的意思
            private double volume = 0;

            @Override
            public void run() {
                //标记为开始采集状态
                isRecording = true;
                try {
                    //获取到文件的数据流
                    DataOutputStream mDataOutputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(mRecordingFile, true)));
                    byte[] buffer = new byte[recordBufSize];
                    audioRecord.startRecording();//开始录音
                    //getRecordingState获取当前AudioReroding是否正在采集数据的状态
                    while (isRecording && audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                        int bufferReadResult = audioRecord.read(buffer, 0, recordBufSize);
                        for (int i = 0; i < bufferReadResult; i++) {
                            mDataOutputStream.write(buffer[i]);
                        }
                        setFinish();//这里会调到下面的finish()方法，finish()方法处于UI线程中
                    }
                    mDataOutputStream.close();
                } catch (Throwable t) {
                    Log.e("lu", "Recording Failed");
                    stopRecording();
                }
            }

            @Override
            public void finish() {
                if (onAudioRecordingListener != null) {
                    onAudioRecordingListener.onChange(volume);
                }
            }
        });

    }

    *//**
     * 暂停录音
     *//*
    public void pauseRecording() {
        isRecording = false;
        if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
            audioRecord.stop();
        }
    }

    //停止录音
    public void stopRecording() {
        isRecording = false;
        //停止录音，回收AudioRecord对象，释放内存
        if (audioRecord != null) {
            if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                audioRecord.stop();
            }
            if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                audioRecord.release();
            }
        }
    }

    public interface OnAudioRecordingListener {
        public void onChange(double volume);
    }
}*/

//然后再附带一个将原始pcm转换为wav格式的方法：


class Pcm2WavUtils {

    /**
     * PCM文件转WAV文件
     *
     * @param inPcmFilePath  输入PCM文件路径
     * @param outWavFilePath 输出WAV文件路径
     * @param sampleRate     采样率，例如44100
     * @param channels       声道数 单声道：1或双声道：2
     * @param bitNum         采样位数，8或16
     */
    public void convertPcm2Wav(String inPcmFilePath, String outWavFilePath, int sampleRate,
                               int channels, int bitNum) {

        FileInputStream in = null;
        FileOutputStream out = null;
        byte[] data = new byte[1024];

        try {
            //采样字节byte率
            long byteRate = sampleRate * channels * bitNum / 8;

            in = new FileInputStream(inPcmFilePath);
            out = new FileOutputStream(outWavFilePath);

            //PCM文件大小
            long totalAudioLen = in.getChannel().size();

            //总大小，由于不包括RIFF和WAV，所以是44 - 8 = 36，在加上PCM文件大小
            long totalDataLen = totalAudioLen + 36;

            writeWaveFileHeader(out, totalAudioLen, totalDataLen, sampleRate, channels, byteRate);

            int length = 0;
            while ((length = in.read(data)) > 0) {
                out.write(data, 0, length);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 输出WAV文件
     *
     * @param out           WAV输出文件流
     * @param totalAudioLen 整个音频PCM数据大小
     * @param totalDataLen  整个数据大小
     * @param sampleRate    采样率
     * @param channels      声道数
     * @param byteRate      采样字节byte率
     * @throws IOException
     */
    private void writeWaveFileHeader(FileOutputStream out, long totalAudioLen,
                                     long totalDataLen, int sampleRate, int channels, long byteRate) throws IOException {
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);//数据大小
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';//WAVE
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        //FMT Chunk
        header[12] = 'f'; // 'fmt '
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';//过渡字节
        //数据大小
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        //编码方式 10H为PCM编码格式
        header[20] = 1; // format = 1
        header[21] = 0;
        //通道数
        header[22] = (byte) channels;
        header[23] = 0;
        //采样率，每个通道的播放速度
        header[24] = (byte) (sampleRate & 0xff);
        header[25] = (byte) ((sampleRate >> 8) & 0xff);
        header[26] = (byte) ((sampleRate >> 16) & 0xff);
        header[27] = (byte) ((sampleRate >> 24) & 0xff);
        //音频数据传送速率,采样率*通道数*采样深度/8
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        // 确定系统一次要处理多少个这样字节的数据，确定缓冲区，通道数*采样位数
        header[32] = (byte) (channels * 16 / 8);
        header[33] = 0;
        //每个样本的数据位数
        header[34] = 16;
        header[35] = 0;
        //Data chunk
        header[36] = 'd';//data
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 44);
    }
}
}