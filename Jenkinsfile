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
        stage('Credentials file'){
            steps {
                withCredentials([file(credentialsId: 'keystoreFile', variable: 'KEYSTOREFILE')]){
                    writeFile file: 'jvlsagetvkeystore', text: readFile(KEYSTOREFILE)
                }
            }

        }

        stage('Build Bundle') {
            steps {
                echo 'Building'
                script {
                    //VARIANT = getBuildType()
                    echo ${KEYSTORE}
                    sh "./gradlew -PstorePass=${STORE_PASSWORD} -Pkeystore=${KEYSTORE} -Palias=${KEY_ALIAS} -PkeyPass=${KEY_PASSWORD} build"
                    //sh "./gradlew build"
                }
            }
        }
        
    }
}