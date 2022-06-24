FROM openjdk:11

WORKDIR project/

# Install Build Essentials
RUN apt-get update \
    && apt-get install build-essential -y

RUN wget https://github.com/Kitware/CMake/releases/download/v3.22.5/cmake-3.22.5.tar.gz
RUN tar xvf cmake-3.22.5.tar.gz
RUN cd cmake-3.22.5 && ./bootstrap -DCMAKE_USE_OPENSSL=OFF && make && make install

# Set Environment Variables


ENV SDK_URL="https://dl.google.com/android/repository/commandlinetools-linux-7583922_latest.zip" \
    ANDROID_HOME="/usr/local/cmdline-tools" \
    ANDROID_SDK_ROOT="/usr/local/cmdline-tools" \
    ANDROID_VERSION=29

# Download Android SDK
RUN mkdir "$ANDROID_HOME" .android \
    && cd "$ANDROID_HOME" \
    && curl -o sdk.zip $SDK_URL \
    && unzip sdk.zip \
    && rm sdk.zip \
    && mv "$ANDROID_HOME/cmdline-tools" "$ANDROID_HOME/tools" \
    && mkdir "$ANDROID_HOME/licenses" || true \
    && echo "24333f8a63b6825ea9c5514f83c2829b004d1fee" > "$ANDROID_HOME/licenses/android-sdk-license" \
    && yes | $ANDROID_HOME/tools/bin/sdkmanager --licenses

# Install Android Build Tool and Libraries
RUN $ANDROID_HOME/tools/bin/sdkmanager --update
RUN $ANDROID_HOME/tools/bin/sdkmanager "build-tools;29.0.2" \
    "platforms;android-${ANDROID_VERSION}" \
    "platform-tools" \
    "ndk;21.0.6113669"

CMD ["/bin/bash"]