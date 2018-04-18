/*
 *  COPYRIGHT NOTICE
 *  Copyright (C) 2016, Jhuster <lujun.hust@gmail.com>
 *  https://github.com/Jhuster/AudioDemo
 *
 *  @license under the Apache License, Version 2.0
 *
 *  @file    AudioCapturer.java
 *
 *  @version 1.0
 *  @author  Jhuster
 *  @date    2016/03/19
 */
package com.jhuster.audiodemo.api.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

public class AudioCapturer {

    private static final String TAG = "AudioCapturer";

    private static final int DEFAULT_SOURCE = MediaRecorder.AudioSource.MIC;
    private static final int DEFAULT_SAMPLE_RATE = 44100;
    private static final int DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int DEFAULT_DATA_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    // Make sure the sample size is the same in different devices
    private static final int SAMPLES_PER_FRAME = 1024;

    // AudioRecord
    private AudioRecord mAudioRecord;

    private Thread mCaptureThread;
    private boolean mIsCaptureStarted = false;  //为true则已经开始采集，为false则没有开始采集
    private volatile boolean mIsLoopExit = false;

    private OnAudioFrameCapturedListener mAudioFrameCapturedListener;

    public interface OnAudioFrameCapturedListener {
        void onAudioFrameCaptured(byte[] audioData);
    }	

    public boolean isCaptureStarted() {		
        return mIsCaptureStarted;
    }

    public void setOnAudioFrameCapturedListener(OnAudioFrameCapturedListener listener) {
        mAudioFrameCapturedListener = listener;
    }

    public boolean startCapture() {
        return startCapture(DEFAULT_SOURCE, DEFAULT_SAMPLE_RATE, DEFAULT_CHANNEL_CONFIG, DEFAULT_DATA_FORMAT);
    }

    /**
     * 1. 录音开始
     * 2. 音频处理
     * 3. 返回true表示正在录音
     **/
    public boolean startCapture(int audioSource, int sampleRateInHz, int channelConfig, int audioFormat) {

        if (mIsCaptureStarted) {  //已经开始采集
            Log.e(TAG, "Capture already started !");
            return false;
        }

        int minBufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);  //建立缓冲区大小
        if (minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            Log.e(TAG, "Invalid parameter !");
            return false;
        }

        mAudioRecord = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, minBufferSize * 4);
        if (mAudioRecord.getState() == AudioRecord.STATE_UNINITIALIZED) {
            Log.e(TAG, "AudioRecord initialize fail !");
            return false;
        }

        mAudioRecord.startRecording();  //开始录音

        mIsLoopExit = false;
        mCaptureThread = new Thread(new AudioCaptureRunnable());  //新建线程开始录音
        mCaptureThread.start();  //启动线程

        mIsCaptureStarted = true;  //设置为true，表示正在录音

        Log.i(TAG, "Start audio capture success !");

        return mIsCaptureStarted;
    }

    public void stopCapture() {
        if (!mIsCaptureStarted) {
            return;
        }

        mIsLoopExit = true;
        try {
            mCaptureThread.interrupt();
            mCaptureThread.join(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
            mAudioRecord.stop();
        }

        mAudioRecord.release();

        mIsCaptureStarted = false;
        mAudioFrameCapturedListener = null;

        Log.i(TAG, "Stop audio capture success! ");
    }

    private class AudioCaptureRunnable implements Runnable {
        @Override
        public void run() {
            while (!mIsLoopExit) {
                byte[] buffer = new byte[SAMPLES_PER_FRAME * 2];  //buffer长度，现在为2 byte
                int ret = mAudioRecord.read(buffer, 0, buffer.length);  //从缓冲区中读取数据
                if (ret == AudioRecord.ERROR_INVALID_OPERATION) {  //无效命令
                    Log.e(TAG, "Error ERROR_INVALID_OPERATION");
                } else if (ret == AudioRecord.ERROR_BAD_VALUE) { //数据损坏
                    Log.e(TAG, "Error ERROR_BAD_VALUE");
                } else {  //数据正常
                    Log.d("TAG", "Audio captured: " + buffer.length);
                    if (mAudioFrameCapturedListener != null) {  //监听正常
                        mAudioFrameCapturedListener.onAudioFrameCaptured(buffer);  //采集音频数据后，进行处理
                    }
                }
            }
        }
    }

}
