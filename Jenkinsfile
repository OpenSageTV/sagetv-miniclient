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

        stage('Build Bundle') {
            steps {
                echo 'Building'
                script {
                    //VARIANT = getBuildType()
                    withCredentials([file(credentialsId: 'keystoreFile', variable: 'KEYSTOREFILE')]){

                        sh "./gradlew -PstorePass=${STORE_PASSWORD} -Pkeystore=${KEYSTOREFILE} -Palias=${KEY_ALIAS} -PkeyPass=${KEY_PASSWORD} build"
                    }
                    //sh "./gradlew build"
                }
            }
        }
        
    }
}