package com.dip.penguin.lowpoly.constant;

import android.os.Environment;

import java.io.File;

/**
 * Created by penguin on 11/8/15.
 */
public class Constants {
    public static String OPENCV_LOAD_TAG = "OPENCV_SUCCESS";
    public static String PATH_IMAGE = "ImagePath";
    public static double threshold = 50; //canny算子阈值
    public static int NUM_EDGE = 900;//选取的非边缘点数
    public static int NUM_BG = 100;//选取的边缘点数
    public static int NUM_POINT_EDGE = 20;//选取图片边界点数
    public static String TAG_CAMERA = "LowPolyCamera";
    public static File FILE_DIR = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            TAG_CAMERA);

}
