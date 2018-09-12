package org.opencv.samples.facedetect;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Scalar;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

public class FdActivity extends Activity implements CvCameraViewListener2 {

    private static final String    TAG                 = "OCVSample::Activity";
    private static final Scalar    FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    public static final int        DX       = 0;
    public static final int        DY     = 1;

    private MenuItem               mItemKernel9;
    private MenuItem               mItemKernel7;
    private MenuItem               mItemKernel5;
    private MenuItem               mItemKernel3;
    private MenuItem               mItemType;

    private Mat                    mRgba;
    private Mat                    mGray;

    private int                    mDerivativeType       = DX;
    private String[]               mDerivativeName;

    private int                  mKernelSize   = 3;

    private CameraBridgeViewBase   mOpenCvCameraView;

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
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

    public FdActivity() {
        mDerivativeName = new String[2];
        mDerivativeName[DX] = "Dx";
        mDerivativeName[DY] = "Dy";

        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.face_detect_surface_view);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.fd_activity_surface_view);
        mOpenCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
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
        mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
    }

    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();
        MatOfRect faces = new MatOfRect();

        if (mDerivativeType == DX) {

            Imgproc.Sobel(mGray,mGray,-1,1,0,mKernelSize,1.0,0,0);
        }
        else if (mDerivativeType == DY) {
            Imgproc.Sobel(mGray,mGray,-1,0,1,mKernelSize,1.0,0,0);
        }
        else {
            Log.e(TAG, "Derivative Type is not selected!");
        }
        Core.bitwise_not(mGray, mGray);
        return mGray;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemKernel3 = menu.add("Kernel=3");
        mItemKernel5 = menu.add("Kernel=5");
        mItemKernel7 = menu.add("Kernel=7");
        mItemKernel9 = menu.add("Kernel=9");
        mItemType   = menu.add(mDerivativeName[mDerivativeType]);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item == mItemKernel3)
            setKernelSize(3);
        else if (item == mItemKernel5)
            setKernelSize(5);
        else if (item == mItemKernel7)
            setKernelSize(7);
        else if (item == mItemKernel9)
            setKernelSize(9);
        else if (item == mItemType) {
            int tmpDerivativeType = (mDerivativeType + 1) % mDerivativeName.length;
            item.setTitle(mDerivativeName[tmpDerivativeType]);
            setDerivativeType(tmpDerivativeType);
        }
        return true;
    }

    private void setKernelSize(int faceSize) {
        mKernelSize = faceSize;
    }

    private void setDerivativeType(int type) {
        if (mDerivativeType != type) {
            mDerivativeType = type;

                    }
    }
}
