#!/bin/sh

sudo dpkg --add-architecture i386
sudo apt-get update
sudo apt-get install zlib1g:i386 libstdc++6:i386 libc6:i386
sudo apt-get install automake ant autopoint cmake build-essential libtool patch pkg-config protobuf-compiler ragel subversion unzip git yasm
wget http://dl.google.com/android/android-sdk_r24.4-linux.tgz
tar -zxvf android-sdk_r24.4-linux.tgz 
wget http://dl.google.com/android/ndk/android-ndk-r10e-linux-x86_64.bin
chmod 755 android-ndk-r10e-linux-x86_64.bin 
./android-ndk-r10e-linux-x86_64.bin 

export ANDROID_SDK=$PWD/android-sdk-linux/
export ANDROID_NDK=$PWD/android-ndk-r10e/
export PATH=$PATH:$ANDROID_SDK/platform-tools:$ANDROID_SDK/tools

git clone git://git.videolan.org/vlc-ports/android.git
cd android

 2035  vim vlc/contrib/contrib-android-arm-linux-androideabi/libgpg-error/configure.ac

./compile.sh 

