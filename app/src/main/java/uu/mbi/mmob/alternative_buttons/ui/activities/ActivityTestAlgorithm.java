package uu.mbi.mmob.alternative_buttons.ui.activities;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import uu.mbi.mmob.alternative_buttons.R;
import uu.mbi.mmob.alternative_buttons.io.algorithms.AlgorithmDetectTap;

/**
 * Created by Teun on 19-5-2017.
 */

public class ActivityTestAlgorithm extends AppCompatActivity implements SensorEventListener
{
    /* * * * * * * * * * * * * * * * * * */
    /*                                   */
    /*             STATIC                */
    /*                                   */
    /* * * * * * * * * * * * * * * * * * */
    
    private static final long DEBUNKER_TIME = 400;
    private static final int BUFFER_SIZE = 32;
    
    /* * * * * * * * * * * * * * * * * * */
    /*                                   */
    /*            ABSTRACT               */
    /*                                   */
    /* * * * * * * * * * * * * * * * * * */

    /* * * * * * * * * * * * * * * * * * */
    /*                                   */
    /*             MEMBERS               */
    /*                                   */
    /* * * * * * * * * * * * * * * * * * */
    
    private TextView mTvTapCounter;
    private TextView mTvHighestPeak;
    private LinearLayout mLlLogParent;
    private ScrollView mScrollView;
    private SensorManager mSensorManager;
    private AlgorithmDetectTap mAlgorithmDetectTap;
    private float[] mLastGyroValues;
    private long mLastTapTime;
    private int mTapCount;
    
    /* * * * * * * * * * * * * * * * * * */
    /*                                   */
    /*            CONSTRUCTION           */
    /*                                   */
    /* * * * * * * * * * * * * * * * * * */
    
    @Override
    protected void onCreate (@Nullable final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_algorithm);
    
        mTvTapCounter = (TextView) findViewById(R.id.activity_test_algorithm_tvTapCounter);
        mTvHighestPeak = (TextView) findViewById(R.id.activity_test_algorithm_tvHighestPeak);
        mLlLogParent = (LinearLayout) findViewById(R.id.activity_test_algorithm_llLogParent);
        mScrollView = (ScrollView) findViewById(R.id.activity_test_algorithm_svScrollView);
        
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAlgorithmDetectTap = new AlgorithmDetectTap();
        mLastTapTime = 0;
        mTapCount = 0;
    }
    
    @Override
    protected void onResume ()
    {
        super.onResume();
        Sensor accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor gyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_FASTEST);
    }
    
    @Override
    protected void onPause ()
    {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }
    
    /* * * * * * * * * * * * * * * * * * */
    /*                                   */
    /*              EVENTS               */
    /*                                   */
    /* * * * * * * * * * * * * * * * * * */
    
    @Override
    public void onSensorChanged (final SensorEvent event)
    {
        float[] values = canonicalToScreen(event.values);
    
        switch(event.sensor.getType())
        {
            case Sensor.TYPE_ACCELEROMETER:
           
                if(mLastGyroValues != null)
                {
                    boolean detected = mAlgorithmDetectTap.addMeasurement(mLastGyroValues[0], values[0], System.currentTimeMillis() - mLastTapTime > DEBUNKER_TIME);
                    if(detected)
                    {
                        log("Tap", "Detected");
                        mLastTapTime = System.currentTimeMillis();
                        mTapCount += 1;
                        
                        mTvTapCounter.setText(String.valueOf(mTapCount));
                    }
                }
    
                break;
            
            case Sensor.TYPE_GYROSCOPE:
                
                mLastGyroValues = values;
                
                break;
        }
    }
    
    @Override
    public void onAccuracyChanged (final Sensor sensor, final int accuracy)
    {
        
    }

    /* * * * * * * * * * * * * * * * * * */
    /*                                   */
    /*         GETTERS & SETTERS         */
    /*                                   */
    /* * * * * * * * * * * * * * * * * * */
    
    /* * * * * * * * * * * * * * * * * * */
    /*                                   */
    /*                API                */
    /*                                   */
    /* * * * * * * * * * * * * * * * * * */
    
    /* * * * * * * * * * * * * * * * * * */
    /*                                   */
    /*              HELPERS              */
    /*                                   */
    /* * * * * * * * * * * * * * * * * * */
    
    private void log(String tag, String message)
    {
        TextView log = new TextView(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        
        log.setText(tag + ": " + message);
        log.setLayoutParams(lp);

        mLlLogParent.addView(log);
        mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
    }
    
    protected float[] canonicalToScreen (float[] eventValues)
    {
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        
        int displayRotation = windowManager.getDefaultDisplay().getRotation();
        
        float[] adjustedValues = new float[3];
        
        final int axisSwap[][] = {{1, - 1, 0, 1}, // ROTATION_0
                {- 1, - 1, 1, 0}, // ROTATION_90
                {- 1, 1, 0, 1}, // ROTATION_180
                {1, 1, 1, 0} // ROTATION_270
        };
        
        final int[] as = axisSwap[displayRotation];
        adjustedValues[0] = (float) as[0] * eventValues[as[2]];
        adjustedValues[1] = (float) as[1] * eventValues[as[3]];
        adjustedValues[2] = eventValues[2];
        
        return adjustedValues;
    }
}
