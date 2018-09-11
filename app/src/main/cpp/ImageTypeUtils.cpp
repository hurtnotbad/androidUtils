//
// Created by zhangpeng30 on 2018/9/11.
//
#include <jni.h>
#include <string>
#include <string.h>
#include <malloc.h>
typedef unsigned char uint8;
extern "C"
JNIEXPORT void JNICALL
Java_com_example_lammy_androidutils_Bitmap_ImageTypeUtils_getYuvDateFromCamera2(JNIEnv *env, jclass type,
                                                                     jbyteArray Camera_y_, jint rowStride_y, jint pixStride_y,
                                                                     jbyteArray Camera_u_, jint rowStride_u, jint pixStride_u,
                                                                     jbyteArray Camera_v_, jint rowStride_v, jint pixStride_v,
                                                                     jint width, jint height, jbyteArray yuv_out_) {
    jbyte *Camera_y = env->GetByteArrayElements(Camera_y_, NULL);
    jbyte *Camera_u = env->GetByteArrayElements(Camera_u_, NULL);
    jbyte *Camera_v = env->GetByteArrayElements(Camera_v_, NULL);
    jbyte *p = env->GetByteArrayElements(yuv_out_, NULL);

// TODO

    int row = 0;
    for(;row < height; row++){
//        LOGE("getdata yuv y row = %d", row);
        memcpy(p ,Camera_y_ ,  width*sizeof(uint8));
        if (height - row != 1) {
            Camera_y = Camera_y + rowStride_y;
        }
        p += width;
    }


    row = 0;
    int width_uv = width/2;
    int height_uv = height/2;
    uint8 * rowData = (uint8*)malloc(sizeof(uint8)*rowStride_y);
    for(;row < height_uv; row++){
        if ((height_uv - row) == 1) {
//            memcpy(rowData_u ,Camera_u , rowStride_u*sizeof(uint8));
            memcpy(rowData , Camera_u, (width - pixStride_u + 1)*sizeof(uint8));

        } else {
            memcpy(rowData,Camera_u ,  rowStride_u*sizeof(uint8));
            Camera_u = Camera_u + rowStride_u;
        }

        for (int col = 0; col < width_uv ; col++) {
            *p = rowData[col * pixStride_u];//2079355..存了5755 crash
            p++;
        }
    }
//    LOGE("getdata yuv u row = %d", 11);

    row = 0;
//    uint8 * rowData_v = (uint8*)malloc(sizeof(uint8)*rowStride_v);
    for(;row < height_uv; row++){

        if ((height_uv - row) == 1) {
            memcpy(rowData , Camera_v, (width - pixStride_v + 1)*sizeof(uint8));

        } else {
            memcpy(rowData,Camera_v ,  rowStride_v*sizeof(uint8));
            Camera_v = Camera_v + rowStride_v;
        }
//        LOGE("getdata yuv v row = %d", row);


        for (int col = 0; col < width_uv ; col++) {
            *p = rowData[col * pixStride_v];//2079355..存了5755 crash
            p++;
        }
    }


    env->ReleaseByteArrayElements(Camera_y_, Camera_y, 0);
    env->ReleaseByteArrayElements(Camera_u_, Camera_u, 0);
    env->ReleaseByteArrayElements(Camera_v_, Camera_v, 0);
    env->ReleaseByteArrayElements(yuv_out_, p, 0);
}