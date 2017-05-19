package uu.mbi.mmob.alternative_buttons.io.excel;

import android.hardware.Sensor;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import uu.mbi.mmob.alternative_buttons.io.algorithms.AlgorithmDetectTap.Motion;

import static android.R.attr.type;

/**
 * Created by Teun on 18-5-2017.
 */

public class MeasurementExcelWriter
{
    
    /* * * * * * * * * * * * * * * * * * */
    /*                                   */
    /*             STATIC                */
    /*                                   */
    /* * * * * * * * * * * * * * * * * * */
  
    public enum SensorType
    {
        Accelerometer(0),
        Gyroscope(4);
    
    
        private int mStartColumnIndex;
        SensorType(int startColumnIndex)
        {   mStartColumnIndex = startColumnIndex;}
    
        public int getStartColumnIndex()
        {   return mStartColumnIndex;}
    
        public static SensorType fromIdentifier (final int type)
        {
            if(type == Sensor.TYPE_ACCELEROMETER)
            {   return Accelerometer;}
            
            if(type == Sensor.TYPE_GYROSCOPE || type == Sensor.TYPE_GYROSCOPE_UNCALIBRATED)
            {   return Gyroscope;}
            
            return null;
        }
    }
    
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
    
    private WritableWorkbook mWorkbook;
    private WritableSheet mSheet;
    private File mFile;
    private int mCurrentRow;
    
    /* * * * * * * * * * * * * * * * * * */
    /*                                   */
    /*            CONSTRUCTION           */
    /*                                   */
    /* * * * * * * * * * * * * * * * * * */
    
    public MeasurementExcelWriter (String fileName)
    {
        try
        {
            File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            directory.mkdirs();
    
            mFile = new File(directory + File.separator + fileName);
            mFile.createNewFile();
    
            WorkbookSettings settings = new WorkbookSettings();
            settings.setLocale(new Locale("en", "EN"));
    
            mCurrentRow = 2;
            
            mWorkbook = Workbook.createWorkbook(mFile, settings);
            mWorkbook.createSheet("Measurements", 0);
    
            mSheet = mWorkbook.getSheet(0);
    
            writeHeader(SensorType.Accelerometer);
            writeHeader(SensorType.Gyroscope);
        }
        catch(IOException e)
        {   e.printStackTrace();}
        catch (WriteException e)
        {   e.printStackTrace();}
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
    
    /* * * * * * * * * * * * * * * * * * */
    /*                                   */
    /*                API                */
    /*                                   */
    /* * * * * * * * * * * * * * * * * * */
    
    public void writeAlgorithmData(Motion motion, boolean detected)
    {
        try
        {
            int columnIndexStart = SensorType.Gyroscope.getStartColumnIndex() + 4;
    
            writeCell(columnIndexStart + 0, mCurrentRow, String.valueOf(motion.mAccelPeak));
            writeCell(columnIndexStart + 1, mCurrentRow, String.valueOf(motion.mGyroPeak));
            writeCell(columnIndexStart + 2, mCurrentRow, String.valueOf(motion.mGyroAveragePeak));

    
            /*writeCell(columnIndexStart + 0, mCurrentRow, String.valueOf(motion.mAccel));
            writeCell(columnIndexStart + 1, mCurrentRow, String.valueOf(motion.mAccelAverage));
            writeCell(columnIndexStart + 2, mCurrentRow, String.valueOf(motion.mAccelAveragePeak));
            writeCell(columnIndexStart + 3, mCurrentRow, String.valueOf(motion.mAccelAverageValley));
            writeCell(columnIndexStart + 4, mCurrentRow, String.valueOf(motion.mAccelPeak));
            writeCell(columnIndexStart + 5, mCurrentRow, String.valueOf(motion.mAccelValley));
    
            writeCell(columnIndexStart + 6, mCurrentRow, String.valueOf(motion.mGyro));
            writeCell(columnIndexStart + 7, mCurrentRow, String.valueOf(motion.mGyroAverage));
            writeCell(columnIndexStart + 8, mCurrentRow, String.valueOf(motion.mGyroAveragePeak));
            writeCell(columnIndexStart + 9, mCurrentRow, String.valueOf(motion.mGyroAverageValley));
            writeCell(columnIndexStart + 10, mCurrentRow, String.valueOf(motion.mGyroPeak));
            writeCell(columnIndexStart + 11, mCurrentRow, String.valueOf(motion.mGyroValley));
    
            writeCell(columnIndexStart + 12, mCurrentRow, String.valueOf(motion.mAccelStd));
            writeCell(columnIndexStart + 13, mCurrentRow, String.valueOf(motion.mAccelStdAverage));
            writeCell(columnIndexStart + 14, mCurrentRow, String.valueOf(motion.mAccelStdPeak));
            writeCell(columnIndexStart + 15, mCurrentRow, String.valueOf(motion.mAccelStdValley));
    
            writeCell(columnIndexStart + 16, mCurrentRow, String.valueOf(motion.mGyroStd));
            writeCell(columnIndexStart + 17, mCurrentRow, String.valueOf(motion.mGyroStdAverage));
            writeCell(columnIndexStart + 18, mCurrentRow, String.valueOf(motion.mGyroStdPeak));
            writeCell(columnIndexStart + 19, mCurrentRow, String.valueOf(motion.mGyroStdValley));*/
            
            writeCell(columnIndexStart + 20, mCurrentRow, String.valueOf(detected));
        }
        catch (WriteException e)
        {   e.printStackTrace();}
    }
    
    public void addMeasurement(long timestamp, float[] accel, float[]gyro)
    {
        writeMeasurement(timestamp, accel, SensorType.Accelerometer);
        writeMeasurement(timestamp, accel, SensorType.Gyroscope);
    
        mCurrentRow += 1;
    }
    
    private void writeMeasurement(long timestamp, float[] values, SensorType type)
    {
        int startColumnIndex = type.getStartColumnIndex();

        try
        {
            writeCell(startColumnIndex, mCurrentRow, String.valueOf(timestamp));
        
            for(int i = 0 ; i < values.length ; i++)
            {   writeCell(startColumnIndex + i + 1, mCurrentRow, String.valueOf(values[i]));}
        }
        catch(WriteException e)
        {   e.printStackTrace();}

    }
    
    public File finish()
    {
        if(mWorkbook == null)
        {   return null;}
        
        try
        {
            mWorkbook.write();
            mWorkbook.close();
        }
        catch(IOException e)
        {   e.printStackTrace();}
        catch (WriteException e)
        {   e.printStackTrace();}
        
        return mFile;
    }
    
    /* * * * * * * * * * * * * * * * * * */
    /*                                   */
    /*              HELPERS              */
    /*                                   */
    /* * * * * * * * * * * * * * * * * * */
    
    private void writeHeader(SensorType type) throws WriteException
    {
        int startColumnIndex = type.getStartColumnIndex();
        String name = type.toString();
        
        writeCell(startColumnIndex, 0, "Timestamp");
        writeCell(startColumnIndex + 1, 0, name + " X");
        writeCell(startColumnIndex + 2, 0, name + " Y");
        writeCell(startColumnIndex + 3, 0, name + " Z");
    }
    
    private void writeCell(int column, int row, String value) throws WriteException
    {
        Label label = new Label(column, row, value);
        mSheet.addCell(label);
    }
}
