#!/usr/bin/env bash

VERSION='r2.2.0-SNAPSHOT'

if [ "$ANDROID_SDK" = "" ] ; then
    echo "Set ANDROID_SDK to be the location of your Sdk, USING DEFAULT"
    export ANDROID_SDK=/home/seans/Android/Sdk/

fi

if [ "$ANDROID_HOME" = "" ] ; then
    export ANDROID_HOME="$ANDROID_SDK"
fi

pushd .

if [ -d ExoPlayer ] ; then
    cd ExoPlayer
    git pull
else
    REPO="https://github.com/stuckless/ExoPlayer.git"
    echo "Fetching ExoPlayer Sources from ${REPO}"
    #REPO="https://github.com/google/ExoPlayer.git"
    git clone -b sagetv-miniclient $REPO
    cd ExoPlayer
fi

pushd .
echo "Building FFMpeg"
if [ ! -d extensions/ffmpeg/contrib ] ; then
    echo "Missing ffmpeg extension dir"
    exit 1
fi

cd extensions/ffmpeg/contrib
export FFMPEG_EXT_ARGS="--enable-decoder=ac3 --enable-decoder=eac3 --enable-decoder=dts --enable-decoder=dcadec --enable-decoder=aac"
./build-natives.sh || exit 1
popd

echo "Building..."
echo "Setting BUILD RELEASE $VERSION"
./gradlew -Dexoplayer.version="${VERSION}" assemble publishToMavenLocal
cd ..

echo "Gradle Dependency is 'com.google.android.exoplayer:exoplayer:$VERSION'"

popd