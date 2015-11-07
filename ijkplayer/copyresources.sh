#!/bin/sh

VERSION=0.4.3
OUTPUT=release
ARCHES="arm64 armv7a x86 java"

echo "BUILDING..."
cd ijkplayer/android/
./compile-ijk.sh all
cd -

echo "COPYING..."
for ARCH in $ARCHES; do
   echo "COPY ARCH: $ARCH"
   mkdir -p ../android/mavenlocal/sagetv/ijkplayer/ijkplayer-${ARCH}/${VERSION}/
   cp -fv ijkplayer/android/ijkplayer/ijkplayer-${ARCH}/build/outputs/aar/ijkplayer-${ARCH}-${OUTPUT}.aar ../android/mavenlocal/sagetv/ijkplayer/ijkplayer-${ARCH}/${VERSION}/ijkplayer-${ARCH}-${VERSION}.aar
done

echo "REMOVING CACHES..."
find ~/.gradle/caches/ -iname 'ijk*' -exec rm -fv {} \;


