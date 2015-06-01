package com.example.chaka.weekendassignment2.util;

import android.util.Log;

import com.example.chaka.weekendassignment2.conf.Constants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Chaka on 01/06/2015.
 */
public class Util {

    public static Boolean checkOutCode(String outcode){

        Pattern pattern = Pattern.compile(Constants.regex);
        Matcher matcher = pattern.matcher(outcode);
        Log.d("Matcher", String.valueOf(matcher.matches()));
        return matcher.matches();
    }


}
