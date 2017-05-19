package uu.mbi.mmob.alternative_buttons.ui.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import uu.mbi.mmob.alternative_buttons.R;
import uu.mbi.mmob.alternative_buttons.buttons.ButtonBackTap;
import uu.mbi.mmob.alternative_buttons.buttons.ButtonBackTap.OnBackTapListener;

/**
 * Created by Teun on 19-5-2017.
 */

public class ActivityButtonTap extends AppCompatActivity implements OnBackTapListener
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

    /* * * * * * * * * * * * * * * * * * */
    /*                                   */
    /*             MEMBERS               */
    /*                                   */
    /* * * * * * * * * * * * * * * * * * */
    
    private TextView mTvTapCounter;
    private int mTapCounter;
    private ButtonBackTap mButtonBackTap;
    
    /* * * * * * * * * * * * * * * * * * */
    /*                                   */
    /*            CONSTRUCTION           */
    /*                                   */
    /* * * * * * * * * * * * * * * * * * */
    
    @Override
    protected void onCreate (@Nullable final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_button_back_tap);
        
        mTvTapCounter = (TextView) findViewById(R.id.activity_button_back_tap_tvTapCounter);
        mTapCounter = 0;
        
        mButtonBackTap = new ButtonBackTap(new WeakReference<Activity>(this));
        mButtonBackTap.setOnBackTapListener(new WeakReference<OnBackTapListener>(this));
    }
    
    @Override
    protected void onResume ()
    {
        super.onResume();

        if(mButtonBackTap != null)
        {   mButtonBackTap.startListening();}
    }
    
    @Override
    protected void onPause ()
    {
        super.onPause();
        
        if(mButtonBackTap != null)
        {   mButtonBackTap.stopListening();}
    }
    
    /* * * * * * * * * * * * * * * * * * */
    /*                                   */
    /*              EVENTS               */
    /*                                   */
    /* * * * * * * * * * * * * * * * * * */
    
    
    @Override
    public void onBackTap (final ButtonBackTap button)
    {
        mTapCounter += 1;
        mTvTapCounter.setText("Amount of taps: " + mTapCounter + ".");
    }
    
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
    
    /* * * * * * * * * * * * * * * * * * */
    /*                                   */
    /*              HELPERS              */
    /*                                   */
    /* * * * * * * * * * * * * * * * * * */
}
