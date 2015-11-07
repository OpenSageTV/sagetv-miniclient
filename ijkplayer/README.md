 2030  git clone git@github.com:stuckless/ijkplayer.git
 2031  cd ijkplayer
 2033  git remote -v
 2034  git remote add upstream https://github.com/Bilibili/ijkplayer.git
 2035  git remote -v
 2036  git fetch upstream
 2037  git rebase upstream/master
 2038  git push
 2040  sudo dpkg-reconfigure dash

 2041  cd
 2042  cd Android
 2044  mkdir Ndk
 2045  cd Ndk/
 2046  wget http://dl.google.com/android/ndk/android-ndk-r10e-linux-x86_64.bin
 2047  chmod 755 android-ndk-r10e-linux-x86_64.bin
 2048  ./android-ndk-r10e-linux-x86_64.bin

 2050  cd
 2051  cd git/ijkplayer
 2052  export ANDROID_NDK=/home/sls/Android/Ndk/android-ndk-r10e
 2053  export ANDROID_SDK=/home/sls/Android/Sdk/
 2054  cd config/
 2055  ls
 2056  ls -al
 2057  less module-default.sh
 2058  ln -sf module-default.sh module.sh
 2059  ls -al
 2060  cd ..
 2062  ./init-android.sh
 2063  cd android/contrib/
 2064  ./compile-ffmpeg.sh clean
 2065  ./compile-ffmpeg.sh all
 2066  cd ..
  2067  ./compile-ijk.sh all
 2069  cd ..
 2071  cd android/
 2073  cd ijkplayer/
 2075  less build.gradle
 2076  cd ..
 2078  cd ijkplayer/
 2079  ./gradlew
 2081  cp ~/git/sagetv-miniclient/local.properties .
 2082  ./gradlew
 2083  vim build.gradle
 2084  ./gradlew
 2086  find . -iname *.so
 2088  cd ijkplayer-sample/
 2090  cd ..
 2092  less ijkplayer-sample/build.gradle
 2096  ./gradlew assemble
 
 # use copyresources to build/update mavelLocal