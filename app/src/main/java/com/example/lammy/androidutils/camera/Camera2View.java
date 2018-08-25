package com.example.lammy.androidutils.camera;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.Face;
import android.hardware.camera2.params.MeteringRectangle;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.support.v4.app.ActivityCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.WindowManager;


import com.example.lammy.androidutils.log.LogUtil;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Camera2View extends TextureView implements TextureView.SurfaceTextureListener{

    private  static  String TAG = "lammy-camera2:";
    public Camera2View(Context context) {
        super(context);
        LogUtil.e("Camera2View...............");
        init();
    }

    public Camera2View(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Camera2View(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        LogUtil.e("init...............");
        getCameraIdList();
        setSurfaceTextureListener(this);
    }

    private Surface mSurface;


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        LogUtil.e("onSurfaceTextureAvailable...............");
        openCamera();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }



    private Size mPreViewSize;
    private ImageReader mImageReader;
    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mPreViewBuilder;
    private CameraDevice.StateCallback cameraOpenCallBack = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            Log.d(TAG, "相机已经打开");
            try {

                mPreViewSize =chooseBestPreviewSize(mCameraCharacteristics);// map.getOutputSizes(SurfaceTexture.class)[0];
//                setAspectRatio(
//                        mPreViewSize.getHeight(), mPreViewSize.getWidth());
                mImageReader = ImageReader.newInstance(mPreViewSize.getWidth(), mPreViewSize.getHeight(),
                        ImageFormat.YUV_420_888, 2);
                mImageReader.setOnImageAvailableListener(onImageAvailableListener, null);

                mCameraDevice = camera;
                mPreViewBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                SurfaceTexture texture = getSurfaceTexture();//new SurfaceTexture(2);//
                texture.setDefaultBufferSize(mPreViewSize.getWidth(), mPreViewSize.getHeight());
                mSurface = new Surface(getSurfaceTexture());
                mPreViewBuilder.addTarget(mSurface);
                camera.createCaptureSession(Arrays.asList(mSurface, mImageReader.getSurface
                        ()), mSessionStateCallBack, null);
            } catch (CameraAccessException e) {
                Log.d(TAG, "相机创建session失败");
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(CameraDevice camera) {

        }

        @Override
        public void onError(CameraDevice camera, int error) {

        }
    };

    private CameraCaptureSession mCameraSession;
    private CameraCaptureSession.StateCallback mSessionStateCallBack = new CameraCaptureSession.StateCallback() {

        @Override
        public void onConfigured(CameraCaptureSession session) {
            Log.e(TAG, "onConfigured......");
            mCameraSession = session;
            mPreViewBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            startPreView();
            cameraLock = false;
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {

        }
    };

    private void startPreView(){
        if(mCameraSession == null||mCameraDevice == null||mImageReader == null) {
            openCamera();
        }

        try {
            mPreViewBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
            mPreViewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_USE_SCENE_MODE);
            mPreViewBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CaptureRequest.CONTROL_SCENE_MODE_FACE_PRIORITY);
            mPreViewBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, CaptureRequest.STATISTICS_FACE_DETECT_MODE_SIMPLE);
            mPreViewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
            mPreViewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);




            mCameraSession.setRepeatingRequest(mPreViewBuilder.build(), captureCallback, null);
        }catch (CameraAccessException e) {
            e.printStackTrace();
        }


    }


    private  int i=0;
    private CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
//            process(result);
             i++;
            LogUtil.e("onCaptureCompleted...............   "+ i);
            long t1 = System.currentTimeMillis();
            Bitmap bitmap = getBitmap();
            long t2 = System.currentTimeMillis();
//            LogUtil.e("onCaptureCompleted...............   " + (t2-t1));
        }

        @Override
        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
        }

//        private void process(CaptureResult result) {
//            Face[] camera_faces = result.get(CaptureResult.STATISTICS_FACES);
//            int curFocusState = focusState;
//            if (camera_faces != null) {
//                if (camera_faces.length > 0) {
//                    focusState = FOCUS_FACE_STATE;
//                    faceRect = new Rect(camera_faces[0].getBounds().left, camera_faces[0].getBounds().top, camera_faces[0].getBounds().right, camera_faces[0].getBounds().bottom);
//                    System.out.println("lammy  faces" + camera_faces.length );
//                    startPreview(new MeteringRectangle(faceRect, MeteringRectangle.METERING_WEIGHT_MAX));
//                }else{
////                    faceRect=null;
////                    if(curFocusState == FOCUS_FACE_STATE){
////                        rePreView();
////                    }
//                }
//            }else{
////                faceRect=null;
////                if(curFocusState == FOCUS_FACE_STATE){
////                    rePreView();
////                }
//            }
//            // }
//        }
    };


    private CameraCharacteristics mCameraCharacteristics;
    private boolean cameraLock;
    public void openCamera(){

        CameraManager manager = (CameraManager) getContext().getSystemService(Context
                .CAMERA_SERVICE);

        try {
            mCameraCharacteristics = manager.getCameraCharacteristics(mCameraID);
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                LogUtil.e("请打开应用相机权限！");
                return;
            }

            Log.e(TAG, "open camera.....");
            if(cameraLock == false) {
                manager.openCamera(mCameraID, cameraOpenCallBack, null);
                cameraLock = true;
            }

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void closeCamera() {
        try {

            if (null != mCameraSession) {
                mCameraSession.close();
                mCameraSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mImageReader) {
                mImageReader.close();
                mImageReader = null;
            }
        } catch (Exception e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
//            mCameraOpenCloseLock.release();
        }
    }


    private ImageReader.OnImageAvailableListener onImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Log.d(TAG, "onImageAvailable .............");
            Log.e(TAG, "onImageAvailable .............");
            try {

                Image img = reader.acquireLatestImage();
                ByteBuffer buffer = img.getPlanes()[0].getBuffer();
                byte[] buff = new byte[buffer.remaining()];
                buffer.get(buff);
                final Bitmap bitmap = BitmapFactory.decodeByteArray(buff, 0, buff.length);




//                int angle = 0;
//                if(mCameraDevice.getId().equals( "0")) {
//                    angle = 90;
//                }else if(mCameraDevice.getId().equals( "1")) {
//                    angle = -90;
//                }
//                Bitmap bitmap2 = BitmapUtil.rotateBitmap(bitmap , angle);
//                bitmap.recycle();
//                LogUtil.e("cccc " , "time = " + (System.currentTimeMillis() - t1));
//                if (takeCaptureCallback != null) {
//                    takeCaptureCallback.takeCaptureSuccess(bitmap2);
//                }
//

            } catch (Exception e) {

            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
        }
    };


    private String mCameraID;
    private String[] mCameraIDList;
    private void getCameraIdList ()  {
        CameraManager manager = (CameraManager) getContext().getSystemService(Context
                .CAMERA_SERVICE);
        try {
            mCameraIDList  = manager.getCameraIdList();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        mCameraID = mCameraIDList[0];
    }

    private static final int MAX_PREVIEW_WIDTH = 1080;
    private static final int MAX_PREVIEW_HEIGHT = 1440;
    private Size chooseBestPreviewSize(CameraCharacteristics characteristics) {
        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (map == null) {
            // LogUtil.e(TAG, "can't get data from CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP");
            return null;
        }

        Size[] sizes = map.getOutputSizes(ImageFormat.YUV_420_888);
        if (null == sizes || 0 == sizes.length) {
            // LogUtil.e(TAG, "no output size for NV21");
            return null;
        }

        Size choosedSize = null;
        int diff = Integer.MAX_VALUE;

        for (int i = 0; i < sizes.length; ++i) {
            Size it = sizes[i];
            int width = it.getWidth();
            int height = it.getHeight();
            int rotateDegree = getRotateDegree(mCameraCharacteristics);
            if (rotateDegree == 90 || rotateDegree == 270) {
                height = it.getWidth();
                width = it.getHeight();
            }

            //  LogUtil.i(TAG, "supportPreview, width: %d, height: %d", width, height);
            if (width * height <= MAX_PREVIEW_HEIGHT * MAX_PREVIEW_WIDTH) {
                int newDiff = diff(height, width, MAX_PREVIEW_HEIGHT, MAX_PREVIEW_WIDTH);
                //     LogUtil.d(TAG, "diff: " + newDiff);
                if (null == choosedSize || newDiff < diff) {
                    choosedSize = new Size(it.getWidth(), it.getHeight());
                    diff = newDiff;
                }
            }
        }

        if (null == choosedSize) {
            List<Size> sizeLst = Arrays.asList(sizes);
            Collections.sort(sizeLst, new Comparator<Size>() {
                @Override
                public int compare(Size lhs, Size rhs) {
                    return lhs.getWidth() * lhs.getHeight() - rhs.getWidth() * rhs.getHeight();
                }
            });

            Size it = sizeLst.get(sizeLst.size() / 2);
            choosedSize = new Size(it.getWidth(), it.getHeight());
        }

        return choosedSize;
    }

    private int getRotateDegree(CameraCharacteristics characteristics) {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        int displayRotation = wm.getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (displayRotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int senseOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        return   (senseOrientation - degrees + 360) % 360;
    }
    double COEFFICIENT = 1000.0d;
    private int diff(double realH, double realW, double expH, double expW) {
        double rateDiff = Math.abs(COEFFICIENT * (realH / realW - expH / expW));
        return (int) (rateDiff + Math.abs(realH - expH) + Math.abs(realW - expW));
    }
}
