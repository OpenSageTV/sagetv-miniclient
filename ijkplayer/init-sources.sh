#!/usr/bin/env bash

if [ ! -d ijkplayer ] ; then
    echo "Fetching IJKPlayer Sources"
    git clone git@github.com:stuckless/ijkplayer.git
    echo ""
#    cd ijkplayer
#    git remote add upstream https://github.com/Bilibili/ijkplayer.git
#    git fetch upstream
#    git rebase upstream/master
#    cd ..
fi

if [ ! -d Ndk ] ; then
    echo "Setting up NDK"
    mkdir Ndk
    cd Ndk/
    wget http://dl.google.com/android/ndk/android-ndk-r10e-linux-x86_64.bin
    chmod 755 android-ndk-r10e-linux-x86_64.bin
    ./android-ndk-r10e-linux-x86_64.bin
    cd ..
fi




