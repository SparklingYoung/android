package com.dip.penguin.lowpoly.utils;

import android.app.ActivityManager;
import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import com.dip.penguin.lowpoly.constant.Constants;

import java.util.List;

/**
 * Created by penguin on 11/25/15.
 */

public class Utils {

    //bitmap转换为mat形式
    public static Mat changeBitmap2Mat(Bitmap bitmap) {
        Mat matRGB = new Mat();

        //将图片从bitmap格式转换为mat格式
        org.opencv.android.Utils.bitmapToMat(bitmap, matRGB);

        return matRGB;
    }


    //图像灰度化
    public static Mat grayScale(Mat mat) {
        Mat matGray = new Mat();

        //得到mat格式的灰度图
        Imgproc.cvtColor(mat, matGray, Imgproc.COLOR_RGB2GRAY);

        return matGray;
    }


    //canny边缘检测
    public static Mat edgeDetect(Mat mat) {
        Mat matEdge = new Mat();

        //边缘检测得到边缘图
        Imgproc.Canny(mat, matEdge, Constants.threshold, Constants.threshold * 3);


        return matEdge;
    }


    //将mat转换为bitmap格式
    public static Bitmap mat2Bitmap(Mat mat) {
        Bitmap bitmap;
        bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.RGB_565);
        org.opencv.android.Utils.matToBitmap(mat, bitmap);
        return bitmap;
    }

    //将byte转换为unsigned byte数据类型
    public static int getUnsignedByte(byte data){
        return data&0x0FF;
    }


}