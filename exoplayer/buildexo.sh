#!/usr/bin/env bash

VERSION='r1.5.5-SNAPSHOT'
EXO_REPO="https://github.com/google/ExoPlayer.git"
#EXO_REPO="https://github.com/Narflex/ExoPlayer.git"
#EXO_BRANCH="ps-extractor2"

if [ "$ANDROID_SDK" = "" ] ; then
    echo "Set ANDROID_SDK to be the location of your Sdk, USING DEFAULT"
    export ANDROID_SDK=/home/sls/Android/Sdk/
fi

if [ "$ANDROID_HOME" = "" ] ; then
    export ANDROID_HOME="$ANDROID_SDK"
fi

echo "ANDROID HOME is $ANDROID_HOME"

if [ ! -e ExoPlayer ] ; then
    git clone $EXO_REPO
    if [ "$EXO_BRANCH" != "" ] ; then
        git clone $EXO_REPO
        cd ExoPlayer
        git checkout $EXO_BRANCH
        cd  ..
    else
        git clone $EXO_REPO
    fi

    echo "Patching..."
    cd ExoPlayer
    for p in ../*.patch ; do
        echo "Applying $p"
        patch -p0 < $p
        echo "Applyied $p"
    done
    echo "Removing back up files (causes issues with javadoc)"
    find . -name '*.orig' -exec rm -fv {} \;
    cd ..
fi

cd ExoPlayer
git pull

echo "Setting BUILD RELEASE $VERSION"

cd library
cp build.gradle build.gradle.orig
# version = 'r1.5.4'
cat build.gradle.orig | sed "s/.*version =.*/    version = \"${VERSION}\"/g" > build.gradle
cd ..

echo "Building..."
./gradlew clean assemble publishToMavenLocal
cd ..

echo "Gradle Dependency is 'com.google.android.exoplayer:exoplayer:$VERSION'"
