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
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.lammy.androidutils.Bitmap.BitmapUtil;
import com.example.lammy.androidutils.log.LogUtil;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by zhangpeng30 on 2017/11/30.
 */

public class CameraView extends AutoFitTextureView {

    private static String TAG = "lammy";
    private CaptureRequest.Builder mPreViewBuilder;
    private CameraCaptureSession mCameraSession;
    private CameraCharacteristics mCameraCharacteristics;
    private ImageReader mImageReader;
    private CameraDevice mCameraDevice;
    private Context mContext;
    private String savePath;
    private String mCameraID;
    private String []mCameraIDList;

    private boolean cameraLock = false;
    private static final int MAX_PREVIEW_WIDTH = 1080;
    private static final int MAX_PREVIEW_HEIGHT = 1440;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private FacingStateListener facingStateListener;
    private FocusStateListener focusStateListener;
    private FlashStateListener flashStateListener;
    private TakeCaptureCallback takeCaptureCallback;

    public static int FOCUS_FACE_STATE = 2;
    public static int FOCUS_AUTO_STATE = 0;
    public static int FOCUS_PRINT_STATE = 1;
    int focusState = FOCUS_AUTO_STATE;

    public static int FACE_FRONT = 1;
    public static int FACE_BACK = 0;
    public CameraView(Context context) {
        super(context);
        mContext = context;
        getCameraIdList();
        setSurfaceTextureListener( new mSurfaceTextureListener());
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        getCameraIdList();
        setSurfaceTextureListener( new mSurfaceTextureListener());
    }

    private Size mPreViewSize;
    private Rect maxZoomrect;
    private int maxRealRadio;
    private Rect picRect;

    public boolean isSurfaceAvailable = false;

   public static int FLASH_OFF = 0;
    public static  int FLASH_AUTO = 1;
    public static  int FLASH_ON = 2;


    class mSurfaceTextureListener implements TextureView.SurfaceTextureListener {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {

            isSurfaceAvailable = true;
            startPreview();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {

            closeCamera();
            isSurfaceAvailable = false;
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    }



   private Surface mSurface;
    private CameraDevice.StateCallback cameraOpenCallBack = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            Log.d(TAG, "相机已经打开");
            try {

                mPreViewSize =chooseBestPreviewSize(mCameraCharacteristics);// map.getOutputSizes(SurfaceTexture.class)[0];
                setAspectRatio(
                        mPreViewSize.getHeight(), mPreViewSize.getWidth());
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

    private CameraCaptureSession.StateCallback mSessionStateCallBack = new CameraCaptureSession.StateCallback() {

        @Override
        public void onConfigured(CameraCaptureSession session) {
                Log.d(TAG, "onConfigured......");
                mCameraSession = session;
                mPreViewBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            rePreView();
            cameraLock = false;
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {

        }
    };


    private Rect faceRect;
    public void startFocus(float x, float y) {
//        if (currentFacing == FACING_BACK) {
//            y = getHeight() - y;
//        }
        if (mCameraID.equals(mCameraIDList[0])) {
            y = getHeight() - y;
        }

        Rect sensorArraySize = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);

        int fy = (int) (((getWidth() - x) * 1f / getWidth()) * sensorArraySize.height() - 50);
        fy = Math.max(fy, 0);
        int fx = (int) (((getHeight() - y) * 1f / getHeight()) * sensorArraySize.width() - 50);
        fx = Math.max(fx, 0);
        Rect rect = new Rect(fx, fy, fx + 100, fy + 100);
        faceRect = rect;
        focusState = FOCUS_PRINT_STATE;
        startPreview(new MeteringRectangle(rect, MeteringRectangle.METERING_WEIGHT_MAX));
    }

    private CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            process(result);

        }

        @Override
        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
        }

        private void process(CaptureResult result) {
            Face[] camera_faces = result.get(CaptureResult.STATISTICS_FACES);
            int curFocusState = focusState;
            if (camera_faces != null) {
                if (camera_faces.length > 0) {
                    focusState = FOCUS_FACE_STATE;
                    faceRect = new Rect(camera_faces[0].getBounds().left, camera_faces[0].getBounds().top, camera_faces[0].getBounds().right, camera_faces[0].getBounds().bottom);
                    System.out.println("lammy  faces" + camera_faces.length );
                    startPreview(new MeteringRectangle(faceRect, MeteringRectangle.METERING_WEIGHT_MAX));
                }else{
//                    faceRect=null;
//                    if(curFocusState == FOCUS_FACE_STATE){
//                        rePreView();
//                    }
                }
            }else{
//                faceRect=null;
//                if(curFocusState == FOCUS_FACE_STATE){
//                    rePreView();
//                }
            }
            // }
        }
    };

    private void startPreview(final MeteringRectangle rect) {
        if (mCameraSession == null) {
            return;
        }
        try {
            mPreViewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_USE_SCENE_MODE);
            mPreViewBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CaptureRequest.CONTROL_SCENE_MODE_FACE_PRIORITY);
            mPreViewBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, CaptureRequest.STATISTICS_FACE_DETECT_MODE_SIMPLE);
            mPreViewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);

            mPreViewBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, new MeteringRectangle[]{rect});
            mPreViewBuilder.set(CaptureRequest.CONTROL_AE_REGIONS, new MeteringRectangle[]{rect});

            mPreViewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

            mCameraSession.stopRepeating();
            mCameraSession.setRepeatingRequest(mPreViewBuilder.build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                }
            }, null);
            if(focusStateListener!= null){
                focusStateListener.onFocusStateChange(focusState);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

//    private void saveBitmapToSD(final Bitmap bitmap , final String filePath){
//        new Thread() {
//            @Override
//            public void run() {
//                Log.d("lammy", "保存图片");
////                String path = FileUtil.genAbsoluteFolderPath("lammy_camera");
////                File file = new File(savePath, "lammy"+ System.currentTimeMillis() +".jpg");
//                File file = new File(filePath);
//                Log.d("lammy", "保存图片" + file.getAbsolutePath());
//                try {
//                    Matrix matrix = new Matrix();
//                    if(mCameraDevice.getId().equals( 0+""))
//                        matrix.postRotate(90);
//                    if(mCameraDevice.getId().equals( 1+""))
//                        matrix.postRotate(-90);
//                    Bitmap bitmap2 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
//                    bitmap.recycle();
//                    bitmap2.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(file));
//                    bitmap2.recycle();
//
//                    Message msg = new Message();
//                    msg.what = 0;
//                    msg.obj = filePath;
//                    mHandler.sendMessage(msg);
//
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
//            }
//        }.start();
//    }
//
//    Handler mHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            if(msg.what == 0){
//                if (takeCaptureCallback != null) {
//                    takeCaptureCallback.takeCaptureSuccess((String) msg.obj);
//                }
//            }
//        }
//    };




    private void getCameraIdList ()  {
    CameraManager manager = (CameraManager) mContext.getSystemService(Context
            .CAMERA_SERVICE);
    try {
        mCameraIDList  = manager.getCameraIdList();
    } catch (CameraAccessException e) {
        e.printStackTrace();
    }
    mCameraID = mCameraIDList[0];
    }

    public void startPreview(){
        closeCamera();
        if(!isSurfaceAvailable)
            return;

        CameraManager manager = (CameraManager) mContext.getSystemService(Context
                .CAMERA_SERVICE);

//        String mCameraID = CameraCharacteristics.LENS_FACING_FRONT + "";
        try {
            mCameraCharacteristics = manager.getCameraCharacteristics(mCameraID);
//            //画面传感器的面积，单位是像素。
//            maxZoomrect = mCameraCharacteristics.get(CameraCharacteristics
//                    .SENSOR_INFO_ACTIVE_ARRAY_SIZE);
//
//            //最大的数字缩放
//            maxRealRadio = mCameraCharacteristics.get(CameraCharacteristics
//                    .SCALER_AVAILABLE_MAX_DIGITAL_ZOOM).intValue();
//            picRect = new Rect(maxZoomrect);
//            StreamConfigurationMap map = mCameraCharacteristics.get(CameraCharacteristics
//                    .SCALER_STREAM_CONFIGURATION_MAP);
//            Size largest = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)
//            ), new CompareSizeByArea());
//            mPreViewSize =chooseBestPreviewSize(mCameraCharacteristics);// map.getOutputSizes(SurfaceTexture.class)[0];
//            setAspectRatio(
//                    mPreViewSize.getHeight(), mPreViewSize.getWidth());
//            mImageReader = ImageReader.newInstance(mPreViewSize.getWidth(), mPreViewSize.getHeight(),
//                    ImageFormat.JPEG, 2);
//            mImageReader.setOnImageAvailableListener(onImageAvailableListener, null);
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                showToast("请打开应用相机权限！");
                return;
            }
            Log.d(TAG, "onSurfaceTextureAvailable end  ........manager.openCamera.....");
           if(cameraLock == false) {
               manager.openCamera(mCameraID, cameraOpenCallBack, null);
               cameraLock = true;
           }
            Log.d(TAG, "onSurfaceTextureAvailable end  .............");
        } catch (CameraAccessException e) {
            Log.d(TAG, "onSurfaceTextureAvailable failed  .............");
            e.printStackTrace();
        }
    }

    private void showToast(String str) {
        Toast.makeText(mContext, str, Toast.LENGTH_SHORT).show();
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



    public boolean isSupportFlashLight() {
        Boolean available = mCameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
        return available == null ? false : available;
    }



    private int curFlashMode = FLASH_OFF;
    public void setFlash(int flashMode){
        Log.e(TAG, "flashMode......" );
        if(!isSupportFlashLight())
        {
            curFlashMode = FLASH_OFF;
            return;
        }
        curFlashMode = flashMode;
//        rePreView();
        if(flashStateListener != null) {
            flashStateListener.onFlashStateChange(flashMode);
        }
    }


public int getCurrentFlashMode(){
        return curFlashMode;
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


    public void takePhoto(boolean isStopPreview)
    {

        if(mCameraSession == null)
            return;

        LogUtil.e("takePhoto" , "takePhoto,...............");
        this.savePath = savePath;
        try {
//            Log.d(TAG, "captureStillPicture......" + ind);
            CaptureRequest.Builder captureBuilder =
                    mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader.getSurface());

            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_USE_SCENE_MODE);
            captureBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CaptureRequest.CONTROL_SCENE_MODE_FACE_PRIORITY);
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

            if(curFlashMode == FLASH_OFF) {
                captureBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
            }else if(curFlashMode == FLASH_AUTO)
            {
                captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON_AUTO_FLASH);

            }else if(curFlashMode == FLASH_ON) {
//                captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON_ALWAYS_FLASH);
                captureBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
//                captureBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_SINGLE);//用这个部分手机不闪光
            }


            mCameraSession.capture(captureBuilder.build(), null, null);
            mCameraSession.stopRepeating();


        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private void rePreView(){
        if(mCameraSession == null||mCameraDevice == null||mImageReader == null) {
            startPreview();
        }

        try {
            mPreViewBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
            mPreViewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_USE_SCENE_MODE);
            mPreViewBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CaptureRequest.CONTROL_SCENE_MODE_FACE_PRIORITY);
            mPreViewBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, CaptureRequest.STATISTICS_FACE_DETECT_MODE_SIMPLE);
            mPreViewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
            mPreViewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);


            if (faceRect != null) {
                focusState = FOCUS_FACE_STATE;
                mPreViewBuilder.set(CaptureRequest.CONTROL_AF_REGIONS,
                        new MeteringRectangle[]{
                                new MeteringRectangle(faceRect, MeteringRectangle.METERING_WEIGHT_MAX)
                        });
                mPreViewBuilder.set(CaptureRequest.CONTROL_AE_REGIONS,
                        new MeteringRectangle[]{
                                new MeteringRectangle(faceRect, MeteringRectangle.METERING_WEIGHT_MAX)
                        });
            }

            mCameraSession.setRepeatingRequest(mPreViewBuilder.build(), captureCallback, null);
        }catch (CameraAccessException e) {
            e.printStackTrace();
        }


    }


    public void changeCamera()
    {
        closeCamera();
        int len = mCameraIDList.length;

        for(int i = 0 ; i < len ; i++){
            if(mCameraIDList[i].equals(mCameraID)){

               if((i+1) < len){
                   mCameraID = mCameraIDList[i + 1];
               }else{
                   mCameraID = mCameraIDList[0];
               }
               break;
            }
        }
      startPreview();

        if(facingStateListener!= null) {
            if(mCameraID.equals(mCameraIDList[0])) {
                facingStateListener.onFacingStateChange(FACE_BACK);
            }
            else{
                facingStateListener.onFacingStateChange(FACE_FRONT);
            }
        }

    }

    public interface TakeCaptureCallback {
        void takeCaptureSuccess(Bitmap bitmap);
    }

    public interface FacingStateListener {
        void onFacingStateChange(int facingState);
    }

    public interface FlashStateListener {
        void onFlashStateChange(int flashState);
    }

    public  interface FocusStateListener {
        void onFocusStateChange(int focusState);
    }


    public void setFacingStateListener(FacingStateListener facingStateListener) {
        this.facingStateListener = facingStateListener;
    }


    public void setFlashStateListener(FlashStateListener flashStateListener) {
        this.flashStateListener = flashStateListener;
    }

    public void setFocusStateListener(FocusStateListener focusStateListener) {
        this.focusStateListener = focusStateListener;
    }
    public void setTakeCaptureListener(TakeCaptureCallback takeCaptureCallback) {
        this.takeCaptureCallback = takeCaptureCallback;
    }

}

class CompareSizeByArea implements Comparator<Size> {
    @Override
    public int compare(Size lhs, Size rhs) {
        // We cast here to ensure the multiplications won't overflow
        return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                (long) rhs.getWidth() * rhs.getHeight());
    }





}