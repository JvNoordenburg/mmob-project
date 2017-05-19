package uu.mbi.mmob.alternative_buttons.ui.activities;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;

import uu.mbi.mmob.alternative_buttons.R;
import uu.mbi.mmob.alternative_buttons.io.algorithms.AlgorithmDetectTap;
import uu.mbi.mmob.alternative_buttons.io.algorithms.AlgorithmDetectTap.DetectTapInterface;
import uu.mbi.mmob.alternative_buttons.io.algorithms.AlgorithmDetectTap.Motion;
import uu.mbi.mmob.alternative_buttons.io.excel.MeasurementExcelWriter;
import uu.mbi.mmob.alternative_buttons.io.excel.MeasurementExcelWriter.SensorType;
import uu.mbi.mmob.alternative_buttons.utils.UtilityEmail;

/**
 * Created by Teun on 18-5-2017.
 */

public class ActivityTestImu extends AppCompatActivity implements SensorEventListener, OnClickListener, DetectTapInterface
{
    /* * * * * * * * * * * * * * * * * * */
    /*                                   */
    /*             STATIC                */
    /*                                   */
    /* * * * * * * * * * * * * * * * * * */
    
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
    
    private TextView mAccelerometerX;
    private TextView mAccelerometerY;
    private TextView mAccelerometerZ;
    
    private TextView mGyroscopeX;
    private TextView mGyroscopeY;
    private TextView mGyroscopeZ;
    
    private Button mButtonStartMeasuring;
    private boolean mIsMeasuring;
    private float[] mLastGyroValues;
    
    private SensorManager mSensorManager;
    private MeasurementExcelWriter mMeasurementExcelWriter;
    private AlgorithmDetectTap mAlgorithmDetectTap;
    
    /* * * * * * * * * * * * * * * * * * */
    /*                                   */
    /*            CONSTRUCTION           */
    /*                                   */
    /* * * * * * * * * * * * * * * * * * */
    
    
    @Override
    protected void onCreate (@Nullable final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_measure_imu);
        
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mIsMeasuring = false;
        
        mAccelerometerX = (TextView) findViewById(R.id.activity_tap_bAccelerometerX);
        mAccelerometerY = (TextView) findViewById(R.id.activity_tap_bAccelerometerY);
        mAccelerometerZ = (TextView) findViewById(R.id.activity_tap_bAccelerometerZ);
        mGyroscopeX = (TextView) findViewById(R.id.activity_tap_bGyroscopeX);
        mGyroscopeY = (TextView) findViewById(R.id.activity_tap_bGyroscopeY);
        mGyroscopeZ = (TextView) findViewById(R.id.activity_tap_bGyroscopeZ);
        
        mButtonStartMeasuring = (Button) findViewById(R.id.activity_tap_bStartMeasuring);
        mButtonStartMeasuring.setOnClickListener(this);
        
        long now = System.currentTimeMillis();
        mMeasurementExcelWriter = new MeasurementExcelWriter("measurements_" + now + ".xls");
        mAlgorithmDetectTap = new AlgorithmDetectTap();
        mAlgorithmDetectTap.setDetectTapInterface(this);
    }
    
    @Override
    protected void onDestroy ()
    {
        super.onDestroy();
        mSensorManager.unregisterListener(this);
    }

    /* * * * * * * * * * * * * * * * * * */
    /*                                   */
    /*              EVENTS               */
    /*                                   */
    /* * * * * * * * * * * * * * * * * * */
    
    @Override
    public void onClick (final View v)
    {
        switch(v.getId())
        {
            case R.id.activity_tap_bStartMeasuring:
                
                if(mIsMeasuring)
                {
                    mSensorManager.unregisterListener(this);
                    File file = mMeasurementExcelWriter.finish();
                    UtilityEmail.mailFile(this, new String[]{"teun_248@hotmail.com"}, file);
                }
                else
                {
                    Sensor accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                    Sensor gyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    
                    mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
                    mSensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_FASTEST);
                }
    
                mIsMeasuring = ! mIsMeasuring;
                break;
        }
    }
    
    @Override
    public void onAlgorithmRun (Motion motion, boolean detected)
    {
        if(mMeasurementExcelWriter != null)
        {   mMeasurementExcelWriter.writeAlgorithmData(motion, detected);}
    }
    
    @Override
    public void onSensorChanged (final SensorEvent event)
    {
        float[] values = canonicalToScreen(event.values);
        switch (event.sensor.getType())
        {
            case Sensor.TYPE_ACCELEROMETER:
                
                mAccelerometerX.setText(String.valueOf(values[0]));
                mAccelerometerY.setText(String.valueOf(values[1]));
                mAccelerometerZ.setText(String.valueOf(values[2]));
    
                if(mMeasurementExcelWriter != null)
                {
                    if(mLastGyroValues != null)
                    {   mAlgorithmDetectTap.addMeasurement(mLastGyroValues[0], values[0], true);}
                    
                    mMeasurementExcelWriter.addMeasurement(System.currentTimeMillis(), values, mLastGyroValues);
                }
                
                break;
            
            case Sensor.TYPE_GYROSCOPE:
                
                mGyroscopeX.setText(String.valueOf(values[0]));
                mGyroscopeY.setText(String.valueOf(values[1]));
                mGyroscopeZ.setText(String.valueOf(values[2]));

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
