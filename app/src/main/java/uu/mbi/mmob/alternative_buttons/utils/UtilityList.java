package uu.mbi.mmob.alternative_buttons.utils;

/**
 * Created by Teun on 19-5-2017.
 */

public class UtilityList
{
    public static <T> T[] shift(T[] input, T newValue)
    {
        for(int i = input.length - 1 ; i >= 1 ; i--)
        {   input[i -1] = input[i];}
        
        input[input.length - 1] = newValue;
        
        return input;
    }
}
