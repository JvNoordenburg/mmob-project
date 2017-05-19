package uu.mbi.mmob.alternative_buttons.buttons;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.WindowManager;

import java.lang.ref.WeakReference;

import uu.mbi.mmob.alternative_buttons.io.algorithms.AlgorithmDetectTap;

import static android.content.Context.WINDOW_SERVICE;

/**
 * Created by Teun on 19-5-2017.
 */

public class ButtonBackTap implements SensorEventListener
{
    /* * * * * * * * * * * * * * * * * * */
    /*                                   */
    /*             STATIC                */
    /*                                   */
    /* * * * * * * * * * * * * * * * * * */

    public interface OnBackTapListener
    {   void onBackTap(ButtonBackTap button);}
    
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
    
    private SensorManager mSensorManager;
    private WeakReference<Activity> mWeakActivity;
    private WeakReference<OnBackTapListener> mWeakBackTapListener;
    
    private float[] mLastGyroValues;
    private long mLastTapTime;
    private long mDebunkerTime;
    private AlgorithmDetectTap mAlgorithmDetectTap;
    
    /* * * * * * * * * * * * * * * * * * */
    /*                                   */
    /*            CONSTRUCTION           */
    /*                                   */
    /* * * * * * * * * * * * * * * * * * */
    
    public ButtonBackTap (WeakReference<Activity> weakActivity)
    {
        mDebunkerTime = 750;
        mWeakActivity = weakActivity;
        mLastTapTime = 0;
        mAlgorithmDetectTap = new AlgorithmDetectTap();
        
        Activity strong = mWeakActivity.get();
        if(strong != null)
        {   mSensorManager = (SensorManager) strong.getSystemService(Context.SENSOR_SERVICE);}
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
                    boolean detected = mAlgorithmDetectTap.addMeasurement(mLastGyroValues[0], values[0], System.currentTimeMillis() - mLastTapTime > mDebunkerTime);
                    if(detected)
                    {
                        mLastTapTime = System.currentTimeMillis();
                        
                        if(mWeakBackTapListener != null)
                        {
                            OnBackTapListener strong = mWeakBackTapListener.get();
                            if(strong != null)
                            {   strong.onBackTap(this);}
                        }
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
    
    public void setOnBackTapListener(WeakReference<OnBackTapListener> weakListener)
    {   mWeakBackTapListener = weakListener;}
    
    /* * * * * * * * * * * * * * * * * * */
    /*                                   */
    /*                API                */
    /*                                   */
    /* * * * * * * * * * * * * * * * * * */
    
    public void startListening ()
    {
        Sensor accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor gyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_FASTEST);
    }
    
    public void stopListening ()
    {   mSensorManager.unregisterListener(this);}
    
    /* * * * * * * * * * * * * * * * * * */
    /*                                   */
    /*              HELPERS              */
    /*                                   */
    /* * * * * * * * * * * * * * * * * * */
    
    private Activity getActivity()
    {
        if(mWeakActivity == null)
        {   return null;}
    
        return mWeakActivity.get();
    }
    
    protected float[] canonicalToScreen (float[] eventValues)
    {
        Activity activity = getActivity();
        if(activity == null)
        {   return eventValues;}
        
        WindowManager windowManager = (WindowManager) activity.getSystemService(WINDOW_SERVICE);
        
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
