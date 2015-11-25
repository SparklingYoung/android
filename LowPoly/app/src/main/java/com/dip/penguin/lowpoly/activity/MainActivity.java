package com.dip.penguin.lowpoly.activity;


import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.dip.penguin.lowpoly.R;
import com.dip.penguin.lowpoly.constant.Constants;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends Activity implements View.OnClickListener{
    private String TAG = "OPENCV_SUCCESS";
    private static String TAG_CAMERA = "LowPolyCamera";
    private static final int CODE_CAMERA = 100;
    private static final int CODE_GALLERY = 101;
    private Uri uriCamera;
    private Uri uriGallery;
    private ImageView btnGallery = null;
    private ImageView btnCamera = null;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
    }

    private void initUI(){
        btnGallery = (ImageView) findViewById(R.id.btn_gallery);
        btnCamera = (ImageView) findViewById(R.id.btn_camera);

        btnGallery.setOnClickListener(this);
        btnCamera.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11,getApplicationContext(),mLoaderCallback);
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case BaseLoaderCallback.SUCCESS:
                    Log.i(TAG,"openCV成功加载");
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_gallery:
                getImgFromGallery();
                break;
            case R.id.btn_camera:
                getImgFromCamera();
                break;
            default:
                break;
        }
    }

    /*
    从相册获取图片
     */
    private void getImgFromGallery(){
        Intent intent = new Intent();
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,CODE_GALLERY);
    }




    /*
    从相机获取图片
     */
    private void getImgFromCamera(){
        //新建拍照intent
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //创建存储图片路径
        uriCamera = getOutputImageFileUri();
        if (uriCamera == null) {
            Log.d(TAG_CAMERA,"failed to take a picture");
            return ;
        }
        //设置图片存储路径
        intent.putExtra(MediaStore.EXTRA_OUTPUT,uriCamera);
        startActivityForResult(intent, CODE_CAMERA);
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            switch (requestCode){
                case CODE_CAMERA:
                    startLowPolyItActivity(uriCamera);
                    break;
                case CODE_GALLERY:
                    startLowPolyItActivity(data.getData());
                    break;
                default:
                    break;
            }
        }
    }




    //获取照片文件的存储路径
    private  static Uri getOutputImageFileUri(){

        //在指定的路径创建MyLowPolyCamera文件
        File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                TAG_CAMERA);

        //如果该存储路径不存在则创建该路径
        if (!imageStorageDir.exists()){
            if (!imageStorageDir.mkdirs()){
                Log.d(TAG_CAMERA,"failed to create directory");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File imageFile = new File(imageStorageDir.getPath() + File.separator + timeStamp + ".jpg");
        return Uri.fromFile(imageFile);
    }



    //打开LowPolyIt Activity
    private void startLowPolyItActivity(Uri uri){
        Intent intent = new Intent(this,LowPolyIt.class);
        intent.putExtra(Constants.PATH_IMAGE,uri.toString());
        startActivity(intent);
    }
}
