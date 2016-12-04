#!/usr/bin/env bash

VERSION='r2.0.4.1-SNAPSHOT'

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
git pull

echo "Setting BUILD RELEASE $VERSION"

cd library
cp build.gradle build.gradle.orig
# version = 'r1.5.4'
cat build.gradle.orig | sed "s/.*version =.*/    version = \"${VERSION}\"/g" > build.gradle
cd ..

echo "Building..."
./gradlew assemble publishToMavenLocal
cd ..

echo "Gradle Dependency is 'com.google.android.exoplayer:exoplayer:$VERSION'"
