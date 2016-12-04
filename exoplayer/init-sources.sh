#!/usr/bin/env bash

if [ ! -d ExoPlayer ] ; then
    echo "Fetching ExoPlayer Sources"
    REPO="https://github.com/google/ExoPlayer.git"
    git clone $REPO
    echo ""
fi

if [ ! -d Ndk ] ; then
    echo "Setting up NDK"
    NDK=r13b
    mkdir Ndk
    cd Ndk/
    wget http://dl.google.com/android/repository/android-ndk-${NDK}-linux-x86_64.zip
    unzip ./android-ndk-${NDK}-linux-x86_64.zip
    cd ..
fi




