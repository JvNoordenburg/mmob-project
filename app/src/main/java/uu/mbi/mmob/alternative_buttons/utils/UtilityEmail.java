package uu.mbi.mmob.alternative_buttons.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import java.io.File;

import static android.R.id.message;

/**
 * Created by Teun on 19-5-2017.
 */

public class UtilityEmail
{
    public static void mailFile(Activity activity, String[] addresses, File file)
    {
        Uri uri = Uri.fromFile(file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Measurements");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.putExtra(Intent.EXTRA_TEXT, message);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        
        activity.startActivityForResult(Intent.createChooser(intent, "Sending measurements.."), 1);
    }
}
