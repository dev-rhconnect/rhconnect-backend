pipeline {
    agent any

    environment {
        IMAGE_NAME = "rhconnect-backend"
        CONTAINER_NAME = "rhconnect-backend"
        VM_USER = "ubuntu"
        VM_HOST = "192.168.20.136"
        APP_PORT = "8080"
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build JAR') {
            steps {
                sh 'mvn package -DskipTests -q'
            }
        }

        stage('Tests') {
            steps {
                sh 'mvn test -q'
            }
            post {
                always {
                    junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                sh "docker build -t ${IMAGE_NAME}:${BUILD_NUMBER} -t ${IMAGE_NAME}:latest ."
            }
        }

        stage('Deploy') {
            steps {
                sshagent(credentials: ['jenkins-ssh-key']) {
                    sh """
                        ssh -o StrictHostKeyChecking=no ${VM_USER}@${VM_HOST} '
                            docker stop ${CONTAINER_NAME} 2>/dev/null || true
                            docker rm   ${CONTAINER_NAME} 2>/dev/null || true
                        '
                        docker save ${IMAGE_NAME}:latest | ssh ${VM_USER}@${VM_HOST} docker load
                        ssh ${VM_USER}@${VM_HOST} '
                            cd /home/ubuntu/rhconnect
                            docker compose up -d --no-deps backend
                        '
                    """
                }
            }
        }
    }

    post {
        success {
            echo "Déploiement backend réussi — build #${BUILD_NUMBER}"
        }
        failure {
            echo "Échec du pipeline backend — build #${BUILD_NUMBER}"
        }
    }
}
