package com.koraxia.tester;

import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.Environment;

import com.jhuster.audiodemo.api.audio.AudioCapturer;
import com.jhuster.audiodemo.api.wav.WavFileWriter;
import com.wutong.api.MFCC.MFCC;

import java.io.IOException;
import java.util.Arrays;


public class AudioCaptureTester extends Tester implements AudioCapturer.OnAudioFrameCapturedListener {

    private static final String DEFAULT_TEST_FILE = Environment.getExternalStorageDirectory() + "/test.wav";

    private AudioCapturer mAudioCapturer;
    private WavFileWriter mWavFileWriter;

    MFCC mfcc;

    @Override
    public boolean startTesting() {
        mAudioCapturer = new AudioCapturer();  //录音
        mWavFileWriter = new WavFileWriter();  //.wav文件
        try {
            mWavFileWriter.openFile(DEFAULT_TEST_FILE, 44100, 1, 16);  //新建.wav文件
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        mAudioCapturer.setOnAudioFrameCapturedListener(this);  //set listener
        return mAudioCapturer.startCapture(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);  //开始录音
    }

    @Override
    public boolean stopTesting() {
        mAudioCapturer.stopCapture();
        try {
            mWavFileWriter.closeFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    @Override
    public void onAudioFrameCaptured(byte[] audioData) {
        //write data
        //mWavFileWriter.writeData(audioData, 0, audioData.length);
        double[] array = new double[audioData.length];
        //type parse
        for(int w= 0; w<audioData.length; w++) {
            array[w] = (double)audioData[w];
        }
        //Do mfcc
        mfcc = new MFCC();
        float[] m_array = mfcc.process(array);
        System.out.println(Arrays.toString(m_array));
        //send m_array into CNN

    }
}
