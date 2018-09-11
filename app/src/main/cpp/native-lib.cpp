#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring

JNICALL
Java_com_example_lammy_androidutils_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_lammy_androidutils_Bitmap_ImageTypeUtils_getYuvDate(JNIEnv *env, jclass type,
                                                                     jbyteArray Camera_y_,
                                                                     jint rowStride_y,
                                                                     jint pixStride_y,
                                                                     jbyteArray Camera_u_,
                                                                     jint rowStride_u,
                                                                     jint pixStride_u,
                                                                     jbyteArray Camera_v_,
                                                                     jint rowStride_v,
                                                                     jint pixStride_v, jint width,
                                                                     jint height,
                                                                     jbyteArray yuv_out_) {
    jbyte *Camera_y = env->GetByteArrayElements(Camera_y_, NULL);
    jbyte *Camera_u = env->GetByteArrayElements(Camera_u_, NULL);
    jbyte *Camera_v = env->GetByteArrayElements(Camera_v_, NULL);
    jbyte *yuv_out = env->GetByteArrayElements(yuv_out_, NULL);

    // TODO

    env->ReleaseByteArrayElements(Camera_y_, Camera_y, 0);
    env->ReleaseByteArrayElements(Camera_u_, Camera_u, 0);
    env->ReleaseByteArrayElements(Camera_v_, Camera_v, 0);
    env->ReleaseByteArrayElements(yuv_out_, yuv_out, 0);
}