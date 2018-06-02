#!/usr/bin/env bash

HOST_PLATFORM="linux-x86_64"
NDK_VERSION=${NDK_VERSION:-r13b}
DOWNLOAD_NDK=${DOWNLOAD_NDK:-1}

if [ "$NDK_PATH" = "" ] ; then
    if [ -e `pwd`/Ndk/android-ndk-${NDK_VERSION} ] ; then
       NDK_PATH=`pwd`/Ndk/android-ndk-${NDK_VERSION}
    else
        if [ "1" = "${DOWNLOAD_NDK}" ] ; then
            echo "Setting up NDK"
            mkdir Ndk
            cd Ndk/
            wget http://dl.google.com/android/repository/android-ndk-${NDK_VERSION}-linux-x86_64.zip
            unzip ./android-ndk-${NDK_VERSION}-linux-x86_64.zip
            cd ..
            NDK_PATH=`pwd`/Ndk/android-ndk-${NDK_VERSION}
        else
            echo "Set NDK_PATH to be the location of your Ndk"
            exit 1
        fi
    fi
fi

pushd .

EXOPLAYER_ROOT=`realpath ../ExoPlayer`
echo "ExoPlayer Root: $EXOPLAYER_ROOT"
FFMPEG_EXT_PATH="${EXOPLAYER_ROOT}/extensions/ffmpeg/src/main"
# can be used to pass "--enable-decoder=ac3" and other enable args
FFMPEG_EXT_ARGS=${FFMPEG_EXT_ARGS:-""}

if [ -e ${FFMPEG_EXT_PATH}/jni/ffmpeg ] ; then
    echo "found existing ffmpeg"
    #rm -rf ${FFMPEG_EXT_PATH}/jni/ffmpeg
    #git clone git://source.ffmpeg.org/ffmpeg ffmpeg || exit 1
else
    cd ${FFMPEG_EXT_PATH}/jni/
    git clone git://source.ffmpeg.org/ffmpeg ffmpeg || exit 1
    cd -
fi

COMMON_OPTIONS="\
    --target-os=android \
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
    --disable-swresample \
    --enable-avresample \
    --enable-decoder=vorbis \
    --enable-decoder=opus \
    --enable-decoder=flac \
    --enable-decoder=ac3 \
    ${FFMPEG_EXT_ARGS} \
    " && \
cd "${FFMPEG_EXT_PATH}/jni" && \
cd ffmpeg && \
./configure \
    --libdir=android-libs/armeabi-v7a \
    --arch=arm \
    --cpu=armv7-a \
    --cross-prefix="${NDK_PATH}/toolchains/arm-linux-androideabi-4.9/prebuilt/${HOST_PLATFORM}/bin/arm-linux-androideabi-" \
    --sysroot="${NDK_PATH}/platforms/android-9/arch-arm/" \
    --extra-cflags="-march=armv7-a -mfloat-abi=softfp" \
    --extra-ldflags="-Wl,--fix-cortex-a8" \
    --extra-ldexeflags=-pie \
    ${COMMON_OPTIONS} \
    && \
make -j4 && make install-libs && \
make clean && ./configure \
    --libdir=android-libs/arm64-v8a \
    --arch=aarch64 \
    --cpu=armv8-a \
    --cross-prefix="${NDK_PATH}/toolchains/aarch64-linux-android-4.9/prebuilt/${HOST_PLATFORM}/bin/aarch64-linux-android-" \
    --sysroot="${NDK_PATH}/platforms/android-21/arch-arm64/" \
    --extra-ldexeflags=-pie \
    ${COMMON_OPTIONS} \
    && \
make -j4 && make install-libs && \
make clean && ./configure \
    --libdir=android-libs/x86 \
    --arch=x86 \
    --cpu=i686 \
    --cross-prefix="${NDK_PATH}/toolchains/x86-4.9/prebuilt/${HOST_PLATFORM}/bin/i686-linux-android-" \
    --sysroot="${NDK_PATH}/platforms/android-9/arch-x86/" \
    --extra-ldexeflags=-pie \
    --disable-asm \
    ${COMMON_OPTIONS} \
    && \
make -j4 && make install-libs && \
make clean || exit 1

cd "${FFMPEG_EXT_PATH}"/jni && \
${NDK_PATH}/ndk-build APP_ABI="armeabi-v7a arm64-v8a x86" -j4 || exit 1

#cd "${FFMPEG_EXT_PATH}/jni" && \
#if [ -e ffmpeg ] ; then rm -rf ffmpeg ; fi && \
#git clone git://source.ffmpeg.org/ffmpeg ffmpeg && cd ffmpeg && \
#./configure \
#    --libdir=android-libs/armeabi-v7a \
#    --arch=arm \
#    --cpu=armv7-a \
#    --cross-prefix="${NDK_PATH}/toolchains/arm-linux-androideabi-4.9/prebuilt/linux-x86_64/bin/arm-linux-androideabi-" \
#    --target-os=android \
#    --sysroot="${NDK_PATH}/platforms/android-9/arch-arm/" \
#    --extra-cflags="-march=armv7-a -mfloat-abi=softfp" \
#    --extra-ldflags="-Wl,--fix-cortex-a8" \
#    --extra-ldexeflags=-pie \
#    --disable-static \
#    --enable-shared \
#    --disable-doc \
#    --disable-programs \
#    --disable-everything \
#    --disable-avdevice \
#    --disable-avformat \
#    --disable-swscale \
#    --disable-postproc \
#    --disable-avfilter \
#    --disable-symver \
#    --enable-avresample \
#    --enable-decoder=vorbis \
#    --enable-decoder=opus \
#    --enable-decoder=flac \
#    ${FFMPEG_EXT_ARGS} \
#    && \
#make -j4 && \
#make install-libs
#
#cd "${FFMPEG_EXT_PATH}"/jni && \
#${NDK_PATH}/ndk-build APP_ABI=armeabi-v7a -j4

popd

echo "build completed"
echo "   FFMPEG Extra Args were ${FFMPEG_EXT_ARGS}"
echo -n
echo "You can now run "
echo "# ./gradlew assemble publishToMavenLocal"
echo "From the ${EXOPLAYER_ROOT} directory to create, assemble, and publish the the artifacts"
echo "to your local maven repository. (replacing the version with whatever version you like)"
echo -n
echo "In your projects that are consuming ExoPlayer, you can use the dependencies"
echo " compile 'com.google.android.exoplayer:exoplayer:version@aar'"
echo " compile 'com.google.android.exoplayer:extension-ffmpeg:version@aar'"
