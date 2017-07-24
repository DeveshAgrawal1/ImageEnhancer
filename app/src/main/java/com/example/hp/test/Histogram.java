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
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.opencv.core.Core.merge;
import static org.opencv.core.Core.split;

public class Histogram extends AppCompatActivity {

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
                    Toast.makeText(Histogram.this, "Image saved!", Toast.LENGTH_SHORT).show();
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
                performHistogramEqualization();

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

    public void performHistogramEqualization() {
        if(mat.channels()>3) {
            Mat ycrcb = new Mat(mat.rows(),mat.cols(),CvType.CV_8U, new Scalar(4));
            //Utils.bitmapToMat(myBitmap,ycrcb);
            Imgproc.cvtColor(mat,ycrcb,Imgproc.COLOR_BGR2YCrCb);
            List<Mat> channels = new ArrayList<Mat>(3);
            split(ycrcb,channels);

            Imgproc.equalizeHist(channels.get(0),channels.get(0));

            Mat result = new Mat(mat.rows(),mat.cols(),CvType.CV_8U, new Scalar(4));
            //Utils.bitmapToMat(myBitmap,result);
            merge(channels,result);
            Imgproc.cvtColor(ycrcb,result,Imgproc.COLOR_BGR2YCrCb);

            Utils.matToBitmap(result,myBitmap);

        }
    }

}
