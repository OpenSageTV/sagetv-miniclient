git pull the ExoPlayer repo and then make 2 changes...

1. edit core_settings.gradle and change version
2. edit build.gradle and disable javadoc
```groovy
subprojects {
    tasks.withType(Javadoc).all { enabled = false }
}
```

3. edit extensions/ffmpeg and add...
```groovy
ext {
    releaseArtifact = 'extension-ffmpeg'
    releaseDescription = 'FFMpeg extension for ExoPlayer.'
}
apply from: '../../publish.gradle'
```
