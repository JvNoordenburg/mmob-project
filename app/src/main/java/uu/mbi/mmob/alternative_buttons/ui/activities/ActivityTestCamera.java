package uu.mbi.mmob.alternative_buttons.ui.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Arrays;

import uu.mbi.mmob.alternative_buttons.R;
import uu.mbi.mmob.alternative_buttons.io.algorithms.AlgorithmDetectCameraPress;
import uu.mbi.mmob.alternative_buttons.io.excel.CameraExcelWriter;
import uu.mbi.mmob.alternative_buttons.utils.UtilityColor;

/**
 * Created by Jasper on 23-May-17.
 */

public class ActivityTestCamera extends AppCompatActivity implements View.OnClickListener {

    private static int REQUEST_CODE_CAMERA = 123;

    private TextureView mPreview;
    private TextView mButtonState;

    private CameraDevice mCameraDevice;
    private CameraManager mCameraManager;
    private String mCameraID;

    private AlgorithmDetectCameraPress mCameraAlgorithm;

    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            setupCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
            setupCamera();
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
            setupCamera();
        }
    };

    private CameraDevice.StateCallback mDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
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

    private ImageReader mImgReader;
    private ImageReader.OnImageAvailableListener mImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader imageReader) {
            Image image = imageReader.acquireNextImage();

            if (image.getPlanes() != null && mShouldMeasure) {
                final int[] colors = UtilityColor.calculateAvgColors(image);
                Log.i("HEYO", "R: "+ colors[0] + "G: "+ colors[1] + "B: "+ colors[2]);
                ActivityTestCamera.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mCameraExcelWriter.addMeasurement(System.currentTimeMillis(), colors);
                        AlgorithmDetectCameraPress.CameraPressState action = mCameraAlgorithm.addMeasurement(colors[0], colors[1], colors[2]);

                        if(action != AlgorithmDetectCameraPress.CameraPressState.NO_CHANGE) {
                            Log.i("ALGORITHM", action + "");
                            mButtonState.setText(action.toString());
                        } else {
                            //Log.i("ALGORITHM", action + "");
                        }
                    }
                });

            } else {
                Log.d("HEYO", "ImagePanes null");
            }

            image.close();
        }
    };

    private CameraExcelWriter mCameraExcelWriter;

    private boolean mShouldMeasure = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_camera);

        //mPreview = (TextureView) findViewById(R.id.activity_test_camera_tvPreview);
        Button bStart = (Button) findViewById(R.id.activity_test_camera_bStart);
        Button bStop = (Button) findViewById(R.id.activity_test_camera_bStop);

        mButtonState = (TextView) findViewById(R.id.activity_test_camera_tvButtonState);
        mButtonState.setText(AlgorithmDetectCameraPress.CameraPressState.RELEASED.toString());

        bStart.setOnClickListener(this);
        bStop.setOnClickListener(this);

        mCameraAlgorithm = new AlgorithmDetectCameraPress();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 12);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();

        //if (mPreview.isAvailable()) {
            setupCamera();
        //} else {
          //  mPreview.setSurfaceTextureListener(mSurfaceTextureListener);
        //}
    }

    @Override
    protected void onPause() {
        stopBackgroundThread();
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_CAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(getApplicationContext(), "Need permission", Toast.LENGTH_LONG);
            }
        }
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.activity_test_camera_bStart:
                mCameraExcelWriter = new CameraExcelWriter("measurements_" + System.currentTimeMillis() + ".xls");
                mShouldMeasure = true;
                break;
            case R.id.activity_test_camera_bStop:
                mShouldMeasure = false;
                if (mCameraExcelWriter != null) {
                    File file = mCameraExcelWriter.finish();
                }
                break;
        }
    }

    private void startBackgroundThread() {
        mBackgroundHandlerThread = new HandlerThread("CameraButtonImageThread");
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
        mCameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_CAMERA);
            } else {
                Toast.makeText(getApplicationContext(), "No permission", Toast.LENGTH_SHORT);
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
        mImgReader.setOnImageAvailableListener(mImageAvailableListener, mBackgroundHandler);

        Surface processingSurface = mImgReader.getSurface();

        //SurfaceTexture surfaceTexture = mPreview.getSurfaceTexture();
        //Surface previewSurface = new Surface(surfaceTexture);

        try {
            final CaptureRequest.Builder captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            //captureRequestBuilder.addTarget(previewSurface);
            captureRequestBuilder.addTarget(processingSurface);

            cameraDevice.createCaptureSession(Arrays.asList(processingSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    try {
                        cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    // RIP
                    Log.d("RIP", "onConfigureFailed");
                }
            }, mBackgroundHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }


}
