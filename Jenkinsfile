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



        stage('Ge build information') {
            steps {
                script {
                    def version = sh (script: "./gradlew properties -q | grep \"baseVersion:\" | awk '{print \$2}'", returnStdout: true).trim()
                    currentBuild.displayName = "${version}"
                    currentBuild.description = "Version: ${version}\n"
                    currentBuild.description += "Second line test"
                    currentBuild.description += "Third line test"
                }
            }

        }

        stage('Build Bundle') {
            steps {
                echo 'Building'
                script {
                    sh "printenv"
                    //sh "unset NDK_PATH"
                    //sh "./gradlew -PstorePass=${STORE_PASSWORD} -Pkeystore=\"${KEYSTORE}\" -Palias=${KEY_ALIAS} -PkeyPass=${KEY_PASSWORD} bundleRelease"
                }
            }
        }

        /*
        stage('Publish local') {

             steps {
                cifsPublisher(publishers: [[configName: 'SageTVAndroidClient', transfers: [[cleanRemote: false, excludes: '', flatten: false, makeEmptyDirs: false, noDefaultExcludes: false, patternSeparator: '[, ]+', remoteDirectory: '', remoteDirectorySDF: false, removePrefix: 'android-tv/build/outputs/bundle/release', sourceFiles: 'android-tv/build/outputs/bundle/release/*-release.aab']], usePromotionTimestamp: false, useWorkspaceInPromotion: false, verbose: false]])
             }

        }
        */




        //stage('Publish to Play Store') {
        //    steps {
        //        //androidApkUpload googleCredentialsId: 'Google Play API Access', apkFilesPattern: 'android-tv/build/outputs/bundle/release/*-release.aab', trackName: 'Beta Release Track', rolloutPercentage: "100%"
        //    }
        //}
        
    }
}