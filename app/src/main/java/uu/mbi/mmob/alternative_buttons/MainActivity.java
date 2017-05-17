package uu.mbi.mmob.alternative_buttons;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageButton mCameraButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCameraButton = (ImageButton) findViewById(R.id.ib_camera);
        mCameraButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        switch(view.getId()) {
            case R.id.ib_camera:
                Intent intent = new Intent(this, CameraActivity.class);
                startActivity(intent);
                break;
        }

    }

}
