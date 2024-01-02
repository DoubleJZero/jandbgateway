pipeline {
    agent any
    environment {
        DOCKER = 'sudo docker'
    }

    stages {
        stage('Clone Repository') {
            steps {
                checkout scm
                echo 'Checkout Scm'
            }
        }

        stage('Build image') {
            steps {
                sh 'ls -al'
                sh 'chmod +x ./gradlew'
                sh './gradlew build'
                sh 'docker build -t jandb:msagateway .'
                echo 'Build image...'
            }
        }

        stage('Remove Previous image') {
            steps {
                script {
                    try {
                        sh 'docker stop gatewayService'
                        sh 'docker rm gatewayService'
                    } catch (e) {
                        echo 'fail to stop and remove container'
                    }
                }
            }
        }
        stage('Run New image') {
            steps {
                sh 'docker run --name gatewayService -d -p 9000:9000 -e USE_PROFILE=dev jandb:msagateway'
                echo 'Run New member image'
            }
        }
    }
}