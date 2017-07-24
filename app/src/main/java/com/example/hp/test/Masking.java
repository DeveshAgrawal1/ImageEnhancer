package com.example.hp.test;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Masking extends AppCompatActivity {

    Bitmap myBitmap = null;
    private String OPENCVLOADED = "Loaded";
    private ImageView myImage;
    Mat mat;
    Mat blurred;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String imagePath = this.getIntent().getStringExtra("Bitmap");
        File imgFile = new  File(imagePath);
        if(imgFile.exists()){
            myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            setContentView(R.layout.activity_masking);
            ImageView myImage = (ImageView) findViewById(R.id.result_image);
            myImage.setImageBitmap(myBitmap);
        }

        Button save = (Button) findViewById(R.id.btn_save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyymmsshhmmss");
                String date = simpleDateFormat.format(new Date());

                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),
                        "unsharp"+date+".jpg");
                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    myBitmap.compress(Bitmap.CompressFormat.JPEG,100,fileOutputStream);
                    Toast.makeText(Masking.this, "Image saved!", Toast.LENGTH_SHORT).show();
                    fileOutputStream.flush();
                    fileOutputStream.close();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS ) {
                // now we can call opencv code !
                Log.i(OPENCVLOADED, "OpenCV loaded successfully");
                 mat = new Mat(myBitmap.getWidth(), myBitmap.getHeight(),CvType.CV_8U, new Scalar(4));
                Bitmap myBitmap32 = myBitmap.copy(Bitmap.Config.ARGB_8888, true);
                Utils.bitmapToMat(myBitmap32,mat);
                 blurred = new Mat(myBitmap.getWidth(),myBitmap.getHeight(), CvType.CV_8U, new Scalar(4));
                Utils.bitmapToMat(myBitmap32,blurred);
                performMasking();

            } else {
                super.onManagerConnected(status);
            }
        }
    };

    @Override
    public void onResume() {;
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(OPENCVLOADED, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
        } else {
            Log.d(OPENCVLOADED, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void performMasking() {
        org.opencv.core.Size s = new Size(3,3);
        Imgproc.GaussianBlur(mat, blurred, s, 2);
        Core.addWeighted(mat, 2.5, blurred, -0.8, 0, mat);
        Utils.matToBitmap(mat, myBitmap);
        //myImage.setImageBitmap(myBitmap);
    }

}
