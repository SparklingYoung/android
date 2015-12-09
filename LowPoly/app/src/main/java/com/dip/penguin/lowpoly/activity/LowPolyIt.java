package com.dip.penguin.lowpoly.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.dip.penguin.lowpoly.R;
import com.dip.penguin.lowpoly.constant.Constants;
import com.dip.penguin.lowpoly.utils.Utils;
import com.dip.penguin.lowpoly.view.DialogSave;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat6;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Subdiv2D;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class LowPolyIt extends Activity implements View.OnClickListener,DialogSave.OnDialogSaveListener{
    private Bitmap bmpSrc, bmpDst;
    private Uri uriSrc;
    private ImageView imgViewSrc;
    private boolean mark = false;//标记图片是否已经进行lowpoly处理


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_low_poly_it);

        initView();
    }


    @Override
    protected void onResume() {
        super.onResume();
        //OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, getApplicationContext(), mLoaderCallback);
        if (!OpenCVLoader.initDebug()) {
            //Log.d("if", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            //Log.d("else", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case BaseLoaderCallback.SUCCESS:
                    Log.i(Constants.OPENCV_LOAD_TAG,"openCV成功加载");
                    break;
                default:
                    break;
            }
        }
    };


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.img_Image:
                if (!mark){
                    lowPolyIt();
                    mark = true;
                }else{
                    save();
                }
                break;
            default:
                break;
        }
    }

    private void initView(){
        imgViewSrc = (ImageView) findViewById(R.id.img_Image);
        imgViewSrc.setOnClickListener(this);

        showInitImage();
    }

    //初始化图片
    private void showInitImage(){
        //获取图片的Uri
        uriSrc = Uri.parse(getIntent().getStringExtra(Constants.PATH_IMAGE));
        //根据Uri获取图片bitmap
        try {
//            bmpSrc = MediaStore.Images.Media.getBitmap(getContentResolver(),uriSrc);
            //压缩图片
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            String path = Utils.getImageAbsolutePath(this,uriSrc);
            if (path == null){
                Toast.makeText(this,"can not load this image",Toast.LENGTH_LONG).show();
                finish();
            }
            BitmapFactory.decodeFile(path,options);
            int height = options.outHeight;
            int width = options.outWidth;
            int screenHeight, screenWidth;
            int sample = 1;

            screenHeight = Utils.getScreenHeight(getApplicationContext());
            screenWidth = Utils.getScreenWidth(getApplicationContext());

            if (height > 2048 || width > 2048){
                while (height/sample > screenHeight || width/sample > screenWidth) sample *= 2;
            }

            options.inSampleSize = sample;
            options.inJustDecodeBounds = false;
            bmpSrc = BitmapFactory.decodeFile(path,options);

        }catch (Exception e){
            e.printStackTrace();
        }
        //将bitmap图片显示在imageView中
        if (bmpSrc != null){
            //Log.e("msg","width: " + bmpSrc.getWidth() + " height: " + bmpSrc.getHeight());
            imgViewSrc.setImageBitmap(bmpSrc);
        }
    }




    //将图片转换成lowPoly风格
    private void lowPolyIt(){
        //将图像从bitmap格式转换为mat格式
        Mat matRGB = new Mat();
        matRGB = Utils.changeBitmap2Mat(bmpSrc);
        //将彩色图转换为灰度图
        Mat matGray = new Mat();
        matGray = Utils.grayScale(matRGB);
        //使用canny算子进行边缘检测
        Mat matEdge = new Mat();
        matEdge = Utils.edgeDetect(matGray);
        //选取点
        Mat matDst = new Mat();
        matDst = deluany(matRGB, matEdge);

        bmpDst = Utils.mat2Bitmap(matDst);
        imgViewSrc.setImageBitmap(bmpDst);
    }


    //德洛内算法
    private Mat deluany(Mat matRGB, Mat matEdge) {
        int rows = matRGB.rows();
        int cols = matRGB.cols();
        int randomRow, randomCol;//随机取像素点的行和列
        int pickedEdge, pickedBg;//记录已取到的边缘点的个数和非边缘点个数
        pickedEdge = pickedBg = 0;
        byte[] bGet = new byte[1];
        byte[] bPut = new byte[1];


        Rect rect = new Rect(0, 0, cols, rows);
        Subdiv2D subdiv = new Subdiv2D(rect);
        Mat matDst = new Mat(rect.size(), CvType.CV_8UC3, new Scalar(0));
        MatOfFloat6 triList = new MatOfFloat6();
        float[] fGet = new float[6];
        Point pt;
        Point[] pts = new Point[3];
        MatOfPoint matOfPts;
        byte[] bGets = new byte[4];
        Point centreOfGravity;
        int B, G, R, A;
        Scalar triColor;

        //添加图像边界上的点
        for(int i = 0;i <= Constants.NUM_POINT_EDGE;i ++){
            int x, y;
            x = i*cols/Constants.NUM_POINT_EDGE - 1 >= 0?i*cols/Constants.NUM_POINT_EDGE - 1:0;
            y = i*rows/Constants.NUM_POINT_EDGE - 1 >= 0?i*rows/Constants.NUM_POINT_EDGE - 1:0;
            pt = new Point(0,y);
            subdiv.insert(pt);
            pt = new Point(cols - 1,y);
            subdiv.insert(pt);
            pt = new Point(x,0);
            subdiv.insert(pt);
            pt = new Point(x,rows - 1);
            subdiv.insert(pt);
        }

        while (true) {
            //取100个非边缘点和900个边缘点
            if (pickedBg >= Constants.NUM_BG && pickedEdge >= Constants.NUM_EDGE) break;

            //在mat中随机取一个点
            randomRow = (int) (Math.random() * rows);
            randomCol = (int) (Math.random() * cols);
            matEdge.get(randomRow, randomCol, bGet);

            //取到的点为背景点（非边缘点）
            if (bGet[0] == 0 && pickedBg < Constants.NUM_BG) {
                pickedBg++;
                bPut[0] = 1;
                matEdge.put(randomRow, randomCol, bPut);
                bPut[0] = -1;
                pt = new Point(randomCol, randomRow);
                subdiv.insert(pt);
            }

            //取到的点为边缘点
            if (bGet[0] == -1 && pickedEdge < Constants.NUM_EDGE) {
                pickedEdge++;
                bPut[0] = -2;
                matEdge.put(randomRow, randomCol, bPut);
                bPut[0] = -1;
                pt = new Point(randomCol, randomRow);
                subdiv.insert(pt);
            }
        }


        //得到三角形列表
        subdiv.getTriangleList(triList);


        //绘制三角形
        for (int i = 0; i < triList.size().height; i++) {
            triList.get(i, 0, fGet);
            pts[0] = new Point(fGet[0], fGet[1]);
            pts[1] = new Point(fGet[2], fGet[3]);
            pts[2] = new Point(fGet[4], fGet[5]);

            //选取重心颜色，若重心坐标不在mat区域内，将其修改为区域内值
            centreOfGravity = new Point((fGet[0] + fGet[2] + fGet[4]) / 3, (fGet[1] + fGet[3] + fGet[5]) / 3);
            int x, y;
            x = (int)centreOfGravity.x;
            y = (int)centreOfGravity.y;
            if (centreOfGravity.x < 0) x = 0;
            if (centreOfGravity.x >= cols) x = cols - 1;
            if (centreOfGravity.y < 0) y = 0;
            if (centreOfGravity.y >= rows) y = rows - 1;

            matRGB.get(y, x, bGets);

            B = Utils.getUnsignedByte(bGets[0]);
            G = Utils.getUnsignedByte(bGets[1]);
            R = Utils.getUnsignedByte(bGets[2]);
            A = Utils.getUnsignedByte(bGets[3]);
            triColor = new Scalar(B,G,R,A);

            matOfPts = new MatOfPoint(pts);
            Imgproc.fillConvexPoly(matDst, matOfPts, triColor);
        }

        return matDst;
    }


    //保存lowpoly图片
    private void save(){
        DialogSave dialogSave = new DialogSave();
        dialogSave.show(getFragmentManager(), "dialogSave");
    }


    //将lowpoly图片保存至指定目录
    @Override
    public void onDialogSave() {
        File imageStorageDir = Constants.FILE_DIR;

        //如果该存储路径不存在则创建该路径
        if (!imageStorageDir.exists()){
            if (!imageStorageDir.mkdirs()){
                Log.d(Constants.TAG_CAMERA,"failed to create directory");
                return ;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File imageFile = new File(imageStorageDir.getPath() + File.separator + timeStamp + ".jpg");
        try {
            imageFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(imageFile);
            bmpDst.compress(Bitmap.CompressFormat.JPEG,50,fos);
            fos.flush();
            fos.close();
        }catch (IOException e){
            e.printStackTrace();
        }

        Toast.makeText(LowPolyIt.this,"The image has saved into " + Constants.FILE_DIR,Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



    //获取内存
    private void displayBriefMemory(String tag)
    {
        final ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(info);

        Log.e(tag, "系统剩余内存:" + (info.availMem >> 10) + "k");
        Log.e(tag,"系统是否处于低内存运行："+info.lowMemory);
        Log.e(tag,"当系统剩余内存低于"+info.threshold+"时就看成低内存运行");
    }



}
