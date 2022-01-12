pipeline {
    agent { dockerfile true }
    environment {
        appName = 'SageTV Android Client'

        KEY_PASSWORD = credentials('keyPassword')
        KEY_ALIAS = credentials('keyAlias')
        KEYSTORE = credentials('keystoreFile')
        STORE_PASSWORD = credentials('storePassword')
    }
    stages {

        stage('Build exoplayer') {

            steps {
                echo 'Building ExoPlayer'
                dir('exoplayer') {
                    //sh('bash ./buildffmpegext.sh all')
                }
            }

        }

        stage('Build Bundle') {
            steps {
                echo 'Building'
                script {
                    sh "printenv"
                    sh "unset NDK_PATH"
                    sh "./gradlew -PstorePass=${STORE_PASSWORD} -Pkeystore=\"${KEYSTORE}\" -Palias=${KEY_ALIAS} -PkeyPass=${KEY_PASSWORD} bundlerelease"
                }
            }
        }

        stage('Publish to Play Store') {
            steps {
                androidApkUpload googleCredentialsId: 'Google Play API Access', apkFilesPattern: '**/*-release.aab', trackName: 'Beta Release Track', rolloutPercentage: "100%"
            }
        }
        
    }
}