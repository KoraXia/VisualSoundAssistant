package com.koraxia.activity;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.koraxia.R;
import com.koraxia.tester.AudioCaptureTester;
import com.koraxia.tester.AudioPlayerTester;
import com.koraxia.tester.Tester;

public class MainActivity extends AppCompatActivity {

    private Spinner mTestSpinner;
    private Tester mTester;
    boolean f = true;

    public static final String[] TEST_PROGRAM_ARRAY = {
            "Record .wav file",
            "Play .wav file",
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTestSpinner = (Spinner) findViewById(R.id.TestSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, TEST_PROGRAM_ARRAY);
        mTestSpinner.setAdapter(adapter);

        Log.d("MAIN", "onCreate: " + Environment.getExternalStorageDirectory() + "/test.wav");
    }

    //start test
    public void onClickStartTest(View v) throws InterruptedException {
        switch (mTestSpinner.getSelectedItemPosition()) {
            case 0:
                mTester = new AudioCaptureTester();
                break;
            case 1:
                mTester = new AudioPlayerTester();
                break;
            default:
                break;
        }
        //loop to
        if (mTester != null) {
            Toast.makeText(this, "Start Testing! ", Toast.LENGTH_LONG).show();
            mTester.startTesting();
        }
    }

    //stop test
    public void onClickStopTest(View v) {
        if (mTester != null) {
            //f = false;
            mTester.stopTesting();
            //Toast.makeText(this, "Stop Testing !", Toast.LENGTH_SHORT).show();
            //output the text
            //Toast.makeText(this, "classification result", Toast.LENGTH_LONG).show();
        }
    }
}
