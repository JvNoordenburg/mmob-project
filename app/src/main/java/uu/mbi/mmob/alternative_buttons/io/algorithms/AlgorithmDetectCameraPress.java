package uu.mbi.mmob.alternative_buttons.io.algorithms;

import android.util.Log;

/**
 * Created by Jasper on 24-May-17.
 */

public class AlgorithmDetectCameraPress {

    public enum CameraPressState {
        PRESSED,
        RELEASED,
        NO_CHANGE
    }

    private static int NEEDED_REF_FRAMES = 3;
    private static float DETECTION_DEVIATION_NORM = -0.4f;

    private CameraPressState mCurrentState = CameraPressState.RELEASED;
    private int mReferenceCount, mRReference, mGReference, mBReference = 0;

    public AlgorithmDetectCameraPress() {

    }

    public CameraPressState addMeasurement(int rValue, int gValue, int bValue) {

        if(mReferenceCount < NEEDED_REF_FRAMES) {
            addReference(rValue, gValue, bValue);
            return CameraPressState.NO_CHANGE;
        }

        CameraPressState action = determineAction(rValue, gValue, bValue);

        if(action != mCurrentState) {
            mCurrentState = action;
            return action;
        } else {
            return CameraPressState.NO_CHANGE;
        }
    }

    private void addReference(int rValue, int gValue, int bValue) {

        if(gValue >= 50 && bValue >= 50) {

            mRReference += rValue;
            mGReference += gValue;
            mBReference += bValue;

            mReferenceCount++;

            if (mReferenceCount == NEEDED_REF_FRAMES) {
                mRReference = mRReference / mReferenceCount;
                mGReference = mGReference / mReferenceCount;
                mBReference = mBReference / mReferenceCount;
            }
        }
    }

    private float calculateDevation(int value, int reference) {
        float diff = value - reference;
        float result = diff / reference;
        return result;
    }

    private CameraPressState determineAction(int rValue, int gValue, int bValue) {
        float gDeviation = calculateDevation(gValue, mGReference);
        float bDevation = calculateDevation(bValue, mBReference);

        Log.i("ALGORITHM", "" + gDeviation + "; " + bDevation);

        if (gDeviation <= DETECTION_DEVIATION_NORM && bDevation <= DETECTION_DEVIATION_NORM) {
            return CameraPressState.PRESSED;
        } else {
            return CameraPressState.RELEASED;
        }
    }

}
