package com.example.lammy.androidutils.camera;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Paint;
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
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import com.example.lammy.androidutils.Bitmap.BitmapUtil;
import com.example.lammy.androidutils.Bitmap.ImageTypeUtils;
import com.example.lammy.androidutils.log.LogUtil;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;



public class lammyCamera2 extends AutoFitTextureView {

    private  static  String TAG = "lammy-camera2:";
    private SurfaceView surfaceView;
    public lammyCamera2(Context context) {
        super(context);
        init();
    }


    public lammyCamera2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public lammyCamera2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        LogUtil.e("init...............");
        getCameraIdList();
        setSurfaceTextureListener(new mSurfaceTextureListener());
    }

    /**
     *  当想坐滤镜等操作时候，需要设置显示的surface，隐藏原相机预览图片
     */
    private Surface mSurface;
    public void setSurfaceView(SurfaceView surfaceView){
        this.surfaceView = surfaceView;
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(new mSurfaceViewCallback() );
    }


    class mSurfaceViewCallback implements SurfaceHolder.Callback
    {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Thread drawThread = new Thread(new DrawRunnable());
            drawThread.start();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    }

    class mSurfaceTextureListener implements TextureView.SurfaceTextureListener {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
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
    }

    private Size mPreViewSize;
    private ImageReader mPreviewImageReader;
    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mPreViewBuilder;
    private CameraDevice.StateCallback cameraOpenCallBack = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            Log.d(TAG, "相机已经打开");
            try {
                mCameraDevice = camera;
                mPreViewBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                SurfaceTexture texture = getSurfaceTexture();//new SurfaceTexture(2);//
                texture.setDefaultBufferSize(mPreViewSize.getWidth(), mPreViewSize.getHeight());
                mSurface = new Surface(getSurfaceTexture());
                mPreViewBuilder.addTarget( mSurface);
                mPreViewBuilder.addTarget( mPreviewImageReader.getSurface());
                camera.createCaptureSession(Arrays.asList(mSurface, mPreviewImageReader.getSurface
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
            startPreView();
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {

        }
    };

    private void startPreView(){
        isPreview = true;
        if(mCameraSession == null||mCameraDevice == null|| mPreviewImageReader == null) {
            openCamera();
        }
        try {
//            mPreViewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
//            mPreViewBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
//            mPreViewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH);
            //            mPreViewBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
//            mPreViewBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_SINGLE);
//            mPreViewBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
//            mPreViewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON_AUTO_FLASH);
            mPreViewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_USE_SCENE_MODE);
            mPreViewBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CaptureRequest.CONTROL_SCENE_MODE_FACE_PRIORITY);
            mPreViewBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, CaptureRequest.STATISTICS_FACE_DETECT_MODE_SIMPLE);
            mPreViewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

            mCameraSession.setRepeatingRequest(mPreViewBuilder.build(), captureCallback, null);



        }catch (CameraAccessException e) {
            LogUtil.e("lamyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy");
            e.printStackTrace();
        }


    }


    public static int FOCUS_FACE_STATE = 2;
    public static int FOCUS_AUTO_STATE = 0;
    public static int FOCUS_PRINT_STATE = 1;
    int focusState = FOCUS_AUTO_STATE;
    private Rect faceRect;
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

        /**
         * 当有人像时就对人脸进行对焦
         * @param result
         */
        private void process(CaptureResult result) {
            Face[] camera_faces = result.get(CaptureResult.STATISTICS_FACES);
            int curFocusState = focusState;
            if (camera_faces != null) {
                if (camera_faces.length > 0) {
                    focusState = FOCUS_FACE_STATE;
                    faceRect = new Rect(camera_faces[0].getBounds().left, camera_faces[0].getBounds().top, camera_faces[0].getBounds().right, camera_faces[0].getBounds().bottom);
                    System.out.println("lammy  faces" + camera_faces.length);
                    startPreview(new MeteringRectangle(faceRect, MeteringRectangle.METERING_WEIGHT_MAX));
                }
            }
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


            } catch (Exception e) {
                e.printStackTrace();
            }

        }



        private CameraCharacteristics mCameraCharacteristics;
    private boolean cameraLock = false;
    public void openCamera(){
        /**    如果切换相机并且停止了预览，就激活绘制线程        **/
        if(isPreview == false) {
            synchronized ( lammyCamera2.class) {
                isPreview = true;
                lammyCamera2.class.notify();
            }
        }

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(),"请打开应用相机权限！",Toast.LENGTH_LONG).show();
            return;
        }
        closeCamera();

        CameraManager manager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);

        try {
            mCameraCharacteristics = manager.getCameraCharacteristics(mCameraID);
            mPreViewSize =chooseBestPreviewSize(mCameraCharacteristics);

            /*****   获得所有的预览size      ***/
            StreamConfigurationMap map = mCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size [] sizes = map.getOutputSizes(ImageFormat.YUV_420_888);
            for(int i = 0 ; i < sizes.length ; i++){
                LogUtil.e("相机的 "+ i+ "  size："+sizes[i] );
            }

            LogUtil.e("相机的size："+mPreViewSize + "");//1440x1080
            setAspectRatio(mPreViewSize.getHeight(), mPreViewSize.getWidth());



            mPreviewImageReader = ImageReader.newInstance(mPreViewSize.getWidth(), mPreViewSize.getHeight(),
                    ImageFormat.YUV_420_888, 1);//ImageFormat.JPEG
            mPreviewImageReader.setOnImageAvailableListener(onPreViewImageAvailableListener, null);

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
            cameraLock = false;
            if (null != mCameraSession) {
                mCameraSession.close();
                mCameraSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mPreviewImageReader) {
                mPreviewImageReader.close();
                mPreviewImageReader = null;
            }
        } catch (Exception e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
//            mCameraOpenCloseLock.release();
        }
    }



    private SurfaceHolder surfaceHolder;
    private TextView fps_view;
    public void setFpsView(TextView fps_view){
        this.fps_view = fps_view;
    }
    private int fps = 0;
    private Bitmap drawBitmap;
    //连拍功能变量分别为：是否为拍照预览模式，拍照完毕是否停止预览、保存目录、当前保存的帧数、连拍张数
    private boolean isMultipleTakePhoto = false;
    private boolean isStopPreview = false;
    private  String savePath= "";
    private int saveNumber = 0;
    private int takeNumber = 0;



    private ImageReader.OnImageAvailableListener onPreViewImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image mImage = reader.acquireLatestImage();
            if(mImage == null) {
                Log.e(TAG, "onImageAvailable .............mImage == null");
                return;
            }
//            Log.e(TAG, "onImageAvailable ............. start");

            Mat mYuvMat = ImageTypeUtils.cameraYUV_420_888ToMat(mImage);
            Mat rgbMat = new Mat(mImage.getHeight(), mImage.getWidth(), CvType.CV_8UC3);
            Imgproc.cvtColor(mYuvMat, rgbMat, Imgproc.COLOR_YUV2RGB_I420);
            /*****************************更新drawBitmap***********************/
            if(drawBitmap == null)
            {
                drawBitmap = Bitmap.createBitmap(rgbMat.width(), rgbMat.height(), Bitmap.Config.ARGB_8888);
            }

            if (isTransfer) {
                Imgproc.cvtColor(rgbMat, rgbMat, Imgproc.COLOR_RGB2GRAY);
                Utils.matToBitmap(rgbMat, drawBitmap);
            }else {
                Utils.matToBitmap(rgbMat, drawBitmap);
            }

            /*****************************更新face state***********************/
            //在changeCamera的时候切换状态，一面绘图时，最后一帧的时候因为cameraFace 错误，看到切换前一帧 运用到切换后camera的状态
            if(mCameraID.equals(mCameraIDList[0])) {
                cameraFace = FACE_BACK;
            }
            else{
                cameraFace = FACE_FRONT;
            }
//
            /*****************************拍照状态，保存图片***********************/
                // 连拍的功能,
                if(isMultipleTakePhoto && saveNumber < takeNumber){
                    Log.e(TAG, "onImageAvailable .............take some  photos");
                    savePhoto(savePath + "lammy" + System.currentTimeMillis() + ".jpg");
                    saveNumber ++;
                    if(saveNumber >= takeNumber){
                        if(mCameraSession != null && isStopPreview){
                            try {
                                mCameraSession.stopRepeating();
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }
                        isStopPreview = false;
                        saveNumber = 0;
                        isMultipleTakePhoto = false;
                    }
                }

//            Log.e(TAG, "onImageAvailable .............end");
            mImage.close();


//            Mat mYuvMat = BitmapUtil.imageToMat(mImage);
//            Mat bgrMat = new Mat(mImage.getHeight(), mImage.getWidth(), CvType.CV_8UC4);
//            Imgproc.cvtColor(mYuvMat, bgrMat, Imgproc.COLOR_YUV2BGR_I420);
//            Mat rgbaMatOut = new Mat();
//            Imgproc.cvtColor(bgrMat, rgbaMatOut, Imgproc.COLOR_BGR2RGBA, 0);
//            bitmap = Bitmap.createBitmap(bgrMat.cols(), bgrMat.rows(), Bitmap.Config.ARGB_8888);
//            Utils.matToBitmap(rgbaMatOut, bitmap);
//            Mat mYuvMat = BitmapUtil.imageToMat(mImage);
//            Mat rgbMat = new Mat(mImage.getHeight(), mImage.getWidth(), CvType.CV_8UC3);
//            Imgproc.cvtColor(mYuvMat, rgbMat, Imgproc.COLOR_YUV2RGB_I420);
//
//            /*****************************跟新drawBitmap***********************/
//            if(isTransfer) {
//                drawBitmap = Bitmap.createBitmap(rgbMat.width(),rgbMat.height(), Bitmap.Config.ARGB_8888);
//                Utils.matToBitmap(rgbMat , drawBitmap);
//            }else {
//                drawBitmap = Bitmap.createBitmap(rgbMat.width(),rgbMat.height(), Bitmap.Config.ARGB_8888);
//                Utils.matToBitmap(rgbMat , drawBitmap);
//            }
//            Log.d(TAG, "drawBitmap .............跟新");
////                ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
////                byte[] buff = new byte[buffer.remaining()];
////                buffer.get(buff);
////                bitmap = BitmapFactory.decodeByteArray(buff, 0, buff.length);
//
//            long t2 = System.currentTimeMillis();
//            fps = 1000/(int)(t2 - t1 + drawTime);
//            if(fps_view!=null) {
//                fps_view.setText("fps: " + fps);
//            }
////            Log.e(TAG, "onImageAvailable ............."+ (t2 - t1));
//            mImage.close();



            if (reader != null) {
//                    reader.close();
            }

        }
    };



    /**
     * 利用画2帧的时间间隔算fps
     */

    private long preDrawTime = 0;
    private boolean isPreview = true;
    class DrawRunnable implements  Runnable
    {
        @Override
        public void run() {
            Paint paint = new Paint();
            Canvas canvas;

            while(true)
            {
                /***  停止预览  **/
                if(!isPreview){
                    synchronized ( lammyCamera2.class) {
                        try {
                            lammyCamera2.class.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                updateFPS();
                canvas = surfaceHolder.lockCanvas();
                try {
                    if(canvas!= null && drawBitmap!= null)
                    {
//                    Log.d(TAG, "MyRunnable ............."+getCameraFace());
                        int degree = getRotateDegree(mCameraCharacteristics);
                        canvas.drawColor(0x000000);
                        if(cameraFace==FACE_BACK){

                            canvas.translate((float)mPreViewSize.getHeight(),0f);
                            canvas.rotate(degree);
                        }else {

                            canvas.translate(0f , (float)mPreViewSize.getWidth());
                            canvas.rotate(degree);
                        }

                            canvas.drawBitmap(drawBitmap , 0, 0,paint);

                    }
//                    surfaceHolder.unlockCanvasAndPost(canvas);

                }catch (Exception e){

                }finally {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }

        }
    }

    private void updateFPS(){
        long t1 = System.currentTimeMillis();
        fps = 1000/(int)(t1 - preDrawTime);
        ((Activity)getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(System.currentTimeMillis()%20 == 0)
                    fps_view.setText("fps: " + fps);
            }
        });
        preDrawTime = t1;
    }

    private boolean isTransfer = false;
    public void setTransfer(boolean isOpen){
        isTransfer = isOpen;
    }
    public boolean isTransferOpen(){
        return isTransfer;
    }



    private void  savePhoto(final String imagePath ){
        new Thread(new Runnable() {
            @Override
            public void run() {
                BitmapUtil.saveBitmap( imagePath , drawBitmap);
            }
        }).start();
    }


    public void takePhoto(boolean isStopPreview,final String savePath ,int photoNumber)
    {
        if(mCameraSession == null)
            return;
        this.savePath = savePath;
        LogUtil.e("takePhoto" , "takePhoto,...............");

        if(photoNumber == 1) {

          takePhoto(isStopPreview,savePath);
        } else{
            this.isMultipleTakePhoto = true;
            this.takeNumber = photoNumber;
            this.isStopPreview = isStopPreview;
        }
    }



    private String mCameraID;
    public static int FACE_FRONT = 1;
    public static int FACE_BACK = 0;
    private int cameraFace = 0;

    public int getCameraFace(){
        return cameraFace;
    }

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
    public void changeCamera()
    {
        isPreview = false;
        drawBitmap = null;
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

        openCamera();

    }

    public void takePhoto(final boolean isStopPreview ,final String savePath)
    {
        if(mCameraSession == null)
            return;


        LogUtil.e("takePhoto" , "takePhoto,...............");
        this.savePath = savePath;
        try {
           if(mFlashMode == FLASH_SINGLE) {
               mPreViewBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
           }
            // 闪光灯的几种形式，但不同的手机不一样，通常只能生效的就是长亮 FLASH_MODE_TORCH
//            captureBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_SINGLE);
//              captureBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
//            captureBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);

//            captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH_REDEYE);
//            captureBuilder.set(CaptureRequest.CONTROL_AE_MODE,CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH);
//            captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

            mCameraSession.capture(mPreViewBuilder.build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    if(isStopPreview) {
                        isPreview = false;
                    }
                   savePhoto(savePath + System.currentTimeMillis()+".jpg");
                    if(mFlashMode == FLASH_SINGLE) {
                        closeFlash();
                    }
                }
            }, null);

            mCameraSession.stopRepeating();

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }


    }

    /*********************关闭闪光灯*******************************/
    private void closeFlash(){
        try {

            mPreViewBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
            mCameraSession.setRepeatingRequest(mPreViewBuilder.build() , null,null);
            if(isStopPreview) {
                Thread.sleep(50);
                mCameraSession.stopRepeating();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int mFlashMode = 2;
    public  static int FLASH_SINGLE = 0;
    public  static int FLASH_ALWAYS = 1;
    public  static int FLASH_OFF = 2;

    /**
     *
     * @param mFlashMode:FLASH_SINGLE、FLASH_ALWAYS、FLASH_OFF
     */
    public void setFlashMode(int mFlashMode){
        this.mFlashMode = mFlashMode;
        if(mFlashMode == FLASH_ALWAYS){
            mPreViewBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
            try {
                mCameraSession.setRepeatingRequest(mPreViewBuilder.build(),null,null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }else  if(mFlashMode == FLASH_OFF||mFlashMode == FLASH_SINGLE){
            mPreViewBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
            try {
                mCameraSession.setRepeatingRequest(mPreViewBuilder.build(),null,null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }else{
            return;
        }

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
