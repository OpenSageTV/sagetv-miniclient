#!/usr/bin/env bash

if [ ! -d ijkplayer ] ; then
    echo "Fetching IJKPlayer Sources"
#    git clone git@github.com:stuckless/ijkplayer.git
    git clone https://github.com/Bilibili/ijkplayer.git
    echo ""
#    cd ijkplayer
#    git remote add upstream https://github.com/Bilibili/ijkplayer.git
#    git fetch upstream
#    git rebase upstream/master
#    cd ..
fi

if [ ! -d Ndk ] ; then
    echo "Setting up NDK"
    NDK=r11c
    mkdir Ndk
    cd Ndk/
    # wget http://dl.google.com/android/ndk/android-ndk-${NDK}-linux-x86_64.bin
    wget http://dl.google.com/android/repository/android-ndk-${NDK}-linux-x86_64.zip
    #chmod 755 android-ndk-${NDK}-linux-x86_64.bin
    #./android-ndk-${NDK}-linux-x86_64.bin
    unzip ./android-ndk-${NDK}-linux-x86_64.zip
    cd ..
fi




