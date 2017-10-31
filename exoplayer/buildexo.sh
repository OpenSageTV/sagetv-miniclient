#!/usr/bin/env bash

VERSION='r2.5.4-SNAPSHOT'

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
    #REPO="https://github.com/stuckless/ExoPlayer.git"
    #git clone -b sagetv-miniclient $REPO
    REPO="https://github.com/google/ExoPlayer.git"
    echo "Fetching ExoPlayer Sources from ${REPO}"
    git clone $REPO
    cd ExoPlayer
fi

pushd .
echo "Building FFMpeg"
cd ../build-ffmpeg/

./build-natives.sh || exit 1
popd

echo "Building..."
echo "Setting BUILD RELEASE $VERSION"
./gradlew -Dexoplayer.version="${VERSION}" assemble publishToMavenLocal || exit 1
cd ..

echo "Gradle Dependency is 'com.google.android.exoplayer:exoplayer:$VERSION'"

popd