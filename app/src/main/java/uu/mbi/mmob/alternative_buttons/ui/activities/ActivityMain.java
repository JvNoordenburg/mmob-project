package uu.mbi.mmob.alternative_buttons.ui.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import uu.mbi.mmob.alternative_buttons.R;

public class ActivityMain extends AppCompatActivity implements View.OnClickListener
{
    
    private ImageButton mCameraButton;
    
    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mCameraButton = (ImageButton) findViewById(R.id.ib_camera);
        mCameraButton.setOnClickListener(this);
    
        Button bTestAlgorithm = (Button) findViewById(R.id.activity_main_bTestAlgorithm);
        bTestAlgorithm.setOnClickListener(this);
    
        Button bTestImu = (Button) findViewById(R.id.activity_main_bTestImu);
        bTestImu.setOnClickListener(this);
    
        Button bTestTapButton = (Button) findViewById(R.id.activity_main_bTestTapButton);
        bTestTapButton.setOnClickListener(this);
    }
    
    @Override
    public void onClick (View view)
    {
        Intent intent = null;
        
        switch (view.getId())
        {
            case R.id.ib_camera:
                intent = new Intent(this, ActivityTestCamera.class);
                break;
            
            case R.id.activity_main_bTestAlgorithm:
                intent = new Intent(this, ActivityTestAlgorithm.class);
                break;
            
            case R.id.activity_main_bTestImu:
                intent = new Intent(this, ActivityTestImu.class);
                break;
            
            case R.id.activity_main_bTestTapButton:
                intent = new Intent(this, ActivityButtonTap.class);
                break;
        }

        if(intent != null)
        {   startActivity(intent);}
    }
    
}
