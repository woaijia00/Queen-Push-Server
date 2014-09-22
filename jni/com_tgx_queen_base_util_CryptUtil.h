#ifndef TGX_CRYPT_H
#define TGX_CRYPT_H
#undef __cplusplus
#ifdef __APPLE__
#include </System/Library/Frameworks/JavaVM.framework/Headers/jni.h>
#elif  __FreeBSD__
#include <jni.h>
#else
#include <jni.h>
#endif
#include <stdlib.h>
#include <arpa/inet.h>
#include "sha1.h"
#include "sha256.h"
#include "ecc_crypt.h"
JNIEXPORT jbyteArray JNICALL Java_com_tgx_queen_base_util_CryptUtil__1pubKey(JNIEnv *, jobject, jstring);
JNIEXPORT jbyteArray JNICALL Java_com_tgx_queen_base_util_CryptUtil__1sha1(JNIEnv *, jobject, jbyteArray);
JNIEXPORT jbyteArray JNICALL Java_com_tgx_queen_base_util_CryptUtil__1sha256(JNIEnv *, jobject, jbyteArray);
JNIEXPORT jbyteArray JNICALL Java_com_tgx_queen_base_util_CryptUtil__1getRc4Key(JNIEnv *, jobject, jstring, jbyteArray, jbyteArray);
JNIEXPORT jint JNICALL Java_com_tgx_queen_base_util_CryptUtil__1getVlsize(JNIEnv *, jobject);
JNIEXPORT void JNICALL Java_com_tgx_queen_base_util_CryptUtil__1test(JNIEnv *, jobject);
#endif /*TGX_CRYPT_H*/
