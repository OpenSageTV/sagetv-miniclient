#!/usr/bin/bash

if [ -z "$1" ]; then

    echo "Action was not provided and is required build.sh [exoplayer]"
    exit

fi

FFmpegExtVersion="2.18.0"
ExoPlayerVersion="r${FFmpegExtVersion}"
FFmpegVersion="release/4.2"

#I think we should check these and maybe
#export ANDROID_SDK_ROOT=/home/jvl711/Documents/sdk/
#export ANDROID_HOME=/home/jvl711/Documents/sdk/

ENABLED_DECODERS=(vorbis opus flac alac pcm_mulaw pcm_alaw mp3 amrnb amrwb aac ac3 eac3 dca mlp truehd)

ROOT_PATH="$(pwd)"
BUILD_PATH="${ROOT_PATH}/build"
HOST_PLATFORM="linux-x86_64"
EXOPLAYER_ROOT="${BUILD_PATH}/ExoPlayer"
NDK_PATH="${BUILD_PATH}/android-ndk-r21"
export ANDROID_NDK_HOME="${BUILD_PATH}/android-ndk-r21"
FFMPEG_PATH="${BUILD_PATH}/FFmpeg"
FFMPEG_EXT_PATH="${EXOPLAYER_ROOT}/extensions/ffmpeg/src/main"
FFMPEG_EXT_OUTPUT_PATH="${EXOPLAYER_ROOT}/extensions/ffmpeg/buildout/outputs/aar"

echo "ROOT_PATH: $ROOT_PATH" 
echo "BUILD_PATH: $ROOT_PATH"  
echo "EXOPLAYER_ROOT: $EXOPLAYER_ROOT"
echo "NDK_PATH: $NDK_PATH"
echo "FFMPEG_PATH: $FFMPEG_PATH"
echo "FFMPEG_EXT_PATH: $FFMPEG_EXT_PATH"
echo "HOST_PLATFORM: $HOST_PLATFORM"


if [ ! -d $BUILD_PATH ]; then

	mkdir $BUILD_PATH

fi



#--------------------------------------------------- ExoPlayer Checkout ---------------------------------------------------#

if [ $1 = "exoplayer" ] || [ $1 = "all" ]; then

	cd 	$BUILD_PATH

	echo "Setting up ExoPlayer source code for version: $ExoPlayerVersion"

	if [ -d $EXOPLAYER_ROOT  ]; then
		echo "ExoPlayer already exist..."
		cd $EXOPLAYER_ROOT
		git pull 
		git reset --hard
		git checkout $ExoPlayerVersion
	else
		echo "ExoPlayer does not exist. Cloning library from GitHub"
		git clone https://github.com/google/ExoPlayer.git
		cd $EXOPLAYER_ROOT
		git checkout $ExoPlayerVersion
	fi

	cd 	$BUILD_PATH

fi

#--------------------------------------------------- FFMpeg Checkout ---------------------------------------------------#

if [ $1 = "ffmpeg" ] || [ $1 = "all" ]; then

	cd 	$BUILD_PATH

	if [ -d $FFMPEG_PATH ]; then
		echo "FFmpeg already exist..."
		cd $FFMPEG_PATH 
		git pull
		git reset --hard
		git checkout $FFmpegVersion
	else
		echo "FFmpeg does not exist. Cloning library from GitHub"
		git clone https://github.com/FFmpeg/FFmpeg.git
		cd $FFMPEG_PATH
		git checkout $FFmpegVersion

	fi	

	cd 	$BUILD_PATH

	echo "Adding symbolic link to FFmpeg source code in ExoPlayer project"
	cd "${FFMPEG_EXT_PATH}/jni"
	ln -s "$FFMPEG_PATH" ffmpeg

	cd 	$BUILD_PATH

fi


#------------------------------------------------------ NDK Download ------------------------------------------------------#

if [ $1 = "ndk" ] || [ $1 = "all" ]; then

	cd 	$BUILD_PATH

	echo "Setting up NDK library and downloading and unzipping if necessary"

	if [ -d $ANDROID_NDK_HOME ]; then
		echo "NDK exists"
	else
		echo "Downloading and unzippiong NDK"
		wget https://dl.google.com/android/repository/android-ndk-r21-linux-x86_64.zip
		unzip android-ndk-r21-linux-x86_64.zip
	fi

	cd 	$BUILD_PATH

fi

#------------------------------------------------------ Build FFmpeg ------------------------------------------------------#

if [ $1 = "buildffmpeg" ] || [ $1 = "all" ]; then

	cd 	$BUILD_PATH

	echo "Building FFmpeg..."

	cd "${FFMPEG_EXT_PATH}/jni"
	./build_ffmpeg.sh "${FFMPEG_EXT_PATH}" "${NDK_PATH}" "${HOST_PLATFORM}" "${ENABLED_DECODERS[@]}"

	cd 	$BUILD_PATH

fi

#----------------------------------------------------- Build ExoPlayer ----------------------------------------------------#

if [ $1 = "buildexoplayer" ] || [ $1 = "all" ]; then

	cd 	$BUILD_PATH
	
	echo "Building Exoplayer..."
	cd "$EXOPLAYER_ROOT"
	./gradlew assemble

	cd 	$BUILD_PATH	

fi

if [ $1 = "deploy" ] || [ $1 = "all" ]; then

	cd 	$BUILD_PATH

	echo "Package Exoplayer..."

	cp "${FFMPEG_EXT_OUTPUT_PATH}/extension-ffmpeg-release.aar" "../../libs/extension-ffmpeg-${FFmpegExtVersion}.aar"



fi

#--------------------------------------------------------------------------------------------------------------------------#


