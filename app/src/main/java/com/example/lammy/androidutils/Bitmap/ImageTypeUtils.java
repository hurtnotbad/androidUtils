package com.example.lammy.androidutils.Bitmap;

import android.graphics.ImageFormat;
import android.media.Image;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.nio.ByteBuffer;

public class ImageTypeUtils {
    static {
        System.loadLibrary("opencv_java3");
    }

    public static Mat cameraYUV420ToMat(Image image) {
        ByteBuffer buffer;
        int rowStride;
        int pixelStride;
        int width = image.getWidth();
        int height = image.getHeight();
        int offset = 0;

        Image.Plane[] planes = image.getPlanes();
        byte[] data = new byte[image.getWidth() * image.getHeight() * ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888) / 8];
        byte[] rowData = new byte[planes[0].getRowStride()];

        for (int i = 0; i < planes.length; i++) {
            buffer = planes[i].getBuffer();
            rowStride = planes[i].getRowStride();
            pixelStride = planes[i].getPixelStride();
            int w = (i == 0) ? width : width / 2;
            int h = (i == 0) ? height : height / 2;
            for (int row = 0; row < h; row++) {
                int bytesPerPixel = ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888) / 8;
                if (pixelStride == bytesPerPixel) {
                    int length = w * bytesPerPixel;
                    buffer.get(data, offset, length);

                    if (h - row != 1) {
                        buffer.position(buffer.position() + rowStride - length);
                    }
                    offset += length;
                } else {


                    if (h - row == 1) {
                        buffer.get(rowData, 0, width - pixelStride + 1);
                    } else {
                        buffer.get(rowData, 0, rowStride);
                    }

                    for (int col = 0; col < w; col++) {
                        data[offset++] = rowData[col * pixelStride];
                    }
                }
            }
        }

        Mat mat = new Mat(height + height / 2, width, CvType.CV_8UC1);
        mat.put(0, 0, data);

        return mat;
    }

    public static Mat cameraYUV_420_888ToMat(Image image) {
        ByteBuffer buffer;
        int rowStride;
        int pixelStride;
        int width = image.getWidth();
        int height = image.getHeight();
        int offset = 0;

        Image.Plane[] planes = image.getPlanes();
        byte[] data = new byte[width * height * 3/2];
        byte[] rowData = new byte[planes[0].getRowStride()];

        for (int i = 0; i < planes.length; i++) {
            buffer = planes[i].getBuffer();
            rowStride = planes[i].getRowStride();
            int w = (i == 0) ? width : width / 2;
            int h = (i == 0) ? height : height / 2;
            for (int row = 0; row < h; row++) {
                if (i == 0) {

                    buffer.get(data, offset, w);
                    if (h - row != 1) {
                        buffer.position(buffer.position() + rowStride - w);
                    }
                    offset += w;
                } else {
                    // 每一行像素数据不是连续存储的
                    if (h - row == 1) {
                        buffer.get(rowData, 0, width - 1);
                    } else {
                        buffer.get(rowData, 0, rowStride);
                    }

                    for (int col = 0; col < w; col++) {
                        data[offset++] = rowData[col * 2];
                    }
                }
            }
        }

        Mat mat = new Mat(height + height / 2, width, CvType.CV_8UC1);
        mat.put(0, 0, data);

        return mat;
    }

    public static byte[] cameraYUV_420_888ToByte(Image image) {

        int w = image.getWidth();
        int h = image.getHeight();
        int offset = 0;

        Image.Plane[] planes = image.getPlanes();
        byte[] data = new byte[w*h*3/2];

        // 取y的数据
        ByteBuffer buffer0 = planes[0].getBuffer();
        int rowStride0 =  planes[0].getRowStride();
//
//        long t1 = System.currentTimeMillis();
//        byte []buffer_y = new byte[planes[0].getBuffer().remaining()];
//        planes[0].getBuffer().get(buffer_y);
//        byte []buffer_u = new byte[planes[1].getBuffer().remaining()];
//        planes[1].getBuffer().get(buffer_u);
//        byte []buffer_v = new byte[planes[2].getBuffer().remaining()];
//        planes[2].getBuffer().get(buffer_v);
//        long t2 = System.currentTimeMillis();
//        LogUtil.e("getbuffer y time = " + (t2-t1));
        for(int row = 0; row < h ; row ++){
            buffer0.get(data, offset, w);
            if (h - row != 1) {
                buffer0.position(buffer0.position() + rowStride0 - w);
            }
            offset += w;
        }

        int w2 = w/2;
        int h2 = h/2;
        // 取u的数据
        ByteBuffer buffer1 = planes[1].getBuffer();
        int rowStride1 =  planes[1].getRowStride();
        byte[] rowData = new byte[planes[0].getRowStride()];
        for(int row = 0; row < h2 ; row ++){

            // 每一行像素数据不是连续存储的
            if (h2 - row == 1) {
                buffer1.get(rowData, 0, w - 1);
            } else {
                buffer1.get(rowData, 0, rowStride1);
            }

            for (int col = 0; col < w2; col++) {
                data[offset++] = rowData[col * 2];
            }
        }


        // 取v的数据
        ByteBuffer buffer2 = planes[2].getBuffer();
        int rowStride2 =  planes[2].getRowStride();

        for(int row = 0; row < h2 ; row ++){

            // 每一行像素数据不是连续存储的
            if (h2 - row == 1) {
                buffer2.get(rowData, 0, w - 1);
            } else {
                buffer2.get(rowData, 0, rowStride2);
            }

            for (int col = 0; col < w2; col++) {
                data[offset++] = rowData[col * 2];
            }
        }


//        Mat mat = new Mat(3*h/2, w, CvType.CV_8UC1);
//        mat.put(0, 0, data);
//        return mat;

        return data;
    }

    public byte[] getYuvDateFromCamera2(Image mImage){

        if(mImage == null)
            return null;

        Image.Plane[]planes = mImage.getPlanes();

        byte []buffer_y = new byte[planes[0].getBuffer().remaining()];
        planes[0].getBuffer().get(buffer_y);
        byte []buffer_u = new byte[planes[1].getBuffer().remaining()];
        planes[1].getBuffer().get(buffer_u);
        byte []buffer_v = new byte[planes[2].getBuffer().remaining()];
        planes[2].getBuffer().get(buffer_v);


        byte [] yuv_bytes = new byte[mImage.getWidth()*mImage.getHeight()*3/2];
        getYuvDateFromCamera2(buffer_y,planes[0].getRowStride(), planes[0].getPixelStride(),
                buffer_u,planes[1].getRowStride(), planes[1].getPixelStride(),
                buffer_v,planes[2].getRowStride(), planes[2].getPixelStride(),
                mImage.getWidth(),mImage.getHeight(),
                yuv_bytes
        );

        return yuv_bytes;
    }


    public static native void getYuvDateFromCamera2( byte[] Camera_y, int rowStride_y, int pixStride_y,
                                          byte[] Camera_u, int rowStride_u, int pixStride_u,
                                          byte[] Camera_v, int rowStride_v, int pixStride_v,
                                          int width, int height, byte[] yuv_out
    );
}
