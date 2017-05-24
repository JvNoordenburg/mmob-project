package uu.mbi.mmob.alternative_buttons.ui.activities;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.lang.ref.WeakReference;

import uu.mbi.mmob.alternative_buttons.R;
import uu.mbi.mmob.alternative_buttons.buttons.ButtonCamera;
import uu.mbi.mmob.alternative_buttons.io.algorithms.AlgorithmDetectCameraPress;
import uu.mbi.mmob.alternative_buttons.io.excel.CameraExcelWriter;

/**
 * Created by Jasper on 17-May-17.
 */

public class ActivityButtonCamera extends AppCompatActivity implements View.OnClickListener, ButtonCamera.OnCameraButtonListener {

    private TextView mButtonState;
    private ButtonCamera mButtonCamera;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_button_camera);

        Button bStart = (Button) findViewById(R.id.activity_button_camera_bStart);
        Button bStop = (Button) findViewById(R.id.activity_button_camera_bStop);

        mButtonState = (TextView) findViewById(R.id.activity_button_camera_tvButtonState);
        mButtonState.setText(AlgorithmDetectCameraPress.CameraPressState.RELEASED.toString());

        bStart.setOnClickListener(this);
        bStop.setOnClickListener(this);

        mButtonCamera = new ButtonCamera(new WeakReference<Activity>(this));
        mButtonCamera.setOnCameraButtonListener(new WeakReference<ButtonCamera.OnCameraButtonListener>(this));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(mButtonCamera != null) {
            mButtonCamera.onRequestPermissionResult(requestCode, grantResults);
        }
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.activity_button_camera_bStart:
                mButtonCamera.startListening();
                break;
            case R.id.activity_button_camera_bStop:
                mButtonCamera.stopListening();
                break;
        }
    }

    @Override
    public void onButtonPressed() {
        mButtonState.setText(AlgorithmDetectCameraPress.CameraPressState.PRESSED.toString());
    }

    @Override
    public void onButtonReleased() {
        mButtonState.setText(AlgorithmDetectCameraPress.CameraPressState.RELEASED.toString());
    }
}
