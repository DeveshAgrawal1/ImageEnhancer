package com.example.hp.test;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

/*
    // Used for logging success or failure messages
    private static final String TAG = "OCVSample::Activity";

    // Loads camera view of OpenCV for us to use. This lets us see using OpenCV
    private CameraBridgeViewBase mOpenCvCameraView;

    // Used in Camera selection from menu (when implemented)
    private boolean              mIsJavaCamera = true;
    private MenuItem             mItemSwitchCamera = null;

    // These variables are used (at the moment) to fix camera orientation from 270degree to 0degree
    Mat mRgba;
    Mat mRgbaF;
    Mat mRgbaT;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        mOpenCvCameraView = (JavaCameraView) findViewById(R.id.show_camera_activity_java_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {

        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mRgbaF = new Mat(height, width, CvType.CV_8UC4);
        mRgbaT = new Mat(width, width, CvType.CV_8UC4);
    }

    public void onCameraViewStopped() {
        mRgba.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        // TODO Auto-generated method stub
        mRgba = inputFrame.rgba();
        // Rotate mRgba 90 degrees
        Core.transpose(mRgba, mRgbaT);
        Imgproc.resize(mRgbaT, mRgbaF, mRgbaF.size(), 0,0, 0);
        Core.flip(mRgbaF, mRgba, 1 );

        return mRgba; // This function must return
    }


    */
    private Uri mImageCaptureUri;
    private static final int PICK_FROM_CAMERA = 1;
    private static final int PICK_FROM_FILE = 2;
    private ImageView mImageView;
    public Bitmap bitmap 	= null;
    private String finalPath ="";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.cam);

        final String [] items			= new String [] {"From Camera", "From SD Card"};
        ArrayAdapter<String> adapter	= new ArrayAdapter<String> (this, android.R.layout.select_dialog_item,items);
        AlertDialog.Builder builder		= new AlertDialog.Builder(this);
        Button mask = (Button) findViewById(R.id.btn_mask);
        mask.setOnClickListener(new View.OnClickListener() {
            @Override
                public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, Masking.class);
                intent.putExtra("Bitmap",finalPath);
                startActivity(intent);
            }

        });

        Button hist = (Button) findViewById(R.id.btn_hist);
        hist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, Histogram.class);
                intent.putExtra("Bitmap",finalPath);
                startActivity(intent);
            }

        });

        builder.setTitle("Select Image");
        builder.setAdapter( adapter, new DialogInterface.OnClickListener() {
            public void onClick( DialogInterface dialog, int item ) {
                if (item == 0) {
                    Intent intent 	 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File file		 = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),
                            "tmp_avatar_" + String.valueOf(System.currentTimeMillis()) + ".jpg");
                    mImageCaptureUri = Uri.fromFile(file);

                    try {
                        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
                        intent.putExtra("return-data", true);

                        startActivityForResult(intent, PICK_FROM_CAMERA);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    dialog.cancel();
                } else {
                    Intent intent = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                    intent.setType("image/*");


                    startActivityForResult(Intent.createChooser(intent, "Complete action using"), PICK_FROM_FILE);
                }
            }
        } );

        final AlertDialog dialog = builder.create();

        mImageView = (ImageView) findViewById(R.id.iv_pic);

        ((Button) findViewById(R.id.btn_choose)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;


        String path		= "";

        if (requestCode == PICK_FROM_FILE) {
            mImageCaptureUri = data.getData();
            path = getRealPathFromURI(mImageCaptureUri); //from Gallery

            if (path == null)
                path = mImageCaptureUri.getPath(); //from File Manager

            if (path != null)
                bitmap 	= BitmapFactory.decodeFile(path);
        } else {
            path	= mImageCaptureUri.getPath();
            Log.e("ADASDASD",path);
            bitmap  = BitmapFactory.decodeFile(path);
            mImageView.setImageBitmap(bitmap);
        }
        finalPath = path;
        mImageView.setImageBitmap(bitmap);
    }

    public String getRealPathFromURI(Uri contentUri) {
        String [] proj 		= {MediaStore.Images.Media.DATA};
        Cursor cursor 		= getContentResolver().query( contentUri, proj, null, null,null);

        if (cursor == null) return null;

        int column_index 	= cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();

        return cursor.getString(column_index);
    }
}
