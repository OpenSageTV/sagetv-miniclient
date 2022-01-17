def getBuildType() {
    if(env.GIT_BRANCH.toLowerCase().endsWith("master"))
    {
        return "release"
    }
    else if(env.GIT_BRANCH.toLowerCase().endsWith("beta"))
    {
        return "release"
    }
    else
    {
        return "debug"
    }
}

pipeline {
    agent { dockerfile true }

    parameters {
        booleanParam(name: 'GOOGLE_DEPLOY_ALPHA', defaultValue: false, description: 'Deploy to Google Play Store closed beta test track')
    }

    environment {
        appName = 'SageTV Android Client'

        KEY_PASSWORD = credentials('keyPassword')
        KEY_ALIAS = credentials('keyAlias')
        KEYSTORE = credentials('keystoreFile')
        STORE_PASSWORD = credentials('storePassword')

        version = sh (script: "./gradlew properties -q | grep \"baseVersion:\" | awk '{print \$2}'", returnStdout: true).trim()
        appversioncode = sh (script: "./gradlew properties -q | grep \"appVersionCode:\" | awk '{print \$2}'", returnStdout: true).trim()
        exoversion = sh (script: "./gradlew properties -q | grep \"exoVersion:\" | awk '{print \$2}'", returnStdout: true).trim()
        exoversioncustomext = sh (script: "./gradlew properties -q | grep \"exoVersionCustomExt:\" | awk '{print \$2}'", returnStdout: true).trim()
        ijkversion = sh (script: "./gradlew properties -q | grep \"ijkVersionDev:\" | awk '{print \$2}'", returnStdout: true).trim()
        VARIANT = "debug"
    }

    stages {

        stage('Set build informaction') {
                steps {
                    echo "Branch is: ${GIT_BRANCH}"
                    script {
                        VARIANT = getBuildType()
                        currentBuild.displayName = "${version}"
                        currentBuild.description = "<B>Build Type:</B> ${VARIANT}<BR>\n"
                        currentBuild.description += "<B>Version:</B> ${version}<BR>\n"
                        currentBuild.description += "<B>Application Version Code:</B> ${appversioncode}<BR>\n"
                        currentBuild.description += "<B>ExoPlayer Version:</B> ${exoversion}<BR>\n"
                        currentBuild.description += "<B>ExoPlayer FFmpeg Ext Version:</B> ${exoversioncustomext}<BR>\n"
                        currentBuild.description += "<B>IJKPlayer Version:</B> ${ijkversion}<BR>\n"
                    }
                }
            }

        stage('Build Bundle') {
            steps {
                script {
                    sh "./gradlew -PstorePass=${STORE_PASSWORD} -Pkeystore=\"${KEYSTORE}\" -Palias=${KEY_ALIAS} -PkeyPass=${KEY_PASSWORD} bundle${VARIANT}"
                    sh "./gradlew -PstorePass=${STORE_PASSWORD} -Pkeystore=\"${KEYSTORE}\" -Palias=${KEY_ALIAS} -PkeyPass=${KEY_PASSWORD} assemble${VARIANT}"
                }
            }
        }

        stage('Publish local') {
             steps {
                cifsPublisher(publishers: [[configName: 'SageTVAndroidClient', transfers: [[cleanRemote: false, excludes: '', flatten: false, makeEmptyDirs: false, noDefaultExcludes: false, patternSeparator: '[, ]+', remoteDirectory: "builds/${version}", remoteDirectorySDF: false, removePrefix: 'android-tv/build/outputs/bundle/', sourceFiles: 'android-tv/build/outputs/bundle/${VARIANT}/*-release.aab']], usePromotionTimestamp: false, useWorkspaceInPromotion: false, verbose: false]])
                cifsPublisher(publishers: [[configName: 'SageTVAndroidClient', transfers: [[cleanRemote: false, excludes: '', flatten: false, makeEmptyDirs: false, noDefaultExcludes: false, patternSeparator: '[, ]+', remoteDirectory: "builds/${version}", remoteDirectorySDF: false, removePrefix: 'android-tv/build/outputs/apk/', sourceFiles: 'android-tv/build/outputs/apk/${VARIANT}/*-release.apk']], usePromotionTimestamp: false, useWorkspaceInPromotion: false, verbose: false]])
             }
        }

        stage('Publish to Play Store') {
            when { expression { params.GOOGLE_DEPLOY_ALPHA } }
            steps {
                androidApkUpload googleCredentialsId: 'Google Play API Access', apkFilesPattern: 'android-tv/build/outputs/bundle/release/*-release.aab', trackName: 'Beta Release Track', rolloutPercentage: "100%"
            }
        }
        
    }
}