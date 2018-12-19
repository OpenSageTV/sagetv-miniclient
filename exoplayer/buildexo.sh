#!/usr/bin/env bash

# BUILD NOTES
# Edit ExoPlayer constants.gradle and add set the version

# Edit build.gradle allprojects and add
#tasks.withType(Javadoc).all { enabled = false }

# in the ffmpeg extension build.gradle add
#ext {
#    releaseArtifact = 'extension-ffmpeg'
#    releaseDescription = 'FFMpeg extension for ExoPlayer.'
#}
#apply from: '../../publish.gradle'

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

VERSION=`cat constants.gradle | grep releaseVersion | grep -v Code | tr '=' '\n' | grep -v 'release'`

echo "Building..."
echo "Setting BUILD RELEASE $VERSION"
./gradlew assemble publishToMavenLocal || exit 1
cd ..

echo "Gradle Dependency is 'com.google.android.exoplayer:exoplayer:$VERSION'"

popd