package uu.mbi.mmob.alternative_buttons.io.algorithms;

/**
 * Created by Teun on 19-5-2017.
 */

public class AlgorithmDetectTap
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
    
    public interface DetectTapInterface
    {
        void onAlgorithmRun(Motion motion, boolean detected);
    }

    /* * * * * * * * * * * * * * * * * * */
    /*                                   */
    /*             MEMBERS               */
    /*                                   */
    /* * * * * * * * * * * * * * * * * * */
    
    private float[] mAccelerometerBuffer;
    private float[] mGyroscopeBuffer;
   // private float[] mStdGyroBuffer;
    private float[] mStdAccelBuffer;
   // private float[] mAccelMovingAverage;
    private float[] mGyroMovingAverage;

    private int mBufferSize;
    private int mCurrentIndex;
    
    private DetectTapInterface mDetectTapInterface;
    private int mNeuralThreshold;
    
    /* * * * * * * * * * * * * * * * * * */
    /*                                   */
    /*            CONSTRUCTION           */
    /*                                   */
    /* * * * * * * * * * * * * * * * * * */
    
    public AlgorithmDetectTap ()
    {
        mBufferSize = 32;
        mCurrentIndex = 0;

        mAccelerometerBuffer = new float[mBufferSize];
        mGyroscopeBuffer = new float[mBufferSize];
        mStdAccelBuffer = new float[mBufferSize];
        mGyroMovingAverage = new float[mBufferSize];
    }

    /* * * * * * * * * * * * * * * * * * */
    /*                                   */
    /*              EVENTS               */
    /*                                   */
    /* * * * * * * * * * * * * * * * * * */
    
    /* * * * * * * * * * * * * * * * * * */
    /*                                   */
    /*         GETTERS & SETTERS         */
    /*                                   */
    /* * * * * * * * * * * * * * * * * * */
    
    public void setDetectTapInterface(DetectTapInterface listener)
    {   mDetectTapInterface = listener;}
    
    /* * * * * * * * * * * * * * * * * * */
    /*                                   */
    /*                API                */
    /*                                   */
    /* * * * * * * * * * * * * * * * * * */
    
    public boolean addMeasurement(float gyro, float accel, boolean runIfPossible)
    {
        if(mCurrentIndex < mBufferSize - 1)
        {
            mGyroscopeBuffer[mCurrentIndex] = gyro;
            mAccelerometerBuffer[mCurrentIndex] = accel;
        
            mGyroMovingAverage[mCurrentIndex] = getMean(mGyroscopeBuffer);
            mStdAccelBuffer[mCurrentIndex] = getStandardDeviation(mAccelerometerBuffer, getMean(mAccelerometerBuffer));
            
            mCurrentIndex += 1;
            return false;
        }
    
        mAccelerometerBuffer = shift(mAccelerometerBuffer, accel);
        mGyroscopeBuffer = shift(mGyroscopeBuffer, gyro);
    
        mGyroMovingAverage = shift(mGyroMovingAverage, getMean(mGyroscopeBuffer));
        mStdAccelBuffer = shift(mStdAccelBuffer, getStandardDeviation(mAccelerometerBuffer, getMean(mAccelerometerBuffer)));
        
        if(runIfPossible == false)
        {   return false;}
        
        boolean tapDetected = runAlgorithm(mAccelerometerBuffer, mGyroscopeBuffer);
        return tapDetected;
    }
    
    /* * * * * * * * * * * * * * * * * * */
    /*                                   */
    /*              HELPERS              */
    /*                                   */
    /* * * * * * * * * * * * * * * * * * */

    private boolean runAlgorithm(float[] accel, float[] gyro)
    {
        Motion motion = new Motion(accel, gyro);
        boolean tapDetected = motion.isTap();
        
        if(mDetectTapInterface != null)
        {   mDetectTapInterface.onAlgorithmRun(motion, tapDetected);}
    
        return tapDetected;
    }
    
    private float[] shift(float[] values, float newValue)
    {
        float[] shifted = new float[values.length];
        for(int i = values.length - 1; i >= 1 ; i--)
        {   shifted[i - 1] = values[i];}
    
        shifted[shifted.length - 1] = newValue;
        return shifted;
    }
    
    
    private float detectPeak(float[] values, float average)
    {
        float highestPeakDelta = 0;
        
        for(Float value : values)
        {
            float delta = value - average;
            if(delta > highestPeakDelta)
            {   highestPeakDelta = delta;}
        }
        
        return highestPeakDelta;
    }
    
    private float detectValley(float[] values, float average)
    {
        float lowestValleyDelta = 0;
        
        for(Float value : values)
        {
            float delta = value - average;
            if(delta < lowestValleyDelta)
            {   lowestValleyDelta = delta;}
        }
        
        return lowestValleyDelta;
    }
    
    private static float getMean (float[] values)
    {
        float sum = 0;
        for(Float value : values)
        {   sum += value;}
        
        return sum / (float) values.length;
    }
    
    private static float getStandardDeviation(float[] values, float average)
    {
        double ssd = 0;
        for(Float value : values)
        {   ssd += Math.pow(value - average, 2);}
        
        return (float) Math.sqrt(ssd / values.length);
    }
    
    public class Motion
    {
        public float mGyroAverage;
        public float mGyroPeak;
        public float mGyroAveragePeak;
        public float mAccelAverage;
        public float mAccelPeak;
        public float mAccelStd;

        public Motion (final float[] accel, final float[] gyro)
        {
            mGyroAverage = getMean(gyro);
            mGyroPeak = detectPeak(gyro, mGyroAverage);
            mGyroAveragePeak = detectPeak(mGyroMovingAverage, getMean(mGyroMovingAverage));
            
            mAccelAverage = getMean(accel);
            mAccelPeak = detectPeak(accel, mAccelAverage);
            mAccelStd = mStdAccelBuffer[mStdAccelBuffer.length - 1];
        }
    
        public boolean isTap ()
        {
            boolean gyroPeakHighEnough = mGyroPeak > 3;
            boolean accelPeakLessExtreme = mAccelPeak <  (mGyroPeak * 0.85f);
            boolean stdAccelWithinBounds = mAccelStd < 1 && mAccelStd > -1;
            boolean averageGyroPeakSmallEnough = mGyroAveragePeak < 0.8f;
            
            return gyroPeakHighEnough && accelPeakLessExtreme && stdAccelWithinBounds && averageGyroPeakSmallEnough;
        }
    }
    
    
}
