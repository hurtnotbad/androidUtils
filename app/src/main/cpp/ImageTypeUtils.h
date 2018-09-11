//
// Created by zhangpeng30 on 2018/9/11.
//
#include <jni.h>
#include <string>
#include <string.h>
#include <malloc.h>
#ifndef ANDROIDUTILS_IMAGETYPEUTILS_H
#define ANDROIDUTILS_IMAGETYPEUTILS_H

#endif //ANDROIDUTILS_IMAGETYPEUTILS_H


JNIEXPORT void JNICALL
Java_com_example_lammy_androidutils_Bitmap_ImageTypeUtils_getYuvDateFromCamera2(JNIEnv *env, jclass type,
                                                                     jbyteArray Camera_y_, jint rowStride_y, jint pixStride_y,
                                                                     jbyteArray Camera_u_, jint rowStride_u, jint pixStride_u,
                                                                     jbyteArray Camera_v_, jint rowStride_v, jint pixStride_v,
                                                                     jint width, jint height, jbyteArray yuv_out_);