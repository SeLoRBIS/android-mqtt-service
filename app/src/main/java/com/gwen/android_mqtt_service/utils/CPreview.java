package com.gwen.android_mqtt_service.utils;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

@SuppressWarnings("deprecation")
public class CPreview extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = CPreview.class.getName();
    private SurfaceHolder mSurfaceHolder;
    private Camera mCamera;
    private Camera.Parameters parameters;


    public CPreview(Context context, Camera camera) {
        super(context);

        this.mCamera = camera;
        this.mSurfaceHolder = this.getHolder();
        this.mSurfaceHolder.addCallback(this);
        this.mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            mCamera.setParameters(getParameters());

            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.e(TAG, "error surfaceCreated");
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mCamera.stopPreview();
        mCamera.release();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format,
                               int width, int height) {
        try {
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.startPreview();
        } catch (Exception e) {
            Log.e(TAG, "error surfaceChanged");
        }
    }

    /**
     * Set Camera Parameters
     * @return
     */
    private Camera.Parameters getParameters(){

        // Sizes
        parameters = mCamera.getParameters();

        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
        Camera.Size previewSize = previewSizes.get(0);
        parameters.setPreviewSize(previewSize.width, previewSize.height);

        List<Camera.Size> pictureSizes = parameters.getSupportedPictureSizes();
        Camera.Size pictureSize = pictureSizes.get(0);
        parameters.setPictureSize(pictureSize.width, pictureSize.height);

        // AutoFocus
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);

        return parameters;
    }

}