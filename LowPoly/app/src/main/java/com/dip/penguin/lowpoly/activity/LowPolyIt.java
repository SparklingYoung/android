package com.dip.penguin.lowpoly.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import com.dip.penguin.lowpoly.R;
import com.dip.penguin.lowpoly.constant.Constants;
import com.dip.penguin.lowpoly.delaunay.Pnt;
import com.dip.penguin.lowpoly.delaunay.Triangle;
import com.dip.penguin.lowpoly.delaunay.Triangulation;
import com.dip.penguin.lowpoly.utils.Utils;

import org.opencv.core.Mat;

public class LowPolyIt extends Activity implements View.OnClickListener{
    private Bitmap bmpSrc;
    private Uri uriSrc;
    private ImageView imgViewSrc;
    private int rows, cols;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_low_poly_it);

        initView();
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
            bmpSrc = MediaStore.Images.Media.getBitmap(getContentResolver(),uriSrc);
        }catch (Exception e){
            e.printStackTrace();
        }
        //将bitmap图片显示在imageView中
        if (bmpSrc != null){
            imgViewSrc.setImageBitmap(bmpSrc);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.img_Image:
                lowPolyIt();
                Triangle tri =
                        new Triangle(new Pnt(-10,10), new Pnt(10,10), new Pnt(0,-10));
                System.out.println("Triangle created: " + tri);
                Triangulation dt = new Triangulation(tri);
                System.out.println("DelaunayTriangulation created: " + dt);
                dt.delaunayPlace(new Pnt(0,0));
                dt.delaunayPlace(new Pnt(1,0));
                dt.delaunayPlace(new Pnt(0,1));
                System.out.println("After adding 3 points, we have a " + dt);
                Triangle.moreInfo = true;
                System.out.println("Triangles: " + dt.triGraph.nodeSet());
                break;
            default:
                break;
        }
    }

    //将图片转换成lowPoly风格
    private void lowPolyIt(){
        Bitmap bmpTri;

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
        Mat matPoint = new Mat();
        matPoint = Utils.selectPoint(matEdge);
        //三角化
        Mat matTri = new Mat();

        bmpTri = Utils.mat2Bitmap(matPoint);
        imgViewSrc.setImageBitmap(bmpTri);
    }


}
