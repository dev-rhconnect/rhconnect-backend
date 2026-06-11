pipeline {
    agent any

    environment {
        IMAGE_NAME    = "rhconnect-backend"
        COMPOSE_DIR   = "/home/mame/rhconnect"
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
                    junit testResults: 'target/surefire-reports/*.xml',
                          allowEmptyResults: true
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                // Patch l'image existante (pas de pull Docker Hub nécessaire)
                sh """
                    cat > /tmp/Dockerfile.backend.patch << 'EOF'
FROM rhconnect-backend:latest
COPY target/*.jar /app/app.jar
EOF
                    DOCKER_BUILDKIT=0 docker build \
                        -f /tmp/Dockerfile.backend.patch \
                        -t ${IMAGE_NAME}:${BUILD_NUMBER} \
                        -t ${IMAGE_NAME}:latest .
                """
            }
        }

        stage('Deploy') {
            steps {
                sh "cd ${COMPOSE_DIR} && docker compose up -d --no-deps backend"
            }
        }
    }

    post {
        success {
            echo "Backend déployé — build #${BUILD_NUMBER}"
        }
        failure {
            echo "Échec du pipeline backend — build #${BUILD_NUMBER}"
        }
    }
}
