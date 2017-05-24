package uu.mbi.mmob.alternative_buttons.buttons;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Surface;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.Arrays;

import uu.mbi.mmob.alternative_buttons.io.algorithms.AlgorithmDetectCameraPress;
import uu.mbi.mmob.alternative_buttons.ui.activities.ActivityTestCamera;
import uu.mbi.mmob.alternative_buttons.utils.UtilityColor;

import static android.content.Context.CAMERA_SERVICE;

/**
 * Created by Jasper on 24-May-17.
 */

public class ButtonCamera implements ImageReader.OnImageAvailableListener {

    public interface OnCameraButtonListener {
        void onButtonPressed();
        void onButtonReleased();
    }

    private static int REQUEST_CODE_CAMERA_PERMISSION = 80085;
    private static String TAG = "ButtonCamera";

    private WeakReference<Activity> mWeakActivity;
    private WeakReference<OnCameraButtonListener> mWeakListener;

    private CameraDevice mCameraDevice;
    private CameraManager mCameraManager;
    private String mCameraID;

    private ImageReader mImgReader;

    private AlgorithmDetectCameraPress mCameraAlgorithm;

    private CameraDevice.StateCallback mDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            Log.i(TAG, "onOpened: Create capture request");
            mCameraDevice = cameraDevice;
            createCaptureRequest(cameraDevice);
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            cameraDevice.close();
        }
    };

    private HandlerThread mBackgroundHandlerThread;
    private Handler mBackgroundHandler;

    public ButtonCamera(WeakReference<Activity> weakActivity) {
        mWeakActivity = weakActivity;
    }

    public void setOnCameraButtonListener(WeakReference<OnCameraButtonListener> weakListener) {
        mWeakListener = weakListener;
    }

    public void startListening() {
        mCameraAlgorithm = new AlgorithmDetectCameraPress();
        startBackgroundThread();
        setupCamera();
        Log.i(TAG, "startListening: ");
    }

    public void stopListening() {
        mCameraDevice.close();
        stopBackgroundThread();
        mCameraAlgorithm = null;
        Log.i(TAG, "stopListening: ");
    }

    public void onRequestPermissionResult(int requestCode, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Activity strongActivity = mWeakActivity.get();
                if(strongActivity != null) {
                    Toast.makeText(strongActivity, "Need permission", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onImageAvailable(ImageReader imageReader) {
        Image image = imageReader.acquireNextImage();
        Log.i(TAG, "onImageAvailable: ");
        if (image.getPlanes() != null) {
            final Activity strongActivity = mWeakActivity.get();
            final OnCameraButtonListener strongListener = mWeakListener.get();
            
            if (strongListener != null && strongActivity != null) {
                final int[] colors = UtilityColor.calculateAvgColors(image);
                Log.i(TAG, "onImageAvailable: R: " + colors[0] + " G: " + colors[1] + " B: " + colors[2]);
                final AlgorithmDetectCameraPress.CameraPressState action = mCameraAlgorithm.addMeasurement(colors[0], colors[1], colors[2]);

                strongActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "onImageAvailable: " + action.toString());
                        if (action == AlgorithmDetectCameraPress.CameraPressState.PRESSED) {
                            strongListener.onButtonPressed();
                            Log.i(TAG, "onImageAvailable: Pressed");
                        } else if (action == AlgorithmDetectCameraPress.CameraPressState.RELEASED) {
                            strongListener.onButtonReleased();
                            Log.i(TAG, "onImageAvailable: Released");
                        }
                    }
                });
            }
        }

        image.close();
    }

    private void startBackgroundThread() {
        mBackgroundHandlerThread = new HandlerThread("ButtonCameraThread");
        mBackgroundHandlerThread.start();

        mBackgroundHandler = new Handler(mBackgroundHandlerThread.getLooper());
    }

    private void stopBackgroundThread() {
        if(mBackgroundHandlerThread != null) {
            mBackgroundHandlerThread.quitSafely();
            try {
                mBackgroundHandlerThread.join();
                mBackgroundHandlerThread = null;
                mBackgroundHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void setupCamera() {
        Activity strongActivity = mWeakActivity.get();

        if(strongActivity == null) return;

        mCameraManager = (CameraManager) strongActivity.getSystemService(CAMERA_SERVICE);
        try {
            for (String cameraID : mCameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraID);

                Integer rear = characteristics.get(CameraCharacteristics.LENS_FACING);

                if (rear != null && rear == CameraCharacteristics.LENS_FACING_BACK) {
                    mCameraID = cameraID;
                    openCamera();
                    return;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openCamera() {
        Log.i(TAG, "openCamera: Before check");
        Activity strongActivity = mWeakActivity.get();

        if (strongActivity == null) return;

        Log.i(TAG, "openCamera: after check ");
        if (ActivityCompat.checkSelfPermission(strongActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                strongActivity.requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_CAMERA_PERMISSION);
            } else {
                Toast.makeText(strongActivity, "Camera permission required to use camera button", Toast.LENGTH_SHORT).show();
            }
        } else {
            try {
                mCameraManager.openCamera(mCameraID, mDeviceStateCallback, mBackgroundHandler);

            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void createCaptureRequest(CameraDevice cameraDevice) {

        mImgReader = ImageReader.newInstance(100, 100, ImageFormat.JPEG, 1);
        mImgReader.setOnImageAvailableListener(this, mBackgroundHandler);

        Surface processingSurface = mImgReader.getSurface();

        try {
            final CaptureRequest.Builder captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            //captureRequestBuilder.addTarget(previewSurface);
            captureRequestBuilder.addTarget(processingSurface);

            cameraDevice.createCaptureSession(Arrays.asList(processingSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Log.i(TAG, "onConfigured: ");
                    try {
                        cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Log.i(TAG, "onConfigureFailed: ");
                }
            }, mBackgroundHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }


}
