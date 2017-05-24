package uu.mbi.mmob.alternative_buttons.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
import android.util.Log;

import java.nio.ByteBuffer;

/**
 * Created by Jasper on 23-May-17.
 */

public class UtilityColor {

    public static int[] calculateAvgColors(Image image) {

        Bitmap bitmap = convertImageToBitmap(image);

        int redColors = 0;
        int greenColors = 0;
        int blueColors = 0;
        int pixelCount = 0;

        for (int y = 0; y < bitmap.getHeight(); y++)
        {
            for (int x = 0; x < bitmap.getWidth(); x++)
            {
                int c = bitmap.getPixel(x, y);
                pixelCount++;
                redColors += Color.red(c);
                greenColors += Color.green(c);
                blueColors += Color.blue(c);
            }
        }
        // calculate average of bitmap r,g,b values
        int red = (redColors/pixelCount);
        int green = (greenColors/pixelCount);
        int blue = (blueColors/pixelCount);

        return new int[]{red, green, blue};

        /*Log.d("COLORS", "Red: " + red);
        Log.d("COLORS", "Green: " + green);
        Log.d("COLORS", "Blue: " + blue);*/
    }

    private static Bitmap convertImageToBitmap(Image image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.capacity()];
        buffer.get(bytes);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
    }
}
