package uu.mbi.mmob.alternative_buttons.io.excel;

import android.hardware.Sensor;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import uu.mbi.mmob.alternative_buttons.io.algorithms.AlgorithmDetectTap;

/**
 * Created by Jasper on 23-May-17.
 */

public class CameraExcelWriter {

    private WritableWorkbook mWorkbook;
    private WritableSheet mSheet;
    private File mFile;
    private int mCurrentRow;

    /* * * * * * * * * * * * * * * * * * */
    /*                                   */
    /*            CONSTRUCTION           */
    /*                                   */
    /* * * * * * * * * * * * * * * * * * */

    public CameraExcelWriter(String fileName) {
        try {
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

            writeHeaders();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (WriteException e) {
            e.printStackTrace();
        }
    }

    public void addMeasurement(long timestamp, int[] colors) {
        writeColors(timestamp, colors);

        mCurrentRow += 1;
    }

    private void writeColors(long timestamp, int[] values) {
        try {
            writeCell(0, mCurrentRow, String.valueOf(timestamp));

            for (int i = 0; i < values.length; i++) {
                writeCell(i + 1, mCurrentRow, String.valueOf(values[i]));
            }
        } catch (WriteException e) {
            e.printStackTrace();
            Log.d("EXCEL", "WRITEEXCEPTION");
        }

    }

    public File finish() {
        if (mWorkbook == null) {
            return null;
        }

        try {
            mWorkbook.write();
            mWorkbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (WriteException e) {
            e.printStackTrace();
        }

        return mFile;
    }

    /* * * * * * * * * * * * * * * * * * */
    /*                                   */
    /*              HELPERS              */
    /*                                   */
    /* * * * * * * * * * * * * * * * * * */

    private void writeHeaders() throws WriteException {
        writeCell(0, 0, "Timestamp");
        writeCell(1, 0, "R");
        writeCell(2, 0, "G");
        writeCell(3, 0, "B");
    }

    private void writeCell(int column, int row, String value) throws WriteException {
        Label label = new Label(column, row, value);
        mSheet.addCell(label);
    }

}
