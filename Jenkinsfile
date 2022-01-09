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
                script {
                    sh "cd exoplayer"
                    sh "cd exoplayer && ./buildffmpegext.sh all"

                }
            }

        }

        stage('Build Bundle') {
            steps {
                echo 'Building'
                script {
                    //VARIANT = getBuildType()
                    sh "echo `cat \"${KEYSTORE}\"`"
                    sh "./gradlew -PstorePass=${STORE_PASSWORD} -Pkeystore=\"${KEYSTORE}\" -Palias=${KEY_ALIAS} -PkeyPass=${KEY_PASSWORD} build"

                    //sh "./gradlew build"
                }
            }
        }
        
    }
}