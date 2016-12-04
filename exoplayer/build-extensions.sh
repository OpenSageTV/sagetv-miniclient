#!/usr/bin/env bash

if [ "$ANDROID_SDK" = "" ] ; then
    echo "Set ANDROID_SDK to be the location of your Sdk, USING DEFAULT"
    export ANDROID_SDK=/home/seans/Android/Sdk/

fi

if [ "$ANDROID_HOME" = "" ] ; then
    export ANDROID_HOME="$ANDROID_SDK"
fi

if [ ! -d Ndk ] ; then
    echo "run init-sources to init the Ndk"
    exit 1
fi

export ANDROID_NDK=`pwd`/Ndk/android-ndk-r13b

cd ExoPlayer
EXOPLAYER_ROOT="$(pwd)"
FFMPEG_EXT_PATH="${EXOPLAYER_ROOT}/extensions/ffmpeg/src/main"

export NDK_PATH=$ANDROID_NDK

cd "${FFMPEG_EXT_PATH}/jni" && \
if [ -e ffmpeg ] ; then rm -rf ffmpeg ; fi && \
git clone git://source.ffmpeg.org/ffmpeg ffmpeg && cd ffmpeg && \
./configure \
    --libdir=android-libs/armeabi-v7a \
    --arch=arm \
    --cpu=armv7-a \
    --cross-prefix="${NDK_PATH}/toolchains/arm-linux-androideabi-4.9/prebuilt/linux-x86_64/bin/arm-linux-androideabi-" \
    --target-os=android \
    --sysroot="${NDK_PATH}/platforms/android-9/arch-arm/" \
    --extra-cflags="-march=armv7-a -mfloat-abi=softfp" \
    --extra-ldflags="-Wl,--fix-cortex-a8" \
    --extra-ldexeflags=-pie \
    --disable-static \
    --enable-shared \
    --disable-doc \
    --disable-programs \
    --disable-everything \
    --disable-avdevice \
    --disable-avformat \
    --disable-swscale \
    --disable-postproc \
    --disable-avfilter \
    --disable-symver \
    --enable-avresample \
    --enable-decoder=vorbis \
    --enable-decoder=opus \
    --enable-decoder=flac \
    --enable-decoder=aac \
    --enable-decoder=aac_latm \
    --enable-decoder=aac \
    --enable-decoder=ac3 \
    && \
make -j4 && \
make install-libs

echo "Building JNI Parts"

cd "${FFMPEG_EXT_PATH}"/jni && \
${NDK_PATH}/ndk-build APP_ABI=armeabi-v7a -j4

cd "${EXOPLAYER_ROOT}"
cd ..

echo "done"