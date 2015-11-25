package com.dip.penguin.lowpoly.utils;

import android.graphics.Bitmap;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import com.dip.penguin.lowpoly.constant.Constants;

/**
 * Created by penguin on 11/25/15.
 */

public class Utils {

    //bitmap转换为mat形式
    public static Mat changeBitmap2Mat(Bitmap bitmap){
        Mat matRGB = new Mat();

        //将图片从bitmap格式转换为mat格式
        org.opencv.android.Utils.bitmapToMat(bitmap, matRGB);

        return matRGB;
    }



    //图像灰度化
    public static Mat grayScale(Mat mat){
        Mat matGray = new Mat();

        //得到mat格式的灰度图
        Imgproc.cvtColor(mat, matGray, Imgproc.COLOR_RGB2GRAY);

        return matGray;
    }




    //canny边缘检测
    public static Mat edgeDetect(Mat mat){
        Mat matEdge = new Mat();

        //边缘检测得到边缘图
        Imgproc.Canny(mat, matEdge, Constants.threshold, Constants.threshold * 3);


        return matEdge;
    }




    //选取点
    public static Mat selectPoint(Mat mat){
        Bitmap bmpPoint;
        int rows = mat.rows();
        int cols = mat.cols();
        Mat matPoint = new Mat(rows,cols, CvType.CV_8U,new Scalar(0));
        int randomRow, randomCol;//随机取像素点的行和列
        int pickedEdge, pickedBg;//记录已取到的边缘点的个数和非边缘点个数
        pickedEdge = pickedBg = 0;
        byte[] bGet = new byte[1];
        byte[] bPut = new byte[1];


        while (true){
            //取100个非边缘点和900个边缘点
            if (pickedBg >= Constants.NUM_BG && pickedEdge >= Constants.NUM_EDGE) break;

            //在mat中随机取一个点
            randomRow = (int)(Math.random()*rows);
            randomCol = (int)(Math.random()*cols);
            mat.get(randomRow,randomCol,bGet);

            //取到的点为背景点（非边缘点）
            if (bGet[0] == 0 && pickedBg < Constants.NUM_BG){
                pickedBg ++;
                bPut[0] = 1;
                mat.put(randomRow,randomCol,bPut);
                bPut[0] = -1;
                matPoint.put(randomRow,randomCol,bPut);
            }

            //取到的点为边缘点
            if (bGet[0] == -1 && pickedEdge < Constants.NUM_EDGE){
                pickedEdge ++;
                bPut[0] = -2;
                mat.put(randomRow,randomCol,bPut);
                bPut[0] = -1;
                matPoint.put(randomRow,randomCol,bPut);
            }
        }

        return matPoint;
    }

    //三角化
    public static void triangulation(){

    }

    //着色
    public static void shade(){

    }

    public static Bitmap mat2Bitmap(Mat mat){
        Bitmap bitmap;
        bitmap = Bitmap.createBitmap(mat.cols(),mat.rows(), Bitmap.Config.RGB_565);
        org.opencv.android.Utils.matToBitmap(mat, bitmap);
        return bitmap;
    }
}
