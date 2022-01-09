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

        stage('Credents') {

            steps {
                withCredentials([file(credentialsId: 'keystoreFile', variable: 'mySecretFile')]) {
                    // some block can be a groovy block as well and the variable will be available to the groovy script
                    sh '''
                         echo "This is the directory of the secret file \"$mySecretFile\""
                         echo "This is the content of the file `cat \"$mySecretFile\"`"
                       '''
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